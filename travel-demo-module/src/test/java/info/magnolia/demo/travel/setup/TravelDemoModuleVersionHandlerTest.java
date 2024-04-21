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

import static info.magnolia.test.hamcrest.NodeMatchers.hasProperty;
import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsIn.isIn;
import static org.junit.Assert.*;

import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.dam.jcr.DamConstants;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;

import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class TravelDemoModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private static final String UIADMINCENTRAL_CONFIG_APPLAUNCH_GROUPS_TARGET_NODE_PATH = "/modules/ui-admincentral/config/appLauncherLayout/groups/target";
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
        return "/mgnl-nodetypes/magnolia-dam-nodetypes.cnd";
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
        setupConfigNode("/modules/ui-admincentral/virtualUriMappings/default");
        setupConfigNode("/modules/site/config");
        setupConfigProperty("/server", "admin", "true");
        setupConfigNode("/server/filters/securityCallback/clientCallbacks/form");
        setupConfigNode("/server/filters/uriSecurity");
    }

    @Override
    protected void setupNode(String workspace, String path) throws RepositoryException {
        super.setupNode(workspace, path);
        NodeTypes.Activatable.update(session.getNode(path), UserManager.SYSTEM_USER, true);
    }

    @Test
    public void upgradeFrom163() throws Exception {
        // GIVEN
        setupConfigNode("/server/filters/i18n/bypasses");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("1.6.3"));

        // THEN
        String i18nFilterPath = "/server/filters/i18n";
        assertThat(session.getNode(i18nFilterPath), not(hasNode("bypasses")));
    }

    @Test
    public void upgradeFrom169() throws Exception {
        // GIVEN
        NodeUtil.createPath(website.getRootNode(), "travel/members/registration/main/0/fieldsets/0/fields/0", NodeTypes.ContentNode.NAME);
        PropertyUtil.setProperty(NodeUtil.createPath(website.getRootNode(), "travel/members/registration/main/0/fieldsets/0/fields/02", NodeTypes.ContentNode.NAME), "email", Lists.newArrayList("email"));

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("1.6.9"));

        // THEN
        assertTrue(website.getNode("/travel/members/registration/main/0/fieldsets/0/fields/0").getProperty("validation").isMultiple());
        assertThat(website.getNode("/travel/members/registration/main/0/fieldsets/0/fields/0").getProperty("validation").getValues()[0].getString(), is("username"));
        assertTrue(website.getNode("/travel/members/registration/main/0/fieldsets/0/fields/02").getProperty("validation").isMultiple());
        assertThat(website.getNode("/travel/members/registration/main/0/fieldsets/0/fields/02").getProperty("validation").getValues()[0].getString(), is("email"));
        assertThat(website.getNode("/travel/members/registration/main/0/fieldsets/0/fields/02").getProperty("validation").getValues()[1].getString(), is("uniqueEmail"));
    }

    @Test
    public void cleanInstall() throws Exception {
        // GIVEN
        setupConfigNode("/modules/public-user-registration");
        setupConfigProperty("/server", "admin", "false");

        // WHEN
        final InstallContext ctx = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        assertThat("Default URI to home page has been set", session.getNode("/modules/ui-admincentral/virtualUriMappings/default"), hasProperty("toUri", "redirect:/travel.html"));

        this.checkPurSamplesAreInstalled(session.getNode("/server/filters/securityCallback/clientCallbacks"));
        this.checkIfEverythingIsActivated();
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
}
