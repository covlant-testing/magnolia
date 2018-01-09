/**
 * This file Copyright (c) 2016-2018 Magnolia International
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
import javax.jcr.nodetype.NodeType;

/**
 * Removes a mixin from a node.
 */
public class RemoveMixinTask extends AbstractRepositoryTask {

    private final String nodePath;
    private final String workspace;
    private final String mixin;

    /**
     * @param nodePath the path to the node
     * @param workspace the workspace where the node is stored
     * @param mixin the full name of the mixin to remove
     */
    public RemoveMixinTask(String nodePath, String workspace, String mixin) {
        super("Remove mixin from " + nodePath, "Removes " + mixin + " mixin from " + nodePath);
        this.nodePath = nodePath;
        this.workspace = workspace;
        this.mixin = mixin;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Session session = ctx.getJCRSession(workspace);
        try {
            final Node node = session.getNode(nodePath);
            for (NodeType nt : node.getMixinNodeTypes()) {
                if (mixin.equals(nt.getName())) {
                    node.removeMixin(mixin);
                }
            }

        } catch (RepositoryException e) {
            ctx.error(String.format("Could not remove mixin %s from %s", mixin, nodePath), e);
        }
    }
}
