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
package info.magnolia.demo.travel.chatbot.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.context.MgnlContext;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

@Singleton
public class EditorialTool implements Tool {

    private static final String DESTINATIONS_PATH = "/travel/destinations";
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    @Override
    public String name() {
        return "editorial";
    }

    @Override
    public String description() {
        return "Search editorial content pages. operation=searchEditorial with required {query} and optional {tags}.";
    }

    @Override
    public JsonNode parametersSchema() {
        ObjectNode schema = JSON.objectNode();
        schema.put("type", "object");
        ObjectNode props = JSON.objectNode();
        props.set("operation", JSON.objectNode().put("type", "string"));
        props.set("query", JSON.objectNode().put("type", "string"));
        ObjectNode tagsSchema = JSON.objectNode();
        tagsSchema.put("type", "array");
        tagsSchema.set("items", JSON.objectNode().put("type", "string"));
        props.set("tags", tagsSchema);
        schema.set("properties", props);
        ArrayNode required = JSON.arrayNode();
        required.add("operation");
        schema.set("required", required);
        return schema;
    }

    @Override
    public JsonNode invoke(JsonNode args, ToolContext ctx) throws ToolException {
        String op = args.path("operation").asText("");
        try {
            Session session = MgnlContext.getJCRSession("website");
            if ("searchEditorial".equals(op)) {
                return searchEditorial(session, args);
            }
            throw new ToolException("unknown operation: " + op);
        } catch (ToolException te) {
            throw te;
        } catch (Exception e) {
            throw new ToolException("editorial tool failed: " + e.getMessage(), e);
        }
    }

    private JsonNode searchEditorial(Session session, JsonNode args) throws Exception {
        String query = args.path("query").asText(null);
        String lower = query != null ? query.toLowerCase() : null;

        List<ObjectNode> results = new ArrayList<>();
        collectMatchingPages(session.getRootNode(), lower, results);

        ObjectNode out = JSON.objectNode();
        ArrayNode arr = JSON.arrayNode();
        results.forEach(arr::add);
        out.set("results", arr);
        return out;
    }

    private void collectMatchingPages(Node node, String lower, List<ObjectNode> results) throws Exception {
        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            Node child = it.nextNode();
            String path = child.getPath();
            if (path.startsWith(DESTINATIONS_PATH)) {
                continue;
            }
            if (lower == null || matchesQuery(child, lower)) {
                results.add(summarizePage(child));
            }
            collectMatchingPages(child, lower, results);
        }
    }

    private boolean matchesQuery(Node node, String lower) throws Exception {
        String title = node.hasProperty("title") ? node.getProperty("title").getString().toLowerCase() : "";
        String text = node.hasProperty("text") ? node.getProperty("text").getString().toLowerCase() : "";
        String abstractText = node.hasProperty("abstract") ? node.getProperty("abstract").getString().toLowerCase() : "";
        return title.contains(lower) || text.contains(lower) || abstractText.contains(lower);
    }

    private ObjectNode summarizePage(Node node) throws Exception {
        String path = node.getPath();
        ObjectNode summary = JSON.objectNode();
        summary.put("id", node.getName());
        summary.put("path", path);
        summary.put("title", node.hasProperty("title") ? node.getProperty("title").getString() : node.getName());
        if (node.hasProperty("abstract")) {
            summary.put("abstract", node.getProperty("abstract").getString());
        }
        summary.put("url", path);
        return summary;
    }
}
