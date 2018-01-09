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
package info.magnolia.demo.travel.model;

import info.magnolia.demo.travel.definition.NavigationAreaDefinition;
import info.magnolia.demo.travel.user.UserLinksResolver;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.type.DefaultTemplateTypes;
import info.magnolia.templating.functions.TemplatingFunctions;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model for the navigation area of the travel demo.
 *
 * Even though this is a model for an area (see {@link AreaDefinition}) the content {@link Node} is the current page as
 * we have set <code>createAreaNode=false</code> (see {@link info.magnolia.rendering.template.AreaDefinition#getCreateAreaNode()}).
 */
public class NavigationAreaModel extends RenderingModelImpl<AreaDefinition> {

    private static final Logger log = LoggerFactory.getLogger(NavigationAreaModel.class);

    private static final String DEMO_ABOUT_TEMPLATE_SUBTYPE = "demo-about";

    private UserLinksResolver userLinksResolver;

    private final TemplatingFunctions templatingFunctions;

    @Inject
    public NavigationAreaModel(Node content, AreaDefinition definition, RenderingModel<?> parent, TemplatingFunctions templatingFunctions) {
        super(content, definition, parent);

        this.templatingFunctions = templatingFunctions;
    }

    public String getAboutDemoLink() {
        Node siteRoot = templatingFunctions.siteRoot(content);
        String link = null;
        try {
            List<Node> nodes = templatingFunctions.contentListByTemplateType(siteRoot, DefaultTemplateTypes.FEATURE, DEMO_ABOUT_TEMPLATE_SUBTYPE);
            if (nodes.size() > 0) {
                link = templatingFunctions.link(nodes.get(0));
            }
        } catch (RepositoryException e) {
            log.error("Could not get the '{}' page.", DEMO_ABOUT_TEMPLATE_SUBTYPE, e);
        }
        return link;
    }

    public String getUsername() throws RepositoryException {
        if (this.getUserLinksResolver() != null) {
            return this.getUserLinksResolver().getUsername();
        }
        return null;
    }

    public String getLogoutLink() throws RepositoryException {
        if (this.getUserLinksResolver() != null) {
            return this.getUserLinksResolver().getLogoutLink();
        }
        return null;
    }

    public String getLoginPageLink() throws RepositoryException {
        if (this.getUserLinksResolver() != null) {
            return this.getUserLinksResolver().getLoginPageLink();
        }
        return null;
    }

    public String getRegistrationPageLink() throws RepositoryException {
        if (this.getUserLinksResolver() != null) {
            return this.getUserLinksResolver().getRegistrationPageLink();
        }
        return null;
    }

    public String getProfilePageLink() throws RepositoryException {
        if (this.getUserLinksResolver() != null) {
            return this.getUserLinksResolver().getProfilePageLink();
        }
        return null;
    }

    private UserLinksResolver getUserLinksResolver() throws RepositoryException {
        if (userLinksResolver == null) {
            if (this.getDefinition() instanceof NavigationAreaDefinition) {
                for (UserLinksResolver userLinksResolver : ((NavigationAreaDefinition) this.getDefinition()).getUserLinksResolvers()) {
                    if (userLinksResolver.useForCurrentPage()) {
                        this.userLinksResolver = userLinksResolver;
                        break;
                    }
                }
            }
        }
        return userLinksResolver;
    }

    public Locale getLocale(String language) {
        return LocaleUtils.toLocale(language);
    }
}
