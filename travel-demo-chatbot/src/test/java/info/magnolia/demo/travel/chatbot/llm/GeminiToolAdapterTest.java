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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.demo.travel.chatbot.tools.Tool;
import info.magnolia.demo.travel.chatbot.tools.ToolContext;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

public class GeminiToolAdapterTest {

    private static Tool stubTool(String name) {
        return new Tool() {
            @Override public String name() { return name; }
            @Override public String description() { return name + " desc"; }
            @Override public JsonNode parametersSchema() {
                return JsonNodeFactory.instance.objectNode().put("type", "object");
            }
            @Override public JsonNode invoke(JsonNode args, ToolContext ctx) { return null; }
        };
    }

    @Test
    public void toGeminiToolsBuildsFunctionDeclarations() {
        ObjectNode out = GeminiToolAdapter.toGeminiTools(List.of(stubTool("a"), stubTool("b")));
        JsonNode decls = out.get("tools").get(0).get("function_declarations");
        assertEquals(2, decls.size());
        assertEquals("a", decls.get(0).get("name").asText());
        assertEquals("b", decls.get(1).get("name").asText());
        assertEquals("object", decls.get(0).get("parameters").get("type").asText());
    }

    @Test
    public void parsesFunctionCallFromResponse() throws Exception {
        String body = "{\"candidates\":[{\"content\":{\"parts\":["
                + "{\"functionCall\":{\"name\":\"tours\",\"args\":{\"operation\":\"searchTours\"}}}]}}]}";
        Optional<GeminiToolAdapter.FunctionCall> fc =
                GeminiToolAdapter.parseFunctionCall(new ObjectMapper().readTree(body));
        assertTrue(fc.isPresent());
        assertEquals("tours", fc.get().name());
        assertEquals("searchTours", fc.get().args().get("operation").asText());
    }

    @Test
    public void noFunctionCallReturnsEmpty() throws Exception {
        String body = "{\"candidates\":[{\"content\":{\"parts\":["
                + "{\"text\":\"Bali in November is a great match.\"}]}}]}";
        assertFalse(GeminiToolAdapter.parseFunctionCall(new ObjectMapper().readTree(body)).isPresent());
    }

    @Test
    public void parsesTextReply() throws Exception {
        String body = "{\"candidates\":[{\"content\":{\"parts\":["
                + "{\"text\":\"Hello \"},{\"text\":\"world\"}]}}]}";
        assertEquals("Hello world", GeminiToolAdapter.parseTextReply(new ObjectMapper().readTree(body)));
    }
}
