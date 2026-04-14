/**
 * This file Copyright (c) 2026 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.demo.travel.chatbot.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.demo.travel.chatbot.ChatbotModule;
import info.magnolia.demo.travel.chatbot.i18n.LanguageResolver;
import info.magnolia.demo.travel.chatbot.llm.GeminiClient;
import info.magnolia.demo.travel.chatbot.llm.GeminiToolAdapter;
import info.magnolia.demo.travel.chatbot.llm.LlmException;
import info.magnolia.demo.travel.chatbot.personalization.VisitorTraitsResolver;
import info.magnolia.demo.travel.chatbot.ratelimit.SessionRateLimiter;
import info.magnolia.demo.travel.chatbot.session.ChatSessionStore;
import info.magnolia.demo.travel.chatbot.session.ConversationHistory;
import info.magnolia.demo.travel.chatbot.session.Turn;
import info.magnolia.demo.travel.chatbot.tools.Tool;
import info.magnolia.demo.travel.chatbot.tools.ToolContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/chatbot/v1")
public class ChatEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ChatEndpoint.class);
    private static final String SESSION_COOKIE = "MGNL_CHAT_SID";
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private final String apiKey;
    private final ChatbotModule cfg;
    private final GeminiClient gemini;
    private final info.magnolia.demo.travel.chatbot.tools.ToolRegistry registry;
    private final ChatSessionStore sessions;
    private final SessionRateLimiter limiter;
    private final LanguageResolver languageResolver;
    private final VisitorTraitsResolver traitsResolver;

    public ChatEndpoint(
            String apiKey,
            ChatbotModule cfg,
            GeminiClient gemini,
            info.magnolia.demo.travel.chatbot.tools.ToolRegistry registry,
            ChatSessionStore sessions,
            SessionRateLimiter limiter,
            LanguageResolver languageResolver,
            VisitorTraitsResolver traitsResolver) {
        this.apiKey = apiKey;
        this.cfg = cfg;
        this.gemini = gemini;
        this.registry = registry;
        this.sessions = sessions;
        this.limiter = limiter;
        this.languageResolver = languageResolver;
        this.traitsResolver = traitsResolver;
    }

    @POST
    @Path("/turn")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response turn(HttpServletRequest req, ChatTurnRequest body) {
        long startNs = System.nanoTime();

        if (apiKey == null || apiKey.isBlank()) {
            return Response.status(503).build();
        }

        if (body == null || body.getUserMessage() == null || body.getUserMessage().isBlank()) {
            return Response.status(400).build();
        }

        if (body.getUserMessage().length() > cfg.getMaxUserMessageChars()) {
            return Response.status(413).build();
        }

        String sessionId = readSessionCookie(req);
        boolean isNewSession = sessionId == null;
        if (isNewSession) {
            sessionId = UUID.randomUUID().toString();
        }

        if (!limiter.tryAcquire(sessionId)) {
            return Response.status(429).build();
        }

        String language = languageResolver.resolve(req);
        Map<String, String> traits = traitsResolver.resolve();

        ConversationHistory history = sessions.getOrCreate(sessionId);
        ConversationHistory working = copyHistory(history);
        working.append(Turn.user(body.getUserMessage()));

        String assistantReply;
        try {
            assistantReply = runToolLoop(working, language, traits);
        } catch (LlmException e) {
            log.warn("LLM call failed for session {}: {}", hashSessionId(sessionId), e.getMessage());
            return Response.status(502).build();
        }

        history.append(Turn.user(body.getUserMessage()));
        history.append(Turn.assistant(assistantReply));
        history.trimTo(cfg.getHistoryTurnLimit() * 2);

        long latencyMs = (System.nanoTime() - startNs) / 1_000_000;
        log.info("turn sessionIdHash={} language={} latencyMs={} status=200",
                hashSessionId(sessionId), language, latencyMs);

        Response.ResponseBuilder rb = Response.ok(new ChatTurnResponse(assistantReply, Collections.emptyList()));
        if (isNewSession) {
            rb.cookie(new NewCookie(SESSION_COOKIE, sessionId, "/", null, null, NewCookie.DEFAULT_MAX_AGE, false));
        }
        return rb.build();
    }

    private String runToolLoop(ConversationHistory working, String language, Map<String, String> traits)
            throws LlmException {
        List<Tool> tools = registry.enabled(cfg.getEnabledTools());
        ToolContext ctx = new ToolContext(language);
        String systemPrompt = buildSystemPrompt(language, traits);

        for (int i = 0; i < cfg.getMaxToolIterations(); i++) {
            ObjectNode geminiBody = buildGeminiRequest(systemPrompt, working.turns(), tools);
            JsonNode geminiResponse = gemini.generate(cfg.getModel(), geminiBody);

            java.util.Optional<GeminiToolAdapter.FunctionCall> callOpt =
                    GeminiToolAdapter.parseFunctionCall(geminiResponse);
            if (callOpt.isPresent()) {
                GeminiToolAdapter.FunctionCall call = callOpt.get();
                String toolResult;
                try {
                    JsonNode result = registry.invoke(call.name(), call.args(), ctx);
                    toolResult = result != null ? result.toString() : "{}";
                } catch (info.magnolia.demo.travel.chatbot.tools.ToolException e) {
                    toolResult = "{\"error\":\"" + e.getMessage() + "\"}";
                }
                working.append(Turn.tool(call.name(), toolResult));
                continue;
            }

            String text = GeminiToolAdapter.parseTextReply(geminiResponse);
            if (!text.isBlank()) {
                return text;
            }
        }

        return "I'm sorry, I couldn't generate a response at this time. Please try again.";
    }

    private String buildSystemPrompt(String language, Map<String, String> traits) {
        String traitsStr = traits.isEmpty() ? "none" : traits.toString();
        return cfg.getSystemPromptTemplate()
                .replace("${language}", language)
                .replace("${visitorTraits}", traitsStr);
    }

    private ObjectNode buildGeminiRequest(String systemPrompt, List<Turn> turns, List<Tool> tools) {
        ObjectNode root = JSON.objectNode();

        ObjectNode sysInstruction = root.putObject("systemInstruction");
        ArrayNode sysParts = sysInstruction.putArray("parts");
        sysParts.addObject().put("text", systemPrompt);

        ArrayNode contents = root.putArray("contents");
        for (Turn t : turns) {
            ObjectNode entry = contents.addObject();
            switch (t.role()) {
                case USER:
                    entry.put("role", "user");
                    entry.putArray("parts").addObject().put("text", t.content());
                    break;
                case ASSISTANT:
                    entry.put("role", "model");
                    entry.putArray("parts").addObject().put("text", t.content());
                    break;
                case TOOL:
                    entry.put("role", "function");
                    ObjectNode funcResponsePart = entry.putArray("parts").addObject();
                    ObjectNode funcResponse = funcResponsePart.putObject("functionResponse");
                    funcResponse.put("name", t.toolName());
                    funcResponse.putObject("response").put("content", t.content());
                    break;
                default:
                    break;
            }
        }

        if (!tools.isEmpty()) {
            ObjectNode toolsNode = GeminiToolAdapter.toGeminiTools(tools);
            root.set("tools", toolsNode.get("tools"));
        }

        return root;
    }

    private static String readSessionCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (SESSION_COOKIE.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    private static ConversationHistory copyHistory(ConversationHistory source) {
        ConversationHistory copy = new ConversationHistory();
        for (Turn t : source.turns()) {
            copy.append(t);
        }
        return copy;
    }

    private static String hashSessionId(String sessionId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(sessionId.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            return "????????";
        }
    }
}
