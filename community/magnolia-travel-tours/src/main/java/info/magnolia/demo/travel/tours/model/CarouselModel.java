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
import info.magnolia.demo.travel.tours.service.Tour;
import info.magnolia.demo.travel.tours.service.TourServices;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.RenderableDefinition;

import java.util.LinkedList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Model class for the carousel. Gets the linked tours from current content node and creates {@link Tour} objects.
 *
 * @param <RD> The {@link RenderableDefinition} of the model.
 */
public class CarouselModel<RD extends RenderableDefinition> extends RenderingModelImpl<RD> {

    private static final Logger log = LoggerFactory.getLogger(CarouselModel.class);

    public static final String PROPERTY_NAME_TOURS = "tours";

    private final TourServices tourServices;

    @Inject
    public CarouselModel(Node content, RD definition, RenderingModel<?> parent, TourServices tourServices) {
        super(content, definition, parent);
        this.tourServices = tourServices;
    }

    public List<Tour> getTours() {
        final List<Tour> tours = new LinkedList<Tour>();

        final Object object = PropertyUtil.getPropertyValueObject(content, PROPERTY_NAME_TOURS);
        if (object instanceof List) {
            final List<String> results = (List<String>) object;
            for (String identifier : results) {
                try {
                    final Tour tour = getTour(identifier);
                    if (tour != null) {
                        tours.add(tour);
                    }
                } catch (RepositoryException e) {
                    log.error("Could not retrieve linked tour with identifier [{}].", identifier, e);
                }
            }
        }

        return tours;
    }

    private Tour getTour(String identifier) throws RepositoryException {
        final Session session = MgnlContext.getJCRSession(ToursModule.TOURS_REPOSITORY_NAME);
        final Node tourNode = session.getNodeByIdentifier(identifier);

        return tourServices.marshallTourNode(tourNode);
    }

}
