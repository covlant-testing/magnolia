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

/**
 * Simple POJO for wrapping categories.
 */
public class Category {

    public static final String PROPERTY_NAME_DISPLAY_NAME = "displayName";
    public static final String PROPERTY_NAME_DESCRIPTION = "description";
    public static final String PROPERTY_NAME_IMAGE = "image";
    public static final String PROPERTY_NAME_ICON = "icon";
    public static final String PROPERTY_NAME_BODY = "body";

    private String name;
    private String identifier;
    private String link;
    private Asset image;
    private Asset icon;
    private String description;
    private String body;
    private String nodeName;

    public Category() {
    }

    public Category(String name, String identifier) {
        this.name = name;
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getLink() {
        return link;
    }

    public Asset getImage() {
        return image;
    }

    public Asset getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public String getBody() {
        return body;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setImage(Asset image) {
        this.image = image;
    }

    public void setIcon(Asset icon) {
        this.icon = icon;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}