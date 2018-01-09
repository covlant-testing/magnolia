/**
 * This file Copyright (c) 2015-2018 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.demo.travel.personalization.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Adds a mixin to a node.
 */
public class AddMixinTask extends AbstractRepositoryTask {

    private final String nodePath;
    private final String workspace;
    private final String newMixin;

    /**
     * @param nodePath the path to the node
     * @param workspace the workspace where the node is stored
     * @param newMixin the full name of the mixin to add
     */
    public AddMixinTask(String nodePath, String workspace, String newMixin) {
        super("Add mixin to " + nodePath, "Add " + newMixin + " mixin to " + nodePath);
        this.nodePath = nodePath;
        this.workspace = workspace;
        this.newMixin = newMixin;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Session session = ctx.getJCRSession(workspace);
        try {
            final Node node = session.getNode(nodePath);
            node.addMixin(newMixin);
        } catch (RepositoryException e) {
            ctx.error(String.format("Could not add new mixin %s to %s", newMixin, nodePath), e);
        }
    }
}
