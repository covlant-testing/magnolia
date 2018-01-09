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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.junit.Before;
import org.junit.Test;

public class RemoveMixinTaskTest {

    private RemoveMixinTask task;
    private Session session;
    private InstallContext installContext;
    private Node fooNode;

    @Before
    public void setUp() throws Exception {
        session = SessionTestUtil.createSession("config",
                "/foo"
        );
        fooNode = session.getNode("/foo");
        fooNode.addMixin("barMixin");
        installContext = mock(InstallContext.class);
        when(installContext.getConfigJCRSession()).thenReturn(session);
        when(installContext.getJCRSession(RepositoryConstants.CONFIG)).thenReturn(session);
        task = new RemoveMixinTask("/foo", RepositoryConstants.CONFIG, "barMixin");
    }


    @Test
    public void name() throws Exception {
        // WHEN
        task.execute(installContext);

        // THEN
        assertThat(hasMixin(fooNode, "barMixin"), is(false));
    }

    protected static boolean hasMixin(Node node, String mixin) throws RepositoryException {
        for (NodeType nt : node.getMixinNodeTypes()) {
            if (mixin.equals(nt.getName())) {
                return true;
            }
        }
        return false;
    }
}