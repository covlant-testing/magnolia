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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DestinationsToolTest {

    private MockSession session;
    private Node destinationsNode;

    @Before
    public void setUp() throws Exception {
        MockUtil.initMockContext();
        session = new MockSession("website");
        MockUtil.getMockContext().addSession("website", session);
        Node travel = session.getRootNode().addNode("travel", "mgnl:page");
        destinationsNode = travel.addNode("destinations", "mgnl:page");
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    private Node seedDestination(String name, String region, String climate) throws Exception {
        Node node = destinationsNode.addNode(name, "mgnl:page");
        node.setProperty("title", Character.toUpperCase(name.charAt(0)) + name.substring(1));
        node.setProperty("region", region);
        node.setProperty("climate", climate);
        return node;
    }

    @Test
    public void searchDestinationsFiltersByClimate() throws Exception {
        seedDestination("bali", "Asia", "tropical");
        seedDestination("paris", "Europe", "temperate");
        DestinationsTool tool = new DestinationsTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "searchDestinations").put("climate", "tropical");
        JsonNode out = tool.invoke(args, new ToolContext("en"));
        assertEquals(1, out.get("results").size());
        assertEquals("bali", out.get("results").get(0).get("id").asText());
    }

    @Test
    public void searchDestinationsFiltersByRegion() throws Exception {
        seedDestination("bali", "Asia", "tropical");
        seedDestination("tokyo", "Asia", "temperate");
        seedDestination("paris", "Europe", "temperate");
        DestinationsTool tool = new DestinationsTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "searchDestinations").put("region", "Asia");
        JsonNode out = tool.invoke(args, new ToolContext("en"));
        assertEquals(2, out.get("results").size());
    }

    @Test
    public void searchDestinationsFiltersByQuery() throws Exception {
        Node bali = seedDestination("bali", "Asia", "tropical");
        bali.setProperty("description", "Beautiful island paradise");
        seedDestination("paris", "Europe", "temperate");
        DestinationsTool tool = new DestinationsTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "searchDestinations").put("query", "island");
        JsonNode out = tool.invoke(args, new ToolContext("en"));
        assertEquals(1, out.get("results").size());
        assertEquals("bali", out.get("results").get(0).get("id").asText());
    }

    @Test
    public void getDestinationReturnsDetails() throws Exception {
        seedDestination("bali", "Asia", "tropical");
        DestinationsTool tool = new DestinationsTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "getDestination").put("id", "bali");
        JsonNode out = tool.invoke(args, new ToolContext("en"));
        assertEquals("bali", out.get("id").asText());
        assertEquals("Asia", out.get("region").asText());
        assertEquals("tropical", out.get("climate").asText());
    }

    @Test
    public void unknownOperationThrows() {
        DestinationsTool tool = new DestinationsTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode().put("operation", "nope");
        assertThrows(ToolException.class, () -> tool.invoke(args, new ToolContext("en")));
    }

    @Test
    public void schemaIsObject() {
        assertTrue(new DestinationsTool().parametersSchema().isObject());
    }
}
