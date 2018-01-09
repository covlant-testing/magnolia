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
package info.magnolia.demo.travel.tours.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.context.MgnlContext;
import info.magnolia.dam.templating.functions.DamTemplatingFunctions;
import info.magnolia.demo.travel.tours.ToursModule;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.module.categorization.functions.CategorizationTemplatingFunctions;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.rendering.template.type.TemplateTypeHelper;
import info.magnolia.templating.functions.TemplatingFunctions;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockWebContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;

/**
 * Repository tests for {@link TourServices}.
 */
public class TourServicesRepositoryTest extends RepositoryTestCase {

    private final String repositoryConfigFileName = "info/magnolia/demo/travel/tours/service/test-tours-repositories.xml";

    private TourServices tourServices;

    private Session tourSession;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        tourSession = MgnlContext.getJCRSession(ToursModule.TOURS_REPOSITORY_NAME);

        final ToursModule toursModule = new ToursModule();
        final TemplateDefinitionRegistry templateDefinitionRegistry = mock(TemplateDefinitionRegistry.class);
        final TemplateTypeHelper templateTypeHelper = new TemplateTypeHelper(templateDefinitionRegistry);
        final Provider<AggregationState> aggregationStateProvider = new Provider<AggregationState>() {
            @Override
            public AggregationState get() {
                return MgnlContext.getAggregationState();
            }
        };
        final TemplatingFunctions templatingFunctions = new TemplatingFunctions(aggregationStateProvider, templateTypeHelper);

        tourServices = new TourServices(toursModule, templateTypeHelper, templatingFunctions, mock(CategorizationTemplatingFunctions.class), mock(DamTemplatingFunctions.class), new LinkTransformerManager());

        ComponentsTestUtil.setImplementation(I18nContentSupport.class, DefaultI18nContentSupport.class);
    }

    @Override
    public String getRepositoryConfigFileName() {
        return this.repositoryConfigFileName;
    }

    @Test
    public void getTourNodeByParameter() throws Exception {
        // GIVEN
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(TourServices.TOUR_QUERY_PARAMETER, "quz");

        ((MockWebContext) MgnlContext.getInstance()).setParameters(parameters);

        final Node node = NodeUtil.createPath(tourSession.getRootNode(), "/test/foo/bar/quz", NodeTypes.Content.NAME, true); // Need to save, running a query afterwards

        // WHEN
        final Node tourNode = tourServices.getTourNodeByParameter();

        // THEN
        assertThat(tourNode.getIdentifier(), is(node.getIdentifier()));
    }

    @Test
    public void getRelatedToursByCategory() throws Exception {
        // GIVEN
        final Node referenceNode = NodeUtil.createPath(tourSession.getRootNode(), "/test/foo/bar/quz", NodeTypes.Content.NAME);
        final Node node = NodeUtil.createPath(tourSession.getRootNode(), "/another-test", NodeTypes.Content.NAME);
        node.setProperty("isFeatured", true);
        node.setProperty(Tour.PROPERTY_NAME_TOUR_TYPES_CATEGORY, new String[]{referenceNode.getIdentifier()});

        tourSession.save(); // Need to save, running a query afterwards

        // WHEN
        final List<Tour> tours = tourServices.getToursByCategory(Tour.PROPERTY_NAME_TOUR_TYPES_CATEGORY, referenceNode.getIdentifier(), true);

        // THEN
        assertThat(tours, hasSize(1));
        assertThat(tours.get(0).getIdentifier(), is(node.getIdentifier()));
    }

}