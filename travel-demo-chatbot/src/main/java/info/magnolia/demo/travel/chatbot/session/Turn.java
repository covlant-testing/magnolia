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
package info.magnolia.demo.travel.chatbot.session;

public final class Turn {

    public enum Role { USER, ASSISTANT, TOOL }

    private final Role role;
    private final String content;
    private final String toolName;

    private Turn(Role r, String c, String toolName) {
        this.role = r;
        this.content = c;
        this.toolName = toolName;
    }

    public static Turn user(String c) {
        return new Turn(Role.USER, c, null);
    }

    public static Turn assistant(String c) {
        return new Turn(Role.ASSISTANT, c, null);
    }

    public static Turn tool(String toolName, String result) {
        return new Turn(Role.TOOL, result, toolName);
    }

    public Role role() {
        return role;
    }

    public String content() {
        return content;
    }

    public String toolName() {
        return toolName;
    }
}
