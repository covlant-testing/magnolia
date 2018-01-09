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

import static info.magnolia.demo.travel.setup.CopySiteToMultiSiteAndMakeItFallback.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link CopySiteToMultiSiteAndMakeItFallback}. Unfortunately we have to use the "heavy"
 * {@link RepositoryTestCase} because the underlying task uses {@link NodeUtil#copyInSession(Node, String)} which does
 * not work with mocked {@link Node}s.
 */
public class CopySiteToMultiSiteAndMakeItFallbackTest extends RepositoryTestCase {

    private CopySiteToMultiSiteAndMakeItFallback copySiteToMultiSiteAndMakeItFallback;
    private InstallContext installContext;
    private Session configSession;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);

        // Yes this is ugly, but our tests still use this
        final HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(RepositoryConstants.CONFIG);

        installContext = mock(InstallContext.class);
        when(installContext.getConfigJCRSession()).thenReturn(configSession);
        when(installContext.getJCRSession(RepositoryConstants.CONFIG)).thenReturn(configSession);
        when(installContext.getHierarchyManager(RepositoryConstants.CONFIG)).thenReturn(hierarchyManager);

        copySiteToMultiSiteAndMakeItFallback = new CopySiteToMultiSiteAndMakeItFallback();

        // We require the multisite config to exist
        NodeUtil.createPath(configSession.getRootNode(), "/modules/multisite/config/sites", NodeTypes.ContentNode.NAME);
        NodeUtil.createPath(configSession.getRootNode(), MULTISITE_FALLBACK_SITE, NodeTypes.ContentNode.NAME);

        // Travel demo site
        NodeUtil.createPath(configSession.getRootNode(), TRAVEL_DEMO_SITE, NodeTypes.ContentNode.NAME);
    }

    @Test
    public void checkThatSiteIsCopied() throws Exception {
        // GIVEN
        final Node travelSiteNode = configSession.getNode(TRAVEL_DEMO_SITE);
        travelSiteNode.setProperty("test", "test-value");

        // WHEN
        copySiteToMultiSiteAndMakeItFallback.execute(installContext);

        // THEN
        assertTrue(configSession.itemExists(MULTISITE_TRAVEL_SITE));
        assertTrue(configSession.propertyExists(MULTISITE_TRAVEL_SITE + "/test"));
    }

    @Test
    public void checkThatFallbackIsSetWhenPropertyDoesNotExist() throws Exception {
        // GIVEN

        // WHEN
        copySiteToMultiSiteAndMakeItFallback.execute(installContext);

        // THEN
        assertTrue(configSession.propertyExists(MULTISITE_FALLBACK_SITE + "/extends"));
        assertThat(configSession.getProperty(MULTISITE_FALLBACK_SITE + "/extends").getString(), is("../travel"));
    }

    @Test
    public void checkThatFallbackIsSetWhenPropertyDoesExist() throws Exception {
        // GIVEN
        final Node fallBackNode = configSession.getNode(MULTISITE_FALLBACK_SITE);
        fallBackNode.setProperty("extends", "../default");

        // WHEN
        copySiteToMultiSiteAndMakeItFallback.execute(installContext);

        // THEN
        assertTrue(configSession.propertyExists(MULTISITE_FALLBACK_SITE + "/extends"));
        assertThat(configSession.getProperty(MULTISITE_FALLBACK_SITE + "/extends").getString(), is("../travel"));
    }

}