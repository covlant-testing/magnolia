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

import java.util.List;

import org.junit.Test;

public class ToolRegistryTest {

    private static Tool toolNamed(String name) {
        return new Tool() {
            @Override public String name() { return name; }
            @Override public String description() { return "desc"; }
            @Override public JsonNode parametersSchema() { return JsonNodeFactory.instance.objectNode(); }
            @Override public JsonNode invoke(JsonNode args, ToolContext ctx) { return JsonNodeFactory.instance.objectNode(); }
        };
    }

    @Test
    public void enabledNamesFilterRegistry() {
        ToolRegistry reg = new ToolRegistry(List.of(toolNamed("a"), toolNamed("b"), toolNamed("c")));
        List<Tool> enabled = reg.enabled(List.of("a", "c"));
        assertEquals(List.of("a", "c"), enabled.stream().map(Tool::name).toList());
    }

    @Test
    public void invokeUnknownThrows() {
        ToolRegistry reg = new ToolRegistry(List.of(toolNamed("a")));
        assertThrows(ToolException.class,
                () -> reg.invoke("missing", JsonNodeFactory.instance.objectNode(), new ToolContext("en")));
    }

    @Test
    public void invokeKnownReturnsResult() throws Exception {
        ToolRegistry reg = new ToolRegistry(List.of(toolNamed("a")));
        JsonNode out = reg.invoke("a", JsonNodeFactory.instance.objectNode(), new ToolContext("en"));
        assertTrue(out.isObject());
    }
}
