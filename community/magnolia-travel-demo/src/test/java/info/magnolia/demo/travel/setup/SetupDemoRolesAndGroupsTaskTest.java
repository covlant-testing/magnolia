/**
 * This file Copyright (c) 2015-2024 Magnolia International
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

import static info.magnolia.demo.travel.setup.SetupDemoRolesAndGroupsTask.*;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.jcr.MockSession;

import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

public class SetupDemoRolesAndGroupsTaskTest {

    private Session session;
    private InstallContext ctx;

    @Before
    public void setUp() throws Exception {
        session = new MockSession(RepositoryConstants.CONFIG);
        ctx = mock(InstallContext.class);
        when(ctx.getJCRSession(eq(RepositoryConstants.CONFIG))).thenReturn(session);

        NodeUtil.createPath(session.getRootNode(), "modules/pages", NodeTypes.Content.NAME);
        NodeUtil.createPath(session.getRootNode(), "modules/dam-app", NodeTypes.Content.NAME);
    }

    @Test
    public void demoPublisherCanActivatePagesOnCEInstance() throws Exception {
        // GIVEN
        NodeUtil.createPath(session.getRootNode(), PAGES_ACTIVATE_ACCESS_ROLES, NodeTypes.ContentNode.NAME);

        // WHEN
        new SetupDemoRolesAndGroupsTask().execute(ctx);

        // THEN
        assertThat(session.getNode(PAGES_ACTIVATE_ACCESS_ROLES), not(hasProperty(TRAVEL_DEMO_EDITOR_ROLE, TRAVEL_DEMO_EDITOR_ROLE)));
        assertThat(session.getNode(PAGES_ACTIVATE_ACCESS_ROLES), hasProperty(TRAVEL_DEMO_PUBLISHER_ROLE, TRAVEL_DEMO_PUBLISHER_ROLE));
    }

    @Test
    public void demoEditorCanActivatePagesOnEEInstance() throws Exception {
        // GIVEN
        NodeUtil.createPath(session.getRootNode(), PAGES_ACTIVATE_ACCESS_ROLES, NodeTypes.ContentNode.NAME);
        when(ctx.isModuleRegistered(eq(ENTERPRISE_MODULE))).thenReturn(true);

        // WHEN
        new SetupDemoRolesAndGroupsTask().execute(ctx);

        // THEN
        assertThat(session.getNode(PAGES_ACTIVATE_ACCESS_ROLES), hasProperty(TRAVEL_DEMO_EDITOR_ROLE, TRAVEL_DEMO_EDITOR_ROLE));
        assertThat(session.getNode(PAGES_ACTIVATE_ACCESS_ROLES), hasProperty(TRAVEL_DEMO_PUBLISHER_ROLE, TRAVEL_DEMO_PUBLISHER_ROLE));
    }

    @Test
    public void demoRolesCanActivateAssets() throws Exception {
        // GIVEN
        NodeUtil.createPath(session.getRootNode(), DAM_ACTIVATE_ACCESS_ROLES, NodeTypes.ContentNode.NAME);

        // WHEN
        new SetupDemoRolesAndGroupsTask().execute(ctx);

        // THEN
        assertThat(session.getNode(DAM_ACTIVATE_ACCESS_ROLES), hasProperty(TRAVEL_DEMO_EDITOR_ROLE, TRAVEL_DEMO_EDITOR_ROLE));
        assertThat(session.getNode(DAM_ACTIVATE_ACCESS_ROLES), hasProperty(TRAVEL_DEMO_PUBLISHER_ROLE, TRAVEL_DEMO_PUBLISHER_ROLE));
    }

    @Test
    public void demoRolesCanAccessDamApp() throws Exception {
        // GIVEN
        NodeUtil.createPath(session.getRootNode(), DAM_PERMISSIONS_ROLES, NodeTypes.ContentNode.NAME);

        // WHEN
        new SetupDemoRolesAndGroupsTask().execute(ctx);

        // THEN
        assertThat(session.getNode(DAM_PERMISSIONS_ROLES), hasProperty(TRAVEL_DEMO_EDITOR_ROLE, TRAVEL_DEMO_EDITOR_ROLE));
        assertThat(session.getNode(DAM_PERMISSIONS_ROLES), hasProperty(TRAVEL_DEMO_PUBLISHER_ROLE, TRAVEL_DEMO_PUBLISHER_ROLE));
    }

    @Test
    public void demoRolesCanAccessPagesApp() throws Exception {
        // GIVEN
        NodeUtil.createPath(session.getRootNode(), PAGES_PERMISSIONS_ROLES, NodeTypes.ContentNode.NAME);

        // WHEN
        new SetupDemoRolesAndGroupsTask().execute(ctx);

        // THEN
        assertThat(session.getNode(PAGES_PERMISSIONS_ROLES), hasProperty(TRAVEL_DEMO_EDITOR_ROLE, TRAVEL_DEMO_EDITOR_ROLE));
        assertThat(session.getNode(PAGES_PERMISSIONS_ROLES), hasProperty(TRAVEL_DEMO_PUBLISHER_ROLE, TRAVEL_DEMO_PUBLISHER_ROLE));
    }

    @Test
    public void installTaskCreatesPathToAccessRolesIfNotExisting() throws Exception {
        // GIVEN

        // WHEN
        new SetupDemoRolesAndGroupsTask().execute(ctx);

        // THEN
        assertThat(session.nodeExists(PAGES_ACTIVATE_ACCESS_ROLES), is(true));
        assertThat(session.nodeExists(DAM_ACTIVATE_ACCESS_ROLES), is(true));
    }
}