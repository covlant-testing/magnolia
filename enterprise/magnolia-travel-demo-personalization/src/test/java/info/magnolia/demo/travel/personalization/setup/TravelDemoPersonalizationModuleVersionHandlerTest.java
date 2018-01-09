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

import static info.magnolia.demo.travel.personalization.setup.RemoveMixinTaskTest.hasMixin;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeTypes.Activatable;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.objectfactory.Components;
import info.magnolia.personalization.variant.VariantManager;
import info.magnolia.repository.RepositoryConstants;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * Tests for {@link TravelDemoPersonalizationModuleVersionHandler}.
 */
public class TravelDemoPersonalizationModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private final String travelDemoAdminCentralRoleName = "travel-demo-admincentral";

    private Session userRolesSession;
    private Session websiteSession;
    private Node travelDemoAdminCentralRoleNode;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/travel-demo-personalization.xml";
    }

    @Override
    protected String[] getExtraWorkspaces() {
        return new String[]{RepositoryConstants.WEBSITE};
    }

    @Override
    protected String getExtraNodeTypes() {
        return "/mgnl-nodetypes/magnolia-personalization-nodetypes.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList("/META-INF/magnolia/core.xml");
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new TravelDemoPersonalizationModuleVersionHandler();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        addSupportForSetupModuleRepositoriesTask(null);

        // Override role manager from info.magnolia.module.ModuleVersionHandlerTestCase.addSupportForSetupModuleRepositoriesTask()
        // which is simply a mock
        final RoleManager roleManager = new MgnlRoleManager();
        ((SecuritySupportImpl) Components.getComponent(SecuritySupport.class)).setRoleManager(roleManager);

        roleManager.createRole("superuser");
        roleManager.createRole(this.travelDemoAdminCentralRoleName);

        this.userRolesSession = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES);
        this.websiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        this.travelDemoAdminCentralRoleNode = userRolesSession.getNode("/" + this.travelDemoAdminCentralRoleName);
    }

    @Test
    public void updateFrom07AddsNewPermissionsForTravelDemoAdminCentral() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.7"));

        // THEN
        assertThat("We expect that " + this.travelDemoAdminCentralRoleName + " has personas ACLs", this.travelDemoAdminCentralRoleNode.hasNode("acl_personas"), is(true));
    }

    @Test
    public void cleanInstallAddsNewPermissionsForTravelDemoAdminCentral() throws Exception {
        // GIVEN
        setupNode(RepositoryConstants.WEBSITE, "/travel/contact/variants");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        assertThat("We expect that " + this.travelDemoAdminCentralRoleName + " has personas ACLs", this.travelDemoAdminCentralRoleNode.hasNode("acl_personas"), is(true));
    }

    @Test
    public void updateFrom013AddsVariantMixinToTravelContactPage() throws Exception {
        // GIVEN
        NodeUtil.createPath(websiteSession.getRootNode(), "travel/contact/variants", NodeTypes.Page.NAME); // can't use mgnl:variants here as extra node types haven't been registered yet. See MAGNOLIA-6423

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.13"));

        // THEN
        boolean hasMixin = hasMixin(websiteSession.getNode("/travel/contact"), VariantManager.HAS_VARIANT_MIXIN);
        assertThat("We expect that /travel/contact node has mgnl:hasVariants mixin", hasMixin, is(true));
    }


    @Test
    public void updateFrom013SetsPagesAsPublished() throws Exception {
        // GIVEN
        Node variants = NodeUtil.createPath(websiteSession.getRootNode(), "travel/contact/variants", NodeTypes.Page.NAME); // can't use mgnl:variants here as extra node types haven't been registered yet. See MAGNOLIA-6423
        PropertyUtil.setProperty(websiteSession.getNode("/travel"), Activatable.ACTIVATION_STATUS, Long.valueOf(Activatable.ACTIVATION_STATUS_MODIFIED));
        PropertyUtil.setProperty(websiteSession.getNode("/travel/contact"), Activatable.ACTIVATION_STATUS, Long.valueOf(Activatable.ACTIVATION_STATUS_MODIFIED));
        PropertyUtil.setProperty(variants, Activatable.ACTIVATION_STATUS, Long.valueOf(Activatable.ACTIVATION_STATUS_MODIFIED));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.13"));

        // THEN
        int activationStatus = Activatable.getActivationStatus(websiteSession.getNode("/travel"));
        assertThat("We expect that /travel node is activated", activationStatus, equalTo(Activatable.ACTIVATION_STATUS_ACTIVATED));

        activationStatus = Activatable.getActivationStatus(websiteSession.getNode("/travel/contact/variants"));
        assertThat("We expect that /travel/variants node is activated", activationStatus, equalTo(Activatable.ACTIVATION_STATUS_ACTIVATED));
    }

    @Test
    public void cleanInstallSetsPagesAsPublished() throws Exception {
        // GIVEN
        Node variants = NodeUtil.createPath(websiteSession.getRootNode(), "travel/contact/variants", NodeTypes.Page.NAME); // can't use mgnl:variants here as extra node types haven't been registered yet. See MAGNOLIA-6423
        PropertyUtil.setProperty(websiteSession.getNode("/travel"), Activatable.ACTIVATION_STATUS, Long.valueOf(Activatable.ACTIVATION_STATUS_MODIFIED));
        PropertyUtil.setProperty(websiteSession.getNode("/travel/contact"), Activatable.ACTIVATION_STATUS, Long.valueOf(Activatable.ACTIVATION_STATUS_MODIFIED));
        PropertyUtil.setProperty(variants, Activatable.ACTIVATION_STATUS, Long.valueOf(Activatable.ACTIVATION_STATUS_MODIFIED));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        int activationStatus = Activatable.getActivationStatus(websiteSession.getNode("/travel"));
        assertThat("We expect that /travel node is activated", activationStatus, equalTo(Activatable.ACTIVATION_STATUS_ACTIVATED));

        activationStatus = Activatable.getActivationStatus(websiteSession.getNode("/travel/contact/variants"));
        assertThat("We expect that /travel/variants node is activated", activationStatus, equalTo(Activatable.ACTIVATION_STATUS_ACTIVATED));
    }

    @Test
    public void updateFrom013RemovesTravelVariantsAndInstallsContactVariants() throws Exception {
        // GIVEN
        websiteSession.getRootNode().addNode("travel", NodeTypes.Page.NAME);
        websiteSession.getRootNode().addNode("travel/variants", NodeTypes.Page.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.13"));

        // THEN
        Node travelPages = websiteSession.getNode("/travel");
        List<Node> pageNames = Lists.newArrayList(NodeUtil.getNodes(travelPages));
        assertThat(Collections2.transform(pageNames, new ToNodeName()), not(contains("variants")));

        travelPages = websiteSession.getNode("/travel/contact");
        pageNames = Lists.newArrayList(NodeUtil.getNodes(travelPages));
        assertThat(Collections2.transform(pageNames, new ToNodeName()), contains("variants"));
    }

    /**
     * This function is used to extract node name of a given node.
     */
    private static class ToNodeName implements Function<Node, String> {
        @Override
        public String apply(Node node) {
            try {
                return node.getName();
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
