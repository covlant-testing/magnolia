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

import info.magnolia.demo.travel.tours.model.definition.TourCategoryTemplateDefinition;
import info.magnolia.demo.travel.tours.service.Category;
import info.magnolia.demo.travel.tours.service.Tour;
import info.magnolia.demo.travel.tours.service.TourServices;
import info.magnolia.rendering.model.RenderingModel;

import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Model for getting related Tours based on type- and destination-category.
 *
 * @param <RD> The {@link TourCategoryTemplateDefinition} of the model.
 */
public class RelatedToursModel<RD extends TourCategoryTemplateDefinition> extends TourListModel<RD> {

    private static final Logger log = LoggerFactory.getLogger(RelatedToursModel.class);

    @Inject
    public RelatedToursModel(Node content, RD definition, RenderingModel<?> parent, TourServices tourServices) {
        super(content, definition, parent, tourServices);
    }

    public List<Category> getRelatedCategoriesByParameter() {
        return getTourServices().getRelatedCategoriesByParameter();
    }

    /**
     * Filters the current tour from the category.
     */
    public List<Tour> getRelatedToursByCategory(String identifier) {
        List<Tour> relatedTours = Lists.newArrayList();

        try {
            final String currentIdentifier = getTourServices().getTourNodeByParameter().getIdentifier();
            List<Tour> tours = getTourServices().getToursByCategory(definition.getCategory(), identifier, true);

            relatedTours = Lists.newArrayList(Iterables.filter(tours, new Predicate<Tour>() {
                @Override
                public boolean apply(Tour tour) {
                    return !currentIdentifier.equals(tour.getIdentifier());
                }
            }));
        } catch (RepositoryException e) {
            log.error("Could not retrieve identifier for the current tour.", e);
        }

        return relatedTours;
    }
}