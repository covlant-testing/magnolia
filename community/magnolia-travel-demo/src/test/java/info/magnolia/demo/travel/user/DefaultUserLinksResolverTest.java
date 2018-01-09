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
package info.magnolia.demo.travel.user;

import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.LogoutFilter;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.objectfactory.guice.GuiceUtils;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.rendering.template.type.DefaultTemplateTypes;
import info.magnolia.rendering.template.type.TemplateTypeHelper;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.templating.functions.TemplatingFunctions;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link DefaultUserLinksResolver}.
 */
public class DefaultUserLinksResolverTest extends RepositoryTestCase {

    private DefaultUserLinksResolver resolver;
    private Node siteRoot, loginPage, profilePage;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        TemplateDefinitionRegistry registry = mock(TemplateDefinitionRegistry.class);

        ConfiguredTemplateDefinition rootPageDefinition = new ConfiguredTemplateDefinition(null);
        rootPageDefinition.setType(DefaultTemplateTypes.SITE_ROOT);
        when(registry.getTemplateDefinition(rootPageDefinition.getId())).thenReturn(rootPageDefinition);

        ConfiguredTemplateDefinition loginPageDefinition = new ConfiguredTemplateDefinition(null);
        loginPageDefinition.setId("loginPageTemplate");
        when(registry.getTemplateDefinition(loginPageDefinition.getId())).thenReturn(loginPageDefinition);

        ConfiguredTemplateDefinition profilePageDefinition = new ConfiguredTemplateDefinition(null);
        profilePageDefinition.setId("profilePageDefinition");
        when(registry.getTemplateDefinition(profilePageDefinition.getId())).thenReturn(profilePageDefinition);

        resolver = new DefaultUserLinksResolver(
                GuiceUtils.providerForInstance(MgnlContext.getWebContext()),
                GuiceUtils.providerForInstance(MgnlContext.getAggregationState()),
                new TemplatingFunctions(null, new TemplateTypeHelper(registry), null)
        );
        resolver.setLoginPageTemplateId(profilePageDefinition.getId());
        resolver.setRegistrationPageTemplateId("nonExistingTemplate");

        Session session = MgnlContext.getJCRSession(RepositoryConstants.WEBSITE);
        siteRoot = session.getRootNode().addNode("siteRoot", NodeTypes.Page.NAME);
        NodeTypes.Renderable.set(siteRoot, DefaultTemplateTypes.SITE_ROOT);

        loginPage = siteRoot.addNode("loginPage", NodeTypes.Page.NAME);
        NodeTypes.Renderable.set(loginPage, resolver.getLoginPageTemplateId());

        profilePage = siteRoot.addNode("profilePage", NodeTypes.Page.NAME);
        NodeTypes.Renderable.set(profilePage, resolver.getLoginPageTemplateId());

        session.save();

        I18nContentSupport i18nContentSupport = mock(I18nContentSupport.class);
        when(i18nContentSupport.toI18NURI(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (String) args[0];
            }
        });
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18nContentSupport);
    }


    @Test
    public void useForCurrentPage() throws RepositoryException {
        // GIVEN
        MgnlContext.getAggregationState().setMainContentNode(loginPage);

        // WHEN
        boolean useForCurrentPage = resolver.useForCurrentPage();

        // THEN
        assertTrue(useForCurrentPage);
    }

    @Test
    public void getLoginPageLink() throws RepositoryException {
        // GIVEN
        MgnlContext.getAggregationState().setMainContentNode(siteRoot);

        // WHEN
        String link = resolver.getLoginPageLink();

        // THEN
        assertThat(link, equalTo(loginPage.getPath()));
    }

    @Test
    public void getLogoutLink() throws RepositoryException {
        // GIVEN
        MgnlContext.getAggregationState().setMainContentNode(loginPage);

        // WHEN
        String link = resolver.getLogoutLink();

        // THEN
        assertThat(link, equalTo(loginPage.getPath() + "?" + LogoutFilter.PARAMETER_LOGOUT + "=true"));
    }

    @Test
    public void getProfilePageLink() throws RepositoryException {
        // GIVEN
        MgnlContext.getAggregationState().setMainContentNode(siteRoot);

        // WHEN
        String link = resolver.getProfilePageLink();

        // THEN
//        assertThat(link, equalTo(loginPage.getPath());
    }

    @Test
    public void getRegistrationPageLinkWhenRegistrationPageDoesntExist() throws RepositoryException {
        // GIVEN
        MgnlContext.getAggregationState().setMainContentNode(loginPage);

        // WHEN
        String link = resolver.getRegistrationPageLink();

        // THEN
        assertNull(link);
    }

    @Test
    public void getUsernameForAnonymousUser() throws RepositoryException {
        // GIVEN

        // WHEN
        String username = resolver.getUsername();

        // THEN
        assertNull(username);
    }
}