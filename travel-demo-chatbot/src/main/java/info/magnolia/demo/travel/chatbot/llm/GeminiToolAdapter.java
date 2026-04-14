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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.demo.travel.chatbot.tools.Tool;

import java.util.List;
import java.util.Optional;

public final class GeminiToolAdapter {

    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private GeminiToolAdapter() {}

    public record FunctionCall(String name, JsonNode args) {}

    public static ObjectNode toGeminiTools(List<Tool> tools) {
        ObjectNode root = JSON.objectNode();
        ArrayNode toolsArray = root.putArray("tools");
        ObjectNode entry = toolsArray.addObject();
        ArrayNode decls = entry.putArray("function_declarations");
        for (Tool t : tools) {
            ObjectNode d = decls.addObject();
            d.put("name", t.name());
            d.put("description", t.description());
            d.set("parameters", t.parametersSchema());
        }
        return root;
    }

    public static Optional<FunctionCall> parseFunctionCall(JsonNode geminiResponse) {
        JsonNode parts = geminiResponse.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray()) return Optional.empty();
        for (JsonNode part : parts) {
            JsonNode fc = part.path("functionCall");
            if (fc.isObject() && fc.hasNonNull("name")) {
                return Optional.of(new FunctionCall(fc.get("name").asText(), fc.path("args")));
            }
        }
        return Optional.empty();
    }

    public static String parseTextReply(JsonNode geminiResponse) {
        JsonNode parts = geminiResponse.path("candidates").path(0).path("content").path("parts");
        StringBuilder sb = new StringBuilder();
        if (parts.isArray()) for (JsonNode part : parts) {
            if (part.hasNonNull("text")) sb.append(part.get("text").asText());
        }
        return sb.toString();
    }
}
