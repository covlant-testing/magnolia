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

public class ToursToolTest {

    private MockSession session;

    @Before
    public void setUp() throws Exception {
        MockUtil.initMockContext();
        session = new MockSession("tours");
        MockUtil.getMockContext().addSession("tours", session);
    }

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    private void seedTour(String name, String region, long price, long days) throws Exception {
        Node n = session.getRootNode().addNode(name, "mgnl:content");
        n.setProperty("name", name);
        n.setProperty("region", region);
        n.setProperty("priceUsd", price);
        n.setProperty("durationDays", days);
    }

    @Test
    public void searchToursFiltersByRegion() throws Exception {
        seedTour("bali-7day", "Asia", 1500L, 7L);
        seedTour("alps-trek", "Europe", 2500L, 5L);
        ToursTool tool = new ToursTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "searchTours").put("region", "Asia");
        JsonNode out = tool.invoke(args, new ToolContext("en"));
        assertEquals(1, out.get("results").size());
        assertEquals("bali-7day", out.get("results").get(0).get("id").asText());
    }

    @Test
    public void searchToursFiltersByMaxPrice() throws Exception {
        seedTour("cheap", "Asia", 500L, 3L);
        seedTour("expensive", "Asia", 3000L, 7L);
        ToursTool tool = new ToursTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "searchTours").put("maxPriceUsd", 1000);
        JsonNode out = tool.invoke(args, new ToolContext("en"));
        assertEquals(1, out.get("results").size());
        assertEquals("cheap", out.get("results").get(0).get("id").asText());
    }

    @Test
    public void searchToursCapsAtTen() throws Exception {
        for (int i = 0; i < 15; i++) seedTour("t" + i, "Asia", 100, 1);
        ToursTool tool = new ToursTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode().put("operation", "searchTours");
        assertEquals(10, tool.invoke(args, new ToolContext("en")).get("results").size());
    }

    @Test
    public void getTourReturnsDetails() throws Exception {
        seedTour("bali-7day", "Asia", 1500L, 7L);
        ToursTool tool = new ToursTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode()
                .put("operation", "getTour").put("id", "bali-7day");
        JsonNode out = tool.invoke(args, new ToolContext("en"));
        assertEquals("bali-7day", out.get("id").asText());
        assertEquals("Asia", out.get("region").asText());
    }

    @Test
    public void unknownOperationThrows() {
        ToursTool tool = new ToursTool();
        ObjectNode args = JsonNodeFactory.instance.objectNode().put("operation", "nope");
        assertThrows(ToolException.class, () -> tool.invoke(args, new ToolContext("en")));
    }

    @Test
    public void schemaIsObject() {
        assertTrue(new ToursTool().parametersSchema().isObject());
    }
}
