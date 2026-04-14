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
package info.magnolia.demo.travel.chatbot.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Module-version handler for travel-demo-chatbot.
 * Grants the rest-anonymous role access to the chatbot REST endpoint on first install.
 */
public class ChatbotModuleVersionHandler extends DefaultModuleVersionHandler {

    @Override
    protected List<Task> getStartupTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<>(super.getStartupTasks(installContext));
        tasks.add(new AbstractRepositoryTask("Grant chatbot REST access to anonymous",
                "Adds /.rest/chatbot* URI permission to rest-anonymous role") {
            @Override
            protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
                Session session = ctx.getJCRSession("userroles");
                if (!session.nodeExists("/rest-anonymous")) {
                    return;
                }
                Node role = session.getNode("/rest-anonymous");
                Node aclUri = role.hasNode("acl_uri")
                        ? role.getNode("acl_uri")
                        : role.addNode("acl_uri", "mgnl:contentNode");
                if (!aclUri.hasNode("chatbot-access")) {
                    Node entry = aclUri.addNode("chatbot-access", "mgnl:contentNode");
                    entry.setProperty("path", "/.rest/chatbot*");
                    entry.setProperty("permissions", 63L);
                }
                session.save();
            }
        });
        return tasks;
    }
}
