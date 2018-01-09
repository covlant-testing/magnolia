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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link RemoveTravelDemoSiteFromMultiSite}.
 */
public class RemoveTravelDemoSiteFromMultiSiteTest {

    private RemoveTravelDemoSiteFromMultiSite task = new RemoveTravelDemoSiteFromMultiSite();
    private InstallContext context;
    private MockSession session;
    private Node siteNode;

    @Before
    public void setUp() throws Exception {
        session = new MockSession(RepositoryConstants.CONFIG);

        context = mock(InstallContext.class);
        when(context.getJCRSession(RepositoryConstants.CONFIG)).thenReturn(session);
        when(context.getConfigJCRSession()).thenReturn(session);

        siteNode = NodeUtil.createPath(session.getRootNode(), RemoveTravelDemoSiteFromMultiSite.PATH_TO_DEFAULT_SITE, NodeTypes.ContentNode.NAME);
    }

    @Test
    public void checkThatTaskRemovesCorrectSiteDefinition() throws Exception {
        // GIVEN
        final Node themeReferenceNode = siteNode.addNode("theme", NodeTypes.ContentNode.NAME);
        themeReferenceNode.setProperty("name", "travel-demo-theme");
        final Node homeTemplate = NodeUtil.createPath(siteNode, "templates/availability/templates/home", NodeTypes.ContentNode.NAME);
        homeTemplate.setProperty("id", "travel-demo:pages/home");

        // WHEN
        task.doExecute(context);

        // THEN
        assertFalse(session.itemExists(RemoveTravelDemoSiteFromMultiSite.PATH_TO_DEFAULT_SITE));
    }

    @Test
    public void checkThatTaskDoesNotRemoveWrongSiteDefinitionWhenClassPropertyIsThere() throws Exception {
        // GIVEN
        siteNode.setProperty("class", "a.fully.qualified.class.Name");

        // WHEN
        task.doExecute(context);

        // THEN
        assertTrue(session.itemExists(RemoveTravelDemoSiteFromMultiSite.PATH_TO_DEFAULT_SITE));
    }

    @Test
    public void checkThatTaskDoesNotRemoveWrongSiteDefinition() throws Exception {
        // GIVEN
        final Node themeReferenceNode = siteNode.addNode("theme", NodeTypes.ContentNode.NAME);
        themeReferenceNode.setProperty("name", "another-theme-name");

        // WHEN
        task.doExecute(context);

        // THEN
        assertTrue(session.itemExists(RemoveTravelDemoSiteFromMultiSite.PATH_TO_DEFAULT_SITE));
    }

}