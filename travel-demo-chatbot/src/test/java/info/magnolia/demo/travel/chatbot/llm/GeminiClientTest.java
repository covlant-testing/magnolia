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
package info.magnolia.demo.travel.chatbot.llm;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Rule;
import org.junit.Test;

public class GeminiClientTest {

    @Rule
    public WireMockRule wm = new WireMockRule(0);

    private GeminiClient newClient() {
        return new GeminiClient("test-api-key", "http://localhost:" + wm.port() + "/v1beta",
                5000, durMs -> { /* no-op sleep */ });
    }

    private static ObjectNode emptyBody() {
        return JsonNodeFactory.instance.objectNode();
    }

    @Test
    public void successReturnsParsedBody() throws Exception {
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .withHeader("x-goog-api-key", equalTo("test-api-key"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"hi\"}]}}]}")));
        JsonNode out = newClient().generate("gemini-3-flash-preview", emptyBody());
        assertEquals("hi", out.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText());
    }

    @Test
    public void retriesOn500ThenSucceeds() throws Exception {
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .inScenario("retry").whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("once"));
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .inScenario("retry").whenScenarioStateIs("once")
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"ok\":true}")));
        JsonNode out = newClient().generate("m", emptyBody());
        assertEquals(true, out.get("ok").asBoolean());
    }

    @Test
    public void givesUpAfterMaxRetries() {
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .willReturn(aResponse().withStatus(503)));
        LlmException ex = assertThrows(LlmException.class,
                () -> newClient().generate("m", emptyBody()));
        assertEquals(503, ex.status());
    }

    @Test
    public void surfaces4xxImmediately() {
        stubFor(post(urlPathMatching("/v1beta/models/.*:generateContent"))
                .willReturn(aResponse().withStatus(400).withBody("bad")));
        LlmException ex = assertThrows(LlmException.class,
                () -> newClient().generate("m", emptyBody()));
        assertEquals(400, ex.status());
    }
}
