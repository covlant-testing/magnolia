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

import info.magnolia.dam.api.Asset;

import java.util.List;

/**
 * Simple POJO for wrapping a tour node.
 */
public class Tour {

    public static final String PROPERTY_NAME_DISPLAY_NAME = "name";
    public static final String PROPERTY_NAME_DESCRIPTION = "description";
    public static final String PROPERTY_NAME_BODY = "body";
    public static final String PROPERTY_NAME_IMAGE = "image";
    public static final String PROPERTY_NAME_LOCATION = "location";
    public static final String PROPERTY_NAME_DURATION = "duration";
    public static final String PROPERTY_NAME_AUTHOR = "author";

    public static final String PROPERTY_NAME_TOUR_TYPES_CATEGORY = "tourTypes";
    public static final String PROPERTY_NAME_DESTINATION = "destination";

    private String name;
    private String description;
    private String body;
    private String link;

    private String location;
    private String duration;
    private String author;
    private String identifier;

    private Asset image;

    private List<Category> tourTypes;
    private List<Category> destinations;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Asset getImage() {
        return image;
    }

    public void setImage(Asset image) {
        this.image = image;
    }

    public List<Category> getTourTypes() {
        return tourTypes;
    }

    public void setTourTypes(List<Category> tourTypes) {
        this.tourTypes = tourTypes;
    }

    public List<Category> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<Category> destinations) {
        this.destinations = destinations;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}