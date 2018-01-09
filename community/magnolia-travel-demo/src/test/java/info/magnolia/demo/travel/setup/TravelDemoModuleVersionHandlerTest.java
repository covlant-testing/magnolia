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

import static info.magnolia.demo.travel.setup.SetupDemoRolesAndGroupsTask.*;
import static info.magnolia.demo.travel.setup.SetupRoleBasedAccessPermissionsTask.*;
import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsIn.isIn;
import static org.junit.Assert.*;

import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.security.operations.VoterBasedConfiguredAccessDefinition;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.dam.jcr.DamConstants;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.voting.voters.RoleBaseVoter;

import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;


public class TravelDemoModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private static final String UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_TARGET_NODE_PATH = "/modules/ui-admincentral/config/appLauncherLayout/groups/target";
    private static final String PERMISSIONS_VOTERS_DENIED_ROLES_ROLES_NODE_PATH = VOTERS_DENIED_ROLES.concat("/roles");
    private static final String PERMISSIONS_VOTERS_ALLOWED_ROLES_ROLES_NODE_PATH = VOTERS_ALLOWED_ROLES.concat("/roles");
    private static final String CONTACTS_APPS_CONTACTS_NODE_PATH = "/modules/contacts/apps/contacts";
    private static final String UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_STK_NODE_PATH = "/modules/ui-admincentral/config/appLauncherLayout/groups/stk";
    private static final String UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_MANAGE_NODE_PATH = "/modules/ui-admincentral/config/appLauncherLayout/groups/manage";

    private Session session;
    private Session website;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/travel-demo.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new TravelDemoModuleVersionHandler();
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Collections.singletonList("/META-INF/magnolia/core.xml");
    }

    @Override
    protected String[] getExtraWorkspaces() {
        return new String[]{"dam"};
    }

    @Override
    protected String getExtraNodeTypes() {
        return "/mgnl-nodetypes/magnolia-dam-nodetypes.xml";
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        addSupportForSetupModuleRepositoriesTask(null);
        SecuritySupportImpl securitySupport = new SecuritySupportImpl();
        RoleManager roleManager = new MgnlRoleManager();
        securitySupport.setRoleManager(roleManager);
        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupport);
        roleManager.createRole(UserManager.SYSTEM_USER);
        roleManager.createRole(UserManager.ANONYMOUS_USER);
        Node userRolesRoot = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES).getRootNode();
        NodeTypes.Activatable.update(userRolesRoot.getNode(UserManager.SYSTEM_USER), UserManager.SYSTEM_USER, true);
        NodeTypes.Activatable.update(userRolesRoot.getNode(UserManager.ANONYMOUS_USER), UserManager.SYSTEM_USER, true);

        session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        website = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        setupConfigNode(CONTACTS_APPS_CONTACTS_NODE_PATH);
        setupConfigNode(UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_STK_NODE_PATH);
        setupConfigNode(UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_MANAGE_NODE_PATH);
        setupConfigNode(UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_TARGET_NODE_PATH);
        setupConfigNode("/modules/ui-admincentral/virtualURIMapping/default");
        setupConfigNode("/modules/site/config");
        setupConfigProperty("/server", "admin", "true");
        setupConfigNode("/server/filters/securityCallback/clientCallbacks/form");
    }

    @Override
    protected void setupNode(String workspace, String path) throws RepositoryException {
        super.setupNode(workspace, path);
        NodeTypes.Activatable.update(session.getNode(path), UserManager.SYSTEM_USER, true);
    }

    /**
     * When finding the default site in site module (doesn't have any sub nodes nor properties), the demo should add
     * and set the extends property pointing to the STK site.
     */
    @Test
    public void updateFrom07CreatesExtendsPropertyInSiteNodeWhenNodeIsEmpty() throws Exception {
        // GIVEN
        setupConfigNode("/modules/site/config/site");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.7"));

        // THEN
        assertTrue(session.propertyExists("/modules/site/config/site/extends"));
        assertThat(session.getProperty("/modules/site/config/site/extends").getString(), is("/modules/travel-demo/config/travel"));
    }

    @Test
    public void updateFrom07AllowsDemoRolesAccessToPagesApp() throws Exception {
        // GIVEN
        setupConfigNode("/modules/pages/apps/pages");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.7"));

        // THEN
        assertThat(session.getNode(PAGES_PERMISSIONS_ROLES), hasProperty(TRAVEL_DEMO_EDITOR_ROLE, TRAVEL_DEMO_EDITOR_ROLE));
        assertThat(session.getNode(PAGES_PERMISSIONS_ROLES), hasProperty(TRAVEL_DEMO_PUBLISHER_ROLE, TRAVEL_DEMO_PUBLISHER_ROLE));
    }

    @Test
    public void setAccessPermissionsAfterCleanInstall() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        assertThatAccessPermissionsAreConfigured(CONTACTS_APPS_CONTACTS_NODE_PATH, TRAVEL_DEMO_ADMINCENTRAL_ROLE, false);
        assertThatAccessPermissionsAreConfigured(UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_STK_NODE_PATH, TRAVEL_DEMO_ADMINCENTRAL_ROLE, false);
        assertThatAccessPermissionsAreConfigured(UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_MANAGE_NODE_PATH, TRAVEL_DEMO_ADMINCENTRAL_ROLE, false);

        assertThatAccessPermissionsAreConfigured(UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_TARGET_NODE_PATH, TRAVEL_DEMO_EDITOR_ROLE, true);
        assertThatAccessPermissionsAreConfigured(UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_TARGET_NODE_PATH, TRAVEL_DEMO_PUBLISHER_ROLE, true);

    }

    @Test
    public void updateFrom07SetsAccessPermissions() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.7"));

        // THEN
        assertThatAccessPermissionsAreConfigured(CONTACTS_APPS_CONTACTS_NODE_PATH, TRAVEL_DEMO_ADMINCENTRAL_ROLE, false);
        assertThatAccessPermissionsAreConfigured(UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_STK_NODE_PATH, TRAVEL_DEMO_ADMINCENTRAL_ROLE, false);
        assertThatAccessPermissionsAreConfigured(UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_MANAGE_NODE_PATH, TRAVEL_DEMO_ADMINCENTRAL_ROLE, false);
    }

    @Test
    public void updateFrom08SetsUpAccessToTargetAppGroup() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.8"));

        // THEN
        assertThatAccessPermissionsAreConfigured(UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_TARGET_NODE_PATH, TRAVEL_DEMO_EDITOR_ROLE, true);
        assertThatAccessPermissionsAreConfigured(UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_TARGET_NODE_PATH, TRAVEL_DEMO_PUBLISHER_ROLE, true);
    }


    @Test
    public void testUpgradeFrom081InstallsPurSamples() throws Exception {
        // GIVEN
        setupConfigProperty("/server", "admin", "false");
        setupConfigNode("/modules/public-user-registration");
        setupConfigNode("/modules/multisite/config/sites/travel/templates/availability/templates");

        // WHEN
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.8.1"));

        // THEN
        assertThat(session.getRootNode(), hasNode("modules/multisite/config/sites/travel/templates/availability/templates/pur"));

        this.checkPurSamplesAreInstalled(session.getNode("/server/filters/securityCallback/clientCallbacks"));
        this.checkIfEverythingIsActivated();
        this.assertNoMessages(ctx);
    }

    @Test
    public void testUpgradeFrom081PurNotInstalled() throws Exception {
        // GIVEN

        // WHEN
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.8.1"));

        // THEN
        assertThat(website.getRootNode(), hasNode("travel/book-tour"));
        this.checkIfEverythingIsActivated();
        this.assertNoMessages(ctx);
    }

    @Test
    public void upgradeFrom014RemovesTravelDemoThemeFromJCR() throws Exception {
        // GIVEN
        setupConfigNode("/modules/site/config/themes/travel-demo-theme");

        // WHEN
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.14"));

        // THEN
        assertThat(session.getRootNode(), not(hasNode("modules/site/config/themes/travel-demo-theme")));
        this.checkIfEverythingIsActivated();
        this.assertNoMessages(ctx);
    }

    @Test
    public void testCleanInstall() throws Exception {
        // GIVEN
        setupConfigNode("/modules/public-user-registration");
        setupConfigNode("/modules/multisite/config/sites/fallback");
        setupConfigProperty("/server", "admin", "false");

        // WHEN
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        assertThat(session.getRootNode(), hasNode("modules/multisite/config/sites/travel/templates/availability/templates/pur"));

        this.checkPurSamplesAreInstalled(session.getNode("/server/filters/securityCallback/clientCallbacks"));
        this.checkIfEverythingIsActivated();
        this.assertNoMessages(ctx);
    }

    private void checkPurSamplesAreInstalled(Node clientCallbacks) throws RepositoryException {
        assertThat(website.getRootNode(), hasNode("travel/" + InstallPurSamplesTask.PUR_SAMPLE_ROOT_PAGE_NAME));
        assertThat(website.getRootNode(), hasNode(InstallPurSamplesTask.PASSWORD_CHANGE_PAGE_PATH));
        assertThat(website.getNode("/travel/" + InstallPurSamplesTask.PUR_SAMPLE_ROOT_PAGE_NAME), allOf(
                hasNode(InstallPurSamplesTask.PROTECTED_PAGES_NAMES.get(0)),
                hasNode(InstallPurSamplesTask.PROTECTED_PAGES_NAMES.get(1))
        ));

        assertThat(MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES).getRootNode(), hasNode("travel-demo-pur"));
        assertThat(MgnlContext.getJCRSession(DamConstants.WORKSPACE).getRootNode(), hasNode("travel-demo/img/gate-hernan-pinera.jpg"));

        assertThat(clientCallbacks, hasNode("travel-demo-pur/originalUrlPattern"));
        Node callback = clientCallbacks.getNodes().nextNode();
        assertThat(callback.getName(), equalTo("travel-demo-pur"));
        assertThat(callback.getNode("originalUrlPattern"), hasProperty("patternString", "(*|travel)/members/(profile-update|protected)*"));
        assertThat(callback.getNode("originalUrlPattern").getProperty("patternString").getString(), allOf(
                containsString(InstallPurSamplesTask.PUR_SAMPLE_ROOT_PAGE_NAME),
                containsString(InstallPurSamplesTask.PROTECTED_PAGES_NAMES.get(0)),
                containsString(InstallPurSamplesTask.PROTECTED_PAGES_NAMES.get(1))
        ));

        assertThat(session.getRootNode(), hasNode("modules/travel-demo/config/travel/templates/availability/templates/pur"));
        assertThat(session.getNode("/modules/public-user-registration/config/configurations/travel/passwordRetrievalStrategy"), hasProperty("targetPagePath", "/" + InstallPurSamplesTask.PASSWORD_CHANGE_PAGE_PATH));
        assertThat(session.getNode("/modules/public-user-registration/config/configurations/travel/defaultGroups"), hasProperty("pur", "travel-demo-pur"));

        assertThat(MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES).getRootNode(), hasNode(UserManager.ANONYMOUS_USER + "/acl_uri"));
        NodeIterator permissions = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES).getNode("/" + UserManager.ANONYMOUS_USER + "/acl_uri").getNodes();
        while (permissions.hasNext()) {
            assertThat(permissions.nextNode().getProperty("path").getString(), isIn(InstallPurSamplesTask.PROTECTED_PAGES_PATHS));
        }
        assertThat(MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES).getRootNode(), hasNode("travel-demo-pur" + "/acl_uri"));
        permissions = MgnlContext.getJCRSession(RepositoryConstants.USER_ROLES).getNode("/travel-demo-pur/acl_uri").getNodes();
        while (permissions.hasNext()) {
            assertThat(permissions.nextNode().getProperty("path").getString(), isIn(InstallPurSamplesTask.PROTECTED_PAGES_PATHS));
        }

        assertThat(MgnlContext.getJCRSession(RepositoryConstants.USER_GROUPS).getRootNode(), hasNode("travel-demo-pur"));
    }

    private void checkIfEverythingIsActivated() throws RepositoryException {
        //this.checkAllNodesInWorkspaceAreActivated(RepositoryConstants.CONFIG, "/", NodeTypes.Content.NAME); TODO config is not activated
        this.checkAllNodesInWorkspaceAreActivated(RepositoryConstants.WEBSITE, "/", NodeTypes.Page.NAME);
        this.checkAllNodesInWorkspaceAreActivated(RepositoryConstants.USER_ROLES, "/", NodeTypes.Role.NAME);
        this.checkAllNodesInWorkspaceAreActivated(RepositoryConstants.USER_GROUPS, "/", NodeTypes.Group.NAME);
        this.checkAllNodesInWorkspaceAreActivated(RepositoryConstants.USERS, "/", NodeTypes.User.NAME);
        this.checkAllNodesInWorkspaceAreActivated(DamConstants.WORKSPACE, "/", "mgnl:asset");
    }

    private void checkAllNodesInWorkspaceAreActivated(String workspace, String path, String nodetype) throws RepositoryException {
        NodeIterator iterator = QueryUtil.search(workspace, String.format("select * from [%s] WHERE ISDESCENDANTNODE(['%s'])", nodetype, path));
        while (iterator.hasNext()) {
            Node node = iterator.nextNode();
            if (!NodeTypes.Activatable.isActivated(node)) {
                fail(node + " is not activated!");
            }
        }
    }

    private void assertThatAccessPermissionsAreConfigured(String path, String role, boolean allow) throws RepositoryException {

        assertThat(session.getNode(path.concat(PERMISSIONS_NODE_PATH)), hasProperty("class", VoterBasedConfiguredAccessDefinition.class.getName()));

        if (allow) {
            assertThat(session.getNode(path.concat(VOTERS_ALLOWED_ROLES)), hasProperty("class", RoleBaseVoter.class.getName()));
            assertThat(session.getNode(path.concat(PERMISSIONS_VOTERS_ALLOWED_ROLES_ROLES_NODE_PATH)), hasProperty(role, role));
        } else {
            assertThat(session.getNode(path.concat(VOTERS_DENIED_ROLES)), hasProperty("class", RoleBaseVoter.class.getName()));
            assertThat(session.getNode(path.concat(VOTERS_DENIED_ROLES)), hasProperty("not", "true"));
            assertThat(session.getNode(path.concat(PERMISSIONS_VOTERS_DENIED_ROLES_ROLES_NODE_PATH)), hasProperty(role, role));
        }
    }

}
