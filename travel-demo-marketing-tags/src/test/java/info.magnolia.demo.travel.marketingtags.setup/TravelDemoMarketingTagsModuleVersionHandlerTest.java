/**
 * This file Copyright (c) 2015-2024 Magnolia International
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
package info.magnolia.demo.travel.marketingtags.setup;

import static info.magnolia.test.hamcrest.NodeMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.marketingtags.MarketingTagsModule;
import info.magnolia.marketingtags.app.TagsNodeTypes;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.ModuleVersionHandlerTestCase;
import info.magnolia.module.model.Version;
import info.magnolia.repository.RepositoryConstants;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link info.magnolia.demo.travel.marketingtags.setup.TravelDemoMarketingTagsModuleVersionHandler}.
 */
public class TravelDemoMarketingTagsModuleVersionHandlerTest extends ModuleVersionHandlerTestCase {

    private Session config;

    private Session website;

    private Session marketingTags;

    @Override
    protected String getModuleDescriptorPath() {
        return "/META-INF/magnolia/travel-demo-marketing-tags.xml";
    }

    @Override
    protected ModuleVersionHandler newModuleVersionHandlerForTests() {
        return new TravelDemoMarketingTagsModuleVersionHandler();
    }

    @Override
    protected List<String> getModuleDescriptorPathsForTests() {
        return Arrays.asList("/META-INF/magnolia/core.xml");
    }

    @Override
    protected String[] getExtraWorkspaces() {
        return new String[]{"marketing-tags"};
    }

    @Override
    protected String getExtraNodeTypes() {
        return "/mgnl-nodetypes/magnolia-tags-nodetypes.xml";
    }

    @Override
    protected String getRepositoryConfigFileName() {
        return "test-marketing-tags-repositories.xml";
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        addSupportForSetupModuleRepositoriesTask(MarketingTagsModule.WORKSPACE);
        config = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
        website = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        marketingTags = MgnlContext.getJCRSession(MarketingTagsModule.WORKSPACE);

        NodeUtil.createPath(website.getRootNode(), "travel", NodeTypes.Page.NAME);
    }

    @Test
    public void cleanInstall() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(null);

        // THEN
        assertThat(config.getNode("/modules"), not(hasNode("multisite")));
        assertThat(MgnlContext.getJCRSession(MarketingTagsModule.WORKSPACE).getRootNode(), hasNode("Google-Analytics-for-Travel-Demo", TagsNodeTypes.Tag.NAME));
    }

    @Test
    public void updateTo12() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("1.1"));

        // THEN
        assertThat(marketingTags.getRootNode(), hasNode("Clicky-for-Travel-Demo"));
        assertThat(marketingTags.getRootNode(), hasNode("Google-Analytics-for-Travel-Demo"));
    }

    @Test
    public void updateTo13() throws Exception {
        // GIVEN

        // WHEN
        executeUpdatesAsIfTheCurrentlyInstalledVersionWas(Version.parseVersion("1.2.4"));

        // THEN
        assertThat(website.getNode("/travel"), allOf(
                hasProperty("bannerbackground", "#000"),
                hasProperty("buttonbackground", "#ef6155"),
                hasProperty("buttontext", "#fff"),
                hasProperty("complianceType", "info"),
                hasProperty("allow", "Allow cookies"),
                hasProperty("deny", "Decline cookies"),
                hasProperty("dismiss", "Got it!"),
                hasProperty("layout", "block"),
                hasProperty("link", "Learn more ..."),
                hasProperty("header", "Cookies are used on this website!"),
                hasProperty("message", "This website uses cookies to ensure you get the best experience on our website."),
                hasProperty("position", "bottom"),
                hasProperty("readMoreLink", "external"),
                hasProperty("readMoreLinkexternal", "https://cookiesandyou.com/")
        ));
        assertThat(marketingTags.getRootNode(), hasNode("Clicky-for-Travel-Demo"));
        assertThat(marketingTags.getRootNode(), hasNode("Google-Analytics-for-Travel-Demo"));
        assertThat(config.getRootNode(), hasNode("modules/cookie-manager/config/cookies"));
    }
}
