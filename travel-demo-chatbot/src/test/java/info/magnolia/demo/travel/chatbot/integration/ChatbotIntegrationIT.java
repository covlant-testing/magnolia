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
package info.magnolia.demo.travel.chatbot.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import info.magnolia.context.MgnlContext;
import info.magnolia.demo.travel.chatbot.ChatbotModule;
import info.magnolia.demo.travel.chatbot.i18n.LanguageResolver;
import info.magnolia.demo.travel.chatbot.llm.GeminiClient;
import info.magnolia.demo.travel.chatbot.personalization.VisitorTraitsResolver;
import info.magnolia.demo.travel.chatbot.ratelimit.SessionRateLimiter;
import info.magnolia.demo.travel.chatbot.rest.ChatEndpoint;
import info.magnolia.demo.travel.chatbot.rest.ChatTurnRequest;
import info.magnolia.demo.travel.chatbot.rest.ChatTurnResponse;
import info.magnolia.demo.travel.chatbot.session.ChatSessionStore;
import info.magnolia.demo.travel.chatbot.tools.ToursTool;
import info.magnolia.demo.travel.chatbot.tools.ToolRegistry;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ChatbotIntegrationIT {

    @Rule public WireMockRule wm = new WireMockRule(0);

    @Before
    public void setUp() throws Exception {
        MockUtil.initMockContext();
        MockSession toursSession = new MockSession("tours");
        MockUtil.getMockContext().addSession("tours", toursSession);
        Node tour = toursSession.getRootNode().addNode("bali-7day", "mgnl:content");
        tour.setProperty("name", "bali-7day");
        tour.setProperty("region", "Asia");
        tour.setProperty("priceUsd", 1500L);
        tour.setProperty("durationDays", 7L);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Test
    public void roundTripWithToolCallProducesAssistantText() throws Exception {
        // First call: model asks to search tours
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .inScenario("turn").whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"candidates\":[{\"content\":{\"parts\":[{\"functionCall\":"
                                + "{\"name\":\"tours\",\"args\":{\"operation\":\"searchTours\",\"region\":\"Asia\"}}}]}}]}"))
                .willSetStateTo("post-tool"));

        // Second call: model produces text
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .inScenario("turn").whenScenarioStateIs("post-tool")
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Try the bali-7day tour.\"}]}}]}")));

        ChatbotModule cfg = new ChatbotModule();
        GeminiClient gemini = new GeminiClient("test-key",
                "http://localhost:" + wm.port() + "/v1beta", 5000, durMs -> {});
        ToolRegistry registry = new ToolRegistry(List.of(new ToursTool()));
        ChatSessionStore sessions = new ChatSessionStore();
        SessionRateLimiter limiter = mock(SessionRateLimiter.class);
        when(limiter.tryAcquire(any())).thenReturn(true);
        LanguageResolver lang = mock(LanguageResolver.class);
        when(lang.resolve(any())).thenReturn("en");
        VisitorTraitsResolver traits = mock(VisitorTraitsResolver.class);
        when(traits.resolve()).thenReturn(Map.of());

        ChatEndpoint endpoint = new ChatEndpoint("test-key", cfg, gemini, registry,
                sessions, limiter, lang, traits);

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getCookies()).thenReturn(null);

        Response resp = endpoint.turn(req, new ChatTurnRequest("Where should I go in Asia?"));
        assertEquals(200, resp.getStatus());
        ChatTurnResponse body = (ChatTurnResponse) resp.getEntity();
        assertEquals("Try the bali-7day tour.", body.getAssistantMessage());
        assertNotNull(resp.getCookies().get("MGNL_CHAT_SID"));
    }
}
