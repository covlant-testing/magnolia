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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static info.magnolia.test.hamcrest.NodeMatchers.*;

import info.magnolia.cms.security.MgnlRoleManager;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.SecuritySupportImpl;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.hamcrest.NodeMatchers;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * Tests for {@link TravelDemoComponentPersonalizationModuleVersionHandler}.
 */
public class TravelDemoComponentPersonalizationModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private final String travelDemoAdminCentralRoleName = "travel-demo-admincentral";

    private Session websiteSession;
    private Session configSession;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/travel-demo-component-personalization.xml";
    }

    @Override
    protected String[] getExtraWorkspaces() {
        return new String[]{RepositoryConstants.WEBSITE};
    }

    @Override
    protected String getExtraNodeTypes() {
        return "/mgnl-nodetypes/test-component-personalization-nodetypes.xml";
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList("/META-INF/magnolia/core.xml");
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new TravelDemoComponentPersonalizationModuleVersionHandler();
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

        this.websiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        this.configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
    }

    @Test
    public void updateFrom013AddsCookieTraitConfiguration() throws Exception {
        // GIVEN
        Node personalizationTraits = NodeUtil.createPath(configSession.getRootNode(), "modules/personalization-traits", NodeTypes.Content.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.13"));

        // THEN
        Node options = personalizationTraits.getNode("traits/cookies/valueField/options");
        List<Node> tourTypes = Lists.newArrayList(NodeUtil.getNodes(options));
        assertThat(Collections2.transform(tourTypes, new ToNodeName()), contains("tourTypeAny"));

        assertThat(personalizationTraits.hasNode("traits/cookies/ruleField/options/tourType"), is(true));
        assertThat(personalizationTraits.hasNode("traits/cookies/ruleField/fields/tourType"), is(true));

    }

    @Test
    public void updateFrom013AddsComponentVariants() throws Exception {
        // GIVEN
        Node travelPage = websiteSession.getRootNode().addNode("travel", NodeTypes.Page.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("0.13"));

        // THEN
        Node variantRoot = travelPage.getNode("main/0/variants");
        List<Node> variants = Lists.newArrayList(NodeUtil.getNodes(variantRoot));
        assertThat(variants, hasItems(NodeMatchers.hasProperty("jcr:primaryType", NodeTypes.Component.NAME), new MixinPropertyMatcher("mgnl:variant")));
    }

    @Test
    public void updateFrom113RemovesSpecificTourTypesFromTraits() throws Exception {
        // GIVEN
        Node cookiesOptions = NodeUtil.createPath(configSession.getRootNode(), "modules/personalization-traits/traits/cookies/valueField/options", NodeTypes.Content.NAME);
        cookiesOptions.addNode("tourTypeActive", NodeTypes.Content.NAME);
        cookiesOptions.addNode("tourTypeOffbeat", NodeTypes.Content.NAME);
        cookiesOptions.addNode("tourTypeCultural", NodeTypes.Content.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("1.1.3"));

        // THEN
        assertThat(cookiesOptions, allOf(
                not(hasNode("tourTypeActive")),
                not(hasNode("tourTypeOffbeat")),
                not(hasNode("tourTypeCultural"))
        ));
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

    private static class MixinPropertyMatcher extends TypeSafeMatcher<Node> {

        private final String propertyValue;

        public MixinPropertyMatcher(String propertyValue) {
            this.propertyValue = propertyValue;
        }

        @Override
        protected boolean matchesSafely(Node item) {
            try {
                for (NodeType nt : item.getMixinNodeTypes()) {
                    if (nt.getName().equals(propertyValue)) {
                        return true;
                    }
                }
            } catch (RepositoryException e) {
               throw new RuntimeException(e);
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(String.format("should match a mixin property with value [%s]", propertyValue));
        }
    }
}
