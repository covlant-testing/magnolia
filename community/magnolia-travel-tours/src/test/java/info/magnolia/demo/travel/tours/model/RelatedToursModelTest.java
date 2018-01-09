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
package info.magnolia.demo.travel.tours.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.*;

import info.magnolia.demo.travel.tours.model.definition.TourCategoryTemplateDefinition;
import info.magnolia.demo.travel.tours.service.Tour;
import info.magnolia.demo.travel.tours.service.TourServices;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.test.MgnlTestCase;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link RelatedToursModel}.
 */
public class RelatedToursModelTest extends MgnlTestCase {

    private TourServices tourServices;
    private RelatedToursModel model;
    private TourCategoryTemplateDefinition templateDefinition;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.tourServices = mock(TourServices.class);
        templateDefinition = mock(TourCategoryTemplateDefinition.class);

        this.model = new RelatedToursModel(mock(Node.class), templateDefinition, mock(RenderingModel.class), tourServices);
    }

    @Test
    public void testFilterCurrentTour() throws Exception {
        // GIVEN
        final Node currentTourNode = mock(Node.class);
        final Tour currentTour = new Tour();
        final Tour someTour1 = new Tour();
        final Tour someTour2 = new Tour();

        String identifier = "bla-124-124sd";
        String categoryName = "some-category";

        currentTour.setIdentifier(identifier);
        someTour1.setIdentifier(identifier + "-1");
        someTour2.setIdentifier(identifier + "-2");

        when(currentTourNode.getIdentifier()).thenReturn(identifier);
        when(templateDefinition.getCategory()).thenReturn(categoryName);
        when(tourServices.getTourNodeByParameter()).thenReturn(currentTourNode);

        List<Tour> tours = new ArrayList<Tour>() {{
            add(0, currentTour);
            add(1, someTour1);
            add(1, someTour2);
        }};
        when(tourServices.getToursByCategory(categoryName, identifier, true)).thenReturn(tours);

        // WHEN
        List<Tour> relatedTours = model.getRelatedToursByCategory(identifier);

        // THEN
        assertThat(relatedTours, not(hasItem(currentTour)));
        assertThat(relatedTours, hasItem(someTour1));
        assertThat(relatedTours, hasItem(someTour2));
    }

}
