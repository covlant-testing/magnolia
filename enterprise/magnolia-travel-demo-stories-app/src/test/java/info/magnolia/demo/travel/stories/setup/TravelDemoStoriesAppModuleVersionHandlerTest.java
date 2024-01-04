/**
 * This file Copyright (c) 2017-2024 Magnolia International
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
package info.magnolia.demo.travel.stories.setup;

import static info.magnolia.test.hamcrest.NodeMatchers.hasNode;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.junit.Assert.assertThat;

import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.dam.jcr.DamConstants;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Test;

/**
 * Tests for {@link TravelDemoStoriesAppModuleVersionHandler}.
 */
public class TravelDemoStoriesAppModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private final String TRAVEL_DEMO_PUBLISHER = "travel-demo-publisher";
    private final String TRAVEL_DEMO_EDITOR = "travel-demo-editor";
    private final String TRAVEL_DEMO_BASE = "travel-demo-base";

    private Session userRolesSession;
    private Node travelDemoPublisherNode;
    private Node travelDemoEditorNode;
    private Node travelDemoBaseNode;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/travel-demo-stories-app.xml";
    }

    @Override
    protected String getExtraNodeTypes() {
        return "/mgnl-nodetypes/magnolia-dam-nodetypes.cnd";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList("/META-INF/magnolia/core.xml");
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new TravelDemoStoriesAppModuleVersionHandler();
    }

    @Override
    protected String[] getExtraWorkspaces() {
        return new String[] { DamConstants.WORKSPACE };
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        RoleManager roleManager = Components.getComponent(MgnlRoleManager.class);
        SecuritySupportImpl security = new SecuritySupportImpl();
        security.setRoleManager(roleManager);

        ComponentsTestUtil.setInstance(SecuritySupport.class, security);

        roleManager.createRole(TRAVEL_DEMO_PUBLISHER);
        roleManager.createRole(TRAVEL_DEMO_EDITOR);
        roleManager.createRole(TRAVEL_DEMO_BASE);

        this.userRolesSession = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);

        this.travelDemoPublisherNode = userRolesSession.getNode("/" + TRAVEL_DEMO_PUBLISHER);
        this.travelDemoEditorNode = userRolesSession.getNode("/" + TRAVEL_DEMO_EDITOR);
        this.travelDemoBaseNode = userRolesSession.getNode("/" + TRAVEL_DEMO_BASE);
    }

    @Test
    public void updateFrom12() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("1.2"));

        // THEN
        assertThat("We expect that " + TRAVEL_DEMO_PUBLISHER + " has R/W permissions to stories", this.travelDemoPublisherNode.getNode("acl_stories/0"), hasProperty("permissions", Permission.ALL));
        assertThat("We expect that " + TRAVEL_DEMO_EDITOR + " has R/W permissions to stories", this.travelDemoEditorNode.getNode("acl_stories/0"), hasProperty("permissions", Permission.ALL));
        assertThat("We expect that " + TRAVEL_DEMO_BASE + " doesn't have any permissions to stories", this.travelDemoBaseNode.getNode("acl_stories/0"), hasProperty("permissions", Permission.NONE));
    }

    @Test
    public void updateFrom122() throws Exception {
        // GIVEN
        NodeUtil.createPath(MgnlContext.getJCRSession(DamConstants.WORKSPACE).getRootNode(),"stories-demo", NodeTypes.Folder.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("1.2.2"));

        // THEN
        Node storiesDemo = NodeUtil.createPath(MgnlContext.getJCRSession(DamConstants.WORKSPACE).getRootNode(),"stories-demo", NodeTypes.Folder.NAME);
        assertThat(storiesDemo, hasNode("flying-grand-canyon/video-thumbnail-grand-canyon"));
    }
}
