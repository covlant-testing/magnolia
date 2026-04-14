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

import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

@Singleton
public class DestinationsTool implements Tool {

    private static final String DESTINATIONS_PATH = "/travel/destinations";
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    @Override
    public String name() {
        return "destinations";
    }

    @Override
    public String description() {
        return "Search and fetch travel destinations. operation=searchDestinations with optional {query, region, climate}; operation=getDestination with required {id}.";
    }

    @Override
    public JsonNode parametersSchema() {
        ObjectNode schema = JSON.objectNode();
        schema.put("type", "object");
        ObjectNode props = JSON.objectNode();
        props.set("operation", JSON.objectNode().put("type", "string"));
        props.set("id", JSON.objectNode().put("type", "string"));
        props.set("query", JSON.objectNode().put("type", "string"));
        props.set("region", JSON.objectNode().put("type", "string"));
        props.set("climate", JSON.objectNode().put("type", "string"));
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
            switch (op) {
                case "searchDestinations":
                    return searchDestinations(session, args);
                case "getDestination":
                    return getDestination(session, args);
                default:
                    throw new ToolException("unknown operation: " + op);
            }
        } catch (ToolException te) {
            throw te;
        } catch (Exception e) {
            throw new ToolException("destinations tool failed: " + e.getMessage(), e);
        }
    }

    private JsonNode searchDestinations(Session session, JsonNode args) throws Exception {
        String query = args.path("query").asText(null);
        String region = args.path("region").asText(null);
        String climate = args.path("climate").asText(null);

        ArrayNode results = JSON.arrayNode();
        if (!session.nodeExists(DESTINATIONS_PATH)) {
            ObjectNode out = JSON.objectNode();
            out.set("results", results);
            return out;
        }

        Node destinationsNode = session.getNode(DESTINATIONS_PATH);
        NodeIterator it = destinationsNode.getNodes();
        while (it.hasNext()) {
            Node node = it.nextNode();
            if (matchesDestination(node, query, region, climate)) {
                results.add(summarizeDestination(node));
            }
        }

        ObjectNode out = JSON.objectNode();
        out.set("results", results);
        return out;
    }

    private boolean matchesDestination(Node node, String query, String region, String climate) throws Exception {
        if (region != null) {
            String nodeRegion = node.hasProperty("region") ? node.getProperty("region").getString() : "";
            if (!region.equalsIgnoreCase(nodeRegion)) {
                return false;
            }
        }
        if (climate != null) {
            String nodeClimate = node.hasProperty("climate") ? node.getProperty("climate").getString() : "";
            if (!climate.equalsIgnoreCase(nodeClimate)) {
                return false;
            }
        }
        if (query != null) {
            String lower = query.toLowerCase();
            String title = node.hasProperty("title") ? node.getProperty("title").getString().toLowerCase() : "";
            String description = node.hasProperty("description") ? node.getProperty("description").getString().toLowerCase() : "";
            if (!title.contains(lower) && !description.contains(lower)) {
                return false;
            }
        }
        return true;
    }

    private JsonNode getDestination(Session session, JsonNode args) throws ToolException, Exception {
        String id = args.path("id").asText(null);
        if (id == null || id.isEmpty()) {
            throw new ToolException("getDestination requires 'id'");
        }
        String path = DESTINATIONS_PATH + "/" + id;
        if (!session.nodeExists(path)) {
            throw new ToolException("destination not found: " + id);
        }
        return detailDestination(session.getNode(path));
    }

    private ObjectNode summarizeDestination(Node node) throws Exception {
        String id = node.getName();
        ObjectNode summary = JSON.objectNode();
        summary.put("id", id);
        summary.put("title", node.hasProperty("title") ? node.getProperty("title").getString() : id);
        summary.put("region", node.hasProperty("region") ? node.getProperty("region").getString() : "");
        summary.put("climate", node.hasProperty("climate") ? node.getProperty("climate").getString() : "");
        summary.put("url", "/travel/destinations/" + id);
        return summary;
    }

    private ObjectNode detailDestination(Node node) throws Exception {
        ObjectNode detail = summarizeDestination(node);
        if (node.hasProperty("description")) {
            detail.put("description", node.getProperty("description").getString());
        }
        return detail;
    }
}
