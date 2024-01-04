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
package info.magnolia.demo.travel.tours.setup;

import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeTypes.Activatable;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class ToursModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Session configSession;
    private Session websiteSession;
    private Session damSession;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/tours.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new ToursModuleVersionHandler();
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList("/META-INF/magnolia/core.xml");
    }

    @Override
    protected String[] getExtraWorkspaces() {
        return new String[]{RepositoryConstants.WEBSITE, "tours", "category", "dam"};
    }

    @Override
    protected String getExtraNodeTypes() {
        return "/mgnl-nodetypes/test-tour-nodetypes.xml";
    }

    @Override
    public String getRepositoryConfigFileName() {
        return "/info/magnolia/demo/travel/tours/service/test-tours-repositories.xml";
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        websiteSession = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        damSession = MgnlContext.getJCRSession("dam");

        addSupportForSetupModuleRepositoriesTask(null);

        setupConfigNode("/modules/tours/apps");
    }

    @Test
    public void cleanInstall() throws Exception {
        // GIVEN
        setupBootstrapPages();
        PropertyUtil.setProperty(websiteSession.getNode("/travel/tour-type"), Activatable.ACTIVATION_STATUS, Long.valueOf(Activatable.ACTIVATION_STATUS_MODIFIED));
        PropertyUtil.setProperty(websiteSession.getNode("/travel/destination"), Activatable.ACTIVATION_STATUS, Long.valueOf(Activatable.ACTIVATION_STATUS_MODIFIED));
        PropertyUtil.setProperty(websiteSession.getNode("/travel/tour"), Activatable.ACTIVATION_STATUS, Long.valueOf(Activatable.ACTIVATION_STATUS_MODIFIED));
        setupConfigNode("/server/filters/i18n");
        setupConfigNode("/server/filters/virtualURI");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        int activationStatus = Activatable.getActivationStatus(websiteSession.getNode("/travel/tour-type"));
        assertThat("We expect that /travel/tour-type node is activated", activationStatus, equalTo(Activatable.ACTIVATION_STATUS_ACTIVATED));

        activationStatus = Activatable.getActivationStatus(websiteSession.getNode("/travel/destination"));
        assertThat("We expect that /travel/destination node is activated", activationStatus, equalTo(Activatable.ACTIVATION_STATUS_ACTIVATED));

        activationStatus = Activatable.getActivationStatus(websiteSession.getNode("/travel/tour"));
        assertThat("We expect that /travel/tour node is activated", activationStatus, equalTo(Activatable.ACTIVATION_STATUS_ACTIVATED));

        Iterable<Node> filterOrder = NodeUtil.getNodes(configSession.getNode("/server/filters"));
        assertThat(filterOrder, contains(
                Matchers.hasProperty("name", is("virtualURI")),
                Matchers.hasProperty("name", is("i18n"))
        ));
    }

    @Test
    public void explicitlyBootstrappedCareersMain05NodeOrderedFreshInstall() throws Exception {
        // GIVEN
        setupBootstrapPages();
        Node careersMain = NodeUtil.createPath(websiteSession.getRootNode(), "/travel/about/careers/main", NodeTypes.Component.NAME, true);
        setupConfigNode("server/filters/virtualURI");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        assertThat(careersMain, hasNode("05"));
        List<Node> careerNodeList = Lists.newArrayList(careersMain.getNodes());
        assertThat(Collections2.transform(careerNodeList, new ToNodeName()), contains(
                "01",
                "05",
                "06"
        ));
    }

    @Test
    public void updateFrom163RemovesUriMappingsAndReorderFilters() throws Exception {
        // GIVEN
        Node toursModule = NodeUtil.createPath(configSession.getRootNode(), "/modules/tours", NodeTypes.Content.NAME, true);
        toursModule.addNode("virtualUriMappings", NodeTypes.Content.NAME);
        setupConfigNode("/server/filters/i18n");
        setupConfigNode("/server/filters/virtualURI");

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("1.6.3"));

        //THEN
        assertThat(toursModule, not(hasNode("virtualUriMappings")));
        Iterable<Node> filterOrder = NodeUtil.getNodes(configSession.getNode("/server/filters"));
        assertThat(filterOrder, contains(
                Matchers.hasProperty("name", is("virtualURI")),
                Matchers.hasProperty("name", is("i18n"))));
    }

    private void setupBootstrapPages() throws RepositoryException {
        websiteSession.getRootNode().addNode("travel", NodeTypes.Page.NAME);
        websiteSession.getRootNode().addNode("travel/about", NodeTypes.Page.NAME);
        websiteSession.getRootNode().addNode("travel/tour-type", NodeTypes.Page.NAME);
        websiteSession.getRootNode().addNode("travel/destination", NodeTypes.Page.NAME);
        websiteSession.getRootNode().addNode("travel/tour", NodeTypes.Page.NAME);
        websiteSession.getRootNode().addNode("travel/about/careers", NodeTypes.Page.NAME);
        websiteSession.getRootNode().addNode("travel/about/careers/main", NodeTypes.Area.NAME);
        websiteSession.getRootNode().addNode("travel/about/careers/main/01", NodeTypes.Component.NAME);
        websiteSession.getRootNode().addNode("travel/about/careers/main/06", NodeTypes.Component.NAME);
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
