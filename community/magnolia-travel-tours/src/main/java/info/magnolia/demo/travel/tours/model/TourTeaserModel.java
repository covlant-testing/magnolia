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

import info.magnolia.context.MgnlContext;
import info.magnolia.demo.travel.tours.ToursModule;
import info.magnolia.demo.travel.tours.service.Category;
import info.magnolia.demo.travel.tours.service.Tour;
import info.magnolia.demo.travel.tours.service.TourServices;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.categorization.CategorizationModule;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.RenderableDefinition;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model for displaying the 'featured' tourTypes on the home page. reads the 'tourTypes' property from the content
 * and resolves the categories.
 *
 * @param <RD> Renderable definition.
 */
public class TourTeaserModel<RD extends RenderableDefinition> extends RenderingModelImpl<RD> {

    private static final Logger log = LoggerFactory.getLogger(TourTeaserModel.class);

    private final TourServices tourServices;

    @Inject
    public TourTeaserModel(Node content, RD definition, RenderingModel<?> parent, TourServices tourServices) {
        super(content, definition, parent);
        this.tourServices = tourServices;
    }

    public List<Category> getTours() {
        final List<Category> categories = new LinkedList<Category>();

        final Object object = PropertyUtil.getPropertyValueObject(content, Tour.PROPERTY_NAME_TOUR_TYPES_CATEGORY);
        if (object instanceof List) {
            List<String> results = (List<String>) object;
            for (String identifier : results) {
                try {
                    final Category category = getCategory(identifier);
                    if (category != null) {
                        categories.add(category);
                    }
                } catch (RepositoryException e) {
                    log.error("Could not retrieve linked tour.", e);
                }
            }
        }

        return categories;
    }

    private Category getCategory(String identifier) throws RepositoryException {
        final Session session = MgnlContext.getJCRSession(CategorizationModule.CATEGORIZATION_WORKSPACE);
        final Node categoryNode = session.getNodeByIdentifier(identifier);

        final Category category = tourServices.marshallCategoryNode(categoryNode);
        if (category != null) {
            String link = tourServices.getCategoryLink(content, categoryNode.getName(), ToursModule.TEMPLATE_SUB_TYPE_TOUR_OVERVIEW);
            category.setLink(link);
        }

        return category;
    }


}
