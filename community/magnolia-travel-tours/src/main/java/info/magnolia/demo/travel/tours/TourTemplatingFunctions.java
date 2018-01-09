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
package info.magnolia.demo.travel.tours;

import info.magnolia.demo.travel.tours.service.Category;
import info.magnolia.demo.travel.tours.service.Tour;
import info.magnolia.demo.travel.tours.service.TourServices;
import info.magnolia.jcr.util.ContentMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;

/**
 * Useful functions for templating.
 */
@Singleton
public class TourTemplatingFunctions {

    private final TourServices tourServices;

    @Inject
    public TourTemplatingFunctions(TourServices tourServices) {
        this.tourServices = tourServices;
    }

    public Category getCategoryByUrl() {
        return tourServices.getCategoryByUrl();
    }

    /**
     * Returns the link to a tour type.
     *
     * <p>Will use given {@link ContentMap} to find feature page of type
     * {@link ToursModule#TEMPLATE_SUB_TYPE_TOUR_OVERVIEW} to link to.</p>
     */
    public String getTourTypeLink(ContentMap contentMap, String tourTypeName) {
        return getTourTypeLink(contentMap.getJCRNode(), tourTypeName);
    }

    /**
     * Returns the link to a tour type.
     *
     * <p>Will use given {@link Node} to find feature page of type
     * {@link ToursModule#TEMPLATE_SUB_TYPE_TOUR_OVERVIEW} to link to.</p>
     */
    public String getTourTypeLink(Node content, String tourTypeName) {
        return tourServices.getCategoryLink(content, tourTypeName, ToursModule.TEMPLATE_SUB_TYPE_TOUR_OVERVIEW);
    }

    /**
     * Returns the link to a tour type.
     *
     * <p>Will use given {@link ContentMap} to find feature page of type
     * {@link ToursModule#TEMPLATE_SUB_TYPE_DESTINATION_OVERVIEW} to link to.</p>
     */
    public String getDestinationLink(ContentMap contentMap, String destinationName) {
        return getDestinationLink(contentMap.getJCRNode(), destinationName);
    }

    /**
     * Returns the link to a tour type.
     *
     * <p>Will use given {@link Node} to find feature page of type
     * {@link ToursModule#TEMPLATE_SUB_TYPE_DESTINATION_OVERVIEW} to link to.</p>
     */
    public String getDestinationLink(Node content, String destinationName) {
        return tourServices.getCategoryLink(content, destinationName, ToursModule.TEMPLATE_SUB_TYPE_DESTINATION_OVERVIEW);
    }

    public String getTourLink(ContentMap tourContentMap) {
        return getTourLink(tourContentMap.getJCRNode());
    }

    public String getTourLink(Node tourNode) {
        return tourServices.getTourLink(tourNode);
    }

    /**
     * Allows marshalling of tour node from templates. Can be useful when accessing {@link Category categories}.
     */
    public Tour marshallTourNode(Node tourNode) {
        return tourServices.marshallTourNode(tourNode);
    }

    public Tour marshallTourNode(ContentMap tourContentMap) {
        return marshallTourNode(tourContentMap.getJCRNode());
    }
}
