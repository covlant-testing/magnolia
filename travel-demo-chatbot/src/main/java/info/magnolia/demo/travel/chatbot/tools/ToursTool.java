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
public class ToursTool implements Tool {

    private static final int MAX_RESULTS = 10;
    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    @Override
    public String name() {
        return "tours";
    }

    @Override
    public String description() {
        return "Search and fetch tours. operation=searchTours with optional {region, maxPriceUsd, durationDaysMax, theme}; operation=getTour with required {id}.";
    }

    @Override
    public JsonNode parametersSchema() {
        ObjectNode schema = JSON.objectNode();
        schema.put("type", "object");
        ObjectNode props = JSON.objectNode();
        props.set("operation", JSON.objectNode().put("type", "string"));
        props.set("id", JSON.objectNode().put("type", "string"));
        props.set("region", JSON.objectNode().put("type", "string"));
        props.set("maxPriceUsd", JSON.objectNode().put("type", "number"));
        props.set("durationDaysMax", JSON.objectNode().put("type", "number"));
        props.set("theme", JSON.objectNode().put("type", "string"));
        schema.set("properties", props);
        ArrayNode required = JSON.arrayNode();
        required.add("operation");
        schema.set("required", required);
        return schema;
    }

    @Override
    public JsonNode invoke(JsonNode args, ToolContext ctx) throws ToolException {
        String op = args.path("operation").asText("searchTours");
        try {
            Session session = MgnlContext.getJCRSession("tours");
            switch (op) {
                case "searchTours":
                    return searchTours(session, args);
                case "getTour":
                    return getTour(session, args);
                default:
                    throw new ToolException("unknown operation: " + op);
            }
        } catch (ToolException te) {
            throw te;
        } catch (Exception e) {
            throw new ToolException("tours tool failed: " + e.getMessage(), e);
        }
    }

    private JsonNode searchTours(Session session, JsonNode args) throws Exception {
        String region = args.path("region").asText(null);
        long maxPrice = args.path("maxPriceUsd").isMissingNode() ? Long.MAX_VALUE : args.path("maxPriceUsd").asLong();
        long maxDays = args.path("durationDaysMax").isMissingNode() ? Long.MAX_VALUE : args.path("durationDaysMax").asLong();
        String theme = args.path("theme").asText(null);

        List<ObjectNode> results = new ArrayList<>();
        NodeIterator it = session.getRootNode().getNodes();
        while (it.hasNext() && results.size() < MAX_RESULTS) {
            Node node = it.nextNode();
            if (!matchesTour(node, region, maxPrice, maxDays, theme)) {
                continue;
            }
            results.add(summarizeTour(node));
        }

        ObjectNode out = JSON.objectNode();
        ArrayNode arr = JSON.arrayNode();
        results.forEach(arr::add);
        out.set("results", arr);
        return out;
    }

    private boolean matchesTour(Node node, String region, long maxPrice, long maxDays, String theme) throws Exception {
        if (region != null) {
            String nodeRegion = node.hasProperty("region") ? node.getProperty("region").getString() : "";
            if (!region.equalsIgnoreCase(nodeRegion)) {
                return false;
            }
        }
        if (maxPrice < Long.MAX_VALUE && node.hasProperty("priceUsd")) {
            if (node.getProperty("priceUsd").getLong() > maxPrice) {
                return false;
            }
        }
        if (maxDays < Long.MAX_VALUE && node.hasProperty("durationDays")) {
            if (node.getProperty("durationDays").getLong() > maxDays) {
                return false;
            }
        }
        if (theme != null) {
            String nodeTheme = node.hasProperty("theme") ? node.getProperty("theme").getString() : "";
            if (!theme.equalsIgnoreCase(nodeTheme)) {
                return false;
            }
        }
        return true;
    }

    private JsonNode getTour(Session session, JsonNode args) throws ToolException, Exception {
        String id = args.path("id").asText(null);
        if (id == null || id.isEmpty()) {
            throw new ToolException("getTour requires 'id'");
        }
        if (!session.getRootNode().hasNode(id)) {
            throw new ToolException("tour not found: " + id);
        }
        Node node = session.getRootNode().getNode(id);
        return detailTour(node);
    }

    private ObjectNode summarizeTour(Node node) throws Exception {
        String id = node.getName();
        ObjectNode summary = JSON.objectNode();
        summary.put("id", id);
        summary.put("title", node.hasProperty("title") ? node.getProperty("title").getString() : id);
        summary.put("region", node.hasProperty("region") ? node.getProperty("region").getString() : "");
        summary.put("priceUsd", node.hasProperty("priceUsd") ? node.getProperty("priceUsd").getLong() : 0);
        summary.put("durationDays", node.hasProperty("durationDays") ? node.getProperty("durationDays").getLong() : 0);
        summary.put("url", "/travel/tour/" + id);
        return summary;
    }

    private ObjectNode detailTour(Node node) throws Exception {
        ObjectNode detail = summarizeTour(node);
        if (node.hasProperty("description")) {
            detail.put("description", node.getProperty("description").getString());
        }
        if (node.hasProperty("theme")) {
            detail.put("theme", node.getProperty("theme").getString());
        }
        return detail;
    }
}
