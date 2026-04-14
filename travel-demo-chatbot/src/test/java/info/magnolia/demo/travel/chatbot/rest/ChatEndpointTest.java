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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.magnolia.demo.travel.chatbot.ChatbotModule;
import info.magnolia.demo.travel.chatbot.i18n.LanguageResolver;
import info.magnolia.demo.travel.chatbot.llm.GeminiClient;
import info.magnolia.demo.travel.chatbot.llm.LlmException;
import info.magnolia.demo.travel.chatbot.personalization.VisitorTraitsResolver;
import info.magnolia.demo.travel.chatbot.ratelimit.SessionRateLimiter;
import info.magnolia.demo.travel.chatbot.session.ChatSessionStore;
import info.magnolia.demo.travel.chatbot.tools.ToolRegistry;

import java.util.Collections;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

public class ChatEndpointTest {

    private GeminiClient gemini;
    private ToolRegistry registry;
    private ChatSessionStore sessions;
    private SessionRateLimiter limiter;
    private LanguageResolver lang;
    private VisitorTraitsResolver traits;
    private ChatbotModule cfg;
    private ChatEndpoint endpoint;
    private HttpServletRequest req;

    @Before
    public void setup() {
        gemini = mock(GeminiClient.class);
        registry = mock(ToolRegistry.class);
        sessions = new ChatSessionStore();
        limiter = mock(SessionRateLimiter.class);
        lang = mock(LanguageResolver.class);
        traits = mock(VisitorTraitsResolver.class);
        cfg = new ChatbotModule();
        endpoint = new ChatEndpoint("test-key", cfg, gemini, registry, sessions, limiter, lang, traits);
        req = mock(HttpServletRequest.class);
        when(req.getCookies()).thenReturn(new Cookie[0]);
        when(limiter.tryAcquire(any())).thenReturn(true);
        when(lang.resolve(any())).thenReturn("en");
        when(traits.resolve()).thenReturn(Map.of());
        when(registry.enabled(any())).thenReturn(Collections.emptyList());
    }

    @Test
    public void happyPathReturnsAssistantReply() throws Exception {
        ObjectMapper m = new ObjectMapper();
        JsonNode reply = m.readTree("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hello!\"}]}}]}");
        when(gemini.generate(eq("gemini-3-flash-preview"), any())).thenReturn(reply);
        Response resp = endpoint.turn(req, new ChatTurnRequest("hi"));
        assertEquals(200, resp.getStatus());
        ChatTurnResponse body = (ChatTurnResponse) resp.getEntity();
        assertEquals("Hello!", body.getAssistantMessage());
    }

    @Test
    public void rateLimitReturns429() {
        when(limiter.tryAcquire(any())).thenReturn(false);
        Response resp = endpoint.turn(req, new ChatTurnRequest("hi"));
        assertEquals(429, resp.getStatus());
    }

    @Test
    public void oversizedMessageReturns413() {
        cfg.setMaxUserMessageChars(5);
        Response resp = endpoint.turn(req, new ChatTurnRequest("123456"));
        assertEquals(413, resp.getStatus());
    }

    @Test
    public void missingApiKeyReturns503() {
        ChatEndpoint noKey = new ChatEndpoint(null, cfg, gemini, registry, sessions, limiter, lang, traits);
        Response resp = noKey.turn(req, new ChatTurnRequest("hi"));
        assertEquals(503, resp.getStatus());
    }

    @Test
    public void llmFailureReturns502() throws Exception {
        when(gemini.generate(any(), any())).thenThrow(new LlmException(503, "down"));
        Response resp = endpoint.turn(req, new ChatTurnRequest("hi"));
        assertEquals(502, resp.getStatus());
    }

    @Test
    public void responseSetsSessionCookieWhenAbsent() throws Exception {
        ObjectMapper m = new ObjectMapper();
        when(gemini.generate(any(), any())).thenReturn(
                m.readTree("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"ok\"}]}}]}"));
        Response resp = endpoint.turn(req, new ChatTurnRequest("hi"));
        assertNotNull(resp.getCookies().get("MGNL_CHAT_SID"));
    }
}
