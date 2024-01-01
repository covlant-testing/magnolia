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
package info.magnolia.demo.travel.contenttags.setup;

import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.contenttags.manager.TagManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.dam.jcr.AssetNodeTypes;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.repository.RepositoryManager;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;


public class TravelDemoContentTagsModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Session tours;
    private Session dam;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/travel-demo-content-tags.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new TravelDemoContentTagsModuleVersionHandler(new TagManager(() -> Components.getComponent(SystemContext.class), null, new NodeNameHelper(mock(MagnoliaConfigurationProperties.class))));
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Lists.newArrayList("/META-INF/magnolia/core.xml");
    }

    @Override
    protected String getRepositoryConfigFileName() {
        return "test-content-tags-repositories.xml";
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // Register mgnl:tag node type
        RepositoryManager repositoryManager = Components.getComponent(RepositoryManager.class);
        repositoryManager.getRepositoryProvider("magnolia").registerNodeTypes(ClasspathResourcesUtil.getResource("mgnl-nodetypes/content-tags-nodetypes.xml").openStream());

        tours = MgnlContext.getJCRSession("tours");
        dam = MgnlContext.getJCRSession("dam");
    }

    @Test
    public void cleanInstall() throws Exception {
        // GIVEN
        setupNode("tours", "/magnolia-travels/Kyoto");
        setupNode(RepositoryConstants.WEBSITE, "/travel/tour/main/0");

        // WHEN
        final InstallContext installContext = executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        assertThat(tours.getNode("/magnolia-travels/Kyoto"), hasProperty(TagManager.TAGS_PROPERTY));
        Property tags = tours.getNode("/magnolia-travels/Kyoto").getProperty(TagManager.TAGS_PROPERTY);
        assertThat(tags.getValues(), arrayWithSize(3));

        assertThat(installContext.getConfigJCRSession().getRootNode(), hasNode("modules/content-tags-core/config/tagableWorkspaces/tours"));
        Node component = installContext.getJCRSession(RepositoryConstants.WEBSITE).getNode("/travel/tour/main/0");
        assertThat(component, hasProperty(NodeTypes.Renderable.TEMPLATE, "travel-demo-content-tags:components/tourDetail-content-tags"));
        assertThat(component, hasProperty("tourListLink", installContext.getJCRSession(RepositoryConstants.WEBSITE).getNode("/travel/tour-tag").getIdentifier()));
    }

    @Test
    public void tourAssetsAreTaggedUponFreshInstall() throws Exception {
        // GIVEN
        String assetPath = "/tours/shark_brian_warrick_0824.JPG";
        Node assetNode = NodeUtil.createPath(dam.getRootNode(), assetPath, NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        assertThat(assetNode, allOf(
                hasProperty(TagManager.TAGS_PROPERTY, arrayWithSize(5)),
                hasProperty(AssetNodeTypes.Asset.LAST_TAGGING_ATTEMPT_DATE)
        ));
    }

    @Test
    public void updateFrom14ThenTourAssetsAreTagged() throws Exception {
        // GIVEN
        String assetPath = "/tours/shark_brian_warrick_0824.JPG";
        Node assetNode = NodeUtil.createPath(dam.getRootNode(), assetPath, NodeTypes.ContentNode.NAME);

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("1.4"));

        // THEN
        assertThat(assetNode, allOf(
                hasProperty(TagManager.TAGS_PROPERTY, arrayWithSize(5)),
                hasProperty(AssetNodeTypes.Asset.LAST_TAGGING_ATTEMPT_DATE)
        ));
    }
}
