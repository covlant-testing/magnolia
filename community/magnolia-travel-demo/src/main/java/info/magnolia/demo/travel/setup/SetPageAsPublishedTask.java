/**
 * This file Copyright (c) 2015-2018 Magnolia International
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
package info.magnolia.demo.travel.setup;

import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeTypes.Activatable;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmatically sets a page (and optionally its sub-pages) as published.
 */
public class SetPageAsPublishedTask extends AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(SetPageAsPublishedTask.class);

    private String absPath;
    private boolean recursive;

    public SetPageAsPublishedTask(String absPath, boolean recursive) {
        super("Set page as published", String.format("Sets page at [%s]%s as published.", absPath, recursive ? " and its sub-pages" : ""));

        this.absPath = absPath;
        this.recursive = recursive;
    }

    public SetPageAsPublishedTask(String absPath) {
        this(absPath, false);
    }

    @Override
    public void execute(InstallContext ctx) throws TaskExecutionException {
        try {
            List<Task> tasks = getTasks(ctx, absPath, recursive);
            for (Task task : tasks) {
                task.execute(ctx);
            }
        } catch (RepositoryException e) {
            throw new TaskExecutionException(String.format("Something went wrong while trying to execute task %s", getName()), e);
        }
    }

    private List<Task> getTasks(InstallContext ctx, String absPath, boolean recursive) throws RepositoryException {
        final List<Task> tasks = new ArrayList<>();
        final Calendar now = Calendar.getInstance();

        tasks.addAll(getTasksForPath(absPath, now));

        if (recursive) {
            final Session session = ctx.getJCRSession(RepositoryConstants.WEBSITE);
            final Iterable<Node> pages = NodeUtil.collectAllChildren(session.getNode(absPath), new AbstractPredicate<Node>() {
                @Override
                public boolean evaluateTyped(Node node) {
                    try {
                        final String nodeType = node.getPrimaryNodeType().getName();
                        return NodeTypes.Page.NAME.equals(nodeType) || "mgnl:variants".equals(nodeType);
                    } catch (RepositoryException e) {
                        log.warn("An error occurred while evaluating node {}. Will return false.", NodeUtil.getNodePathIfPossible(node), e);
                        return false;
                    }
                }
            });

            for (Node page : pages) {
                tasks.addAll(getTasksForPath(page.getPath(), now));
            }
        }

        return tasks;
    }

    private List<Task> getTasksForPath(final String path, final Calendar now) {
        final List<Task> tasks = new ArrayList<>();
        tasks.add(new SetPropertyTask("", RepositoryConstants.WEBSITE, path, Activatable.ACTIVATION_STATUS, true));
        tasks.add(new SetPropertyTask("", RepositoryConstants.WEBSITE, path, Activatable.LAST_ACTIVATED, now));
        tasks.add(new SetPropertyTask("", RepositoryConstants.WEBSITE, path, Activatable.LAST_ACTIVATED_BY, "superuser"));
        return tasks;
    }

}
