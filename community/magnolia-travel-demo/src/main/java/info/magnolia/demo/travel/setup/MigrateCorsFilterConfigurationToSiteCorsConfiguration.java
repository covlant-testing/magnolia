/**
 * This file Copyright (c) 2020-2024 Magnolia International
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

import info.magnolia.cors.AbstractCorsFilter.Headers;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.JCRMgnlPropertiesFilteringNodeWrapper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;

/**
 * Migrate demo CORS headers into the new CORS configuration.
 */
public class MigrateCorsFilterConfigurationToSiteCorsConfiguration extends AbstractRepositoryTask {

    private static final String ADD_CORS_HEADERS_FILTER_PATH = "/server/filters/addCORSHeaders";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = Headers.ACCESS_CONTROL_ALLOW_HEADERS.getName();
    private static final String ACCESS_CONTROL_ALLOW_METHODS = Headers.ACCESS_CONTROL_ALLOW_METHODS.getName();
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = Headers.ACCESS_CONTROL_ALLOW_ORIGIN.getName();
    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = Headers.ACCESS_CONTROL_ALLOW_CREDENTIALS.getName();

    private final String absSitePath;

    public MigrateCorsFilterConfigurationToSiteCorsConfiguration(final String absSitePath) {
        super("Migrate CORS filter headers", "Migrate CORS Filter headers to new CORS configuration in travel site");
        this.absSitePath = absSitePath;
    }

    @Override
    protected void doExecute(final InstallContext installContext) throws RepositoryException, TaskExecutionException {
        final Session session = installContext.getConfigJCRSession();
        if (!session.nodeExists(absSitePath)) {
            installContext.warn(String.format("Cannot migrate CORS configuration into the site [%s]. Site does not exist.", absSitePath));
            return;
        }
        final Node siteNode = session.getNode(absSitePath);
        final Node corsNode = NodeUtil.createPath(siteNode, "cors/travel", NodeTypes.ContentNode.NAME);
        final Node restUri = NodeUtil.createPath(corsNode, "uris/rest", NodeTypes.ContentNode.NAME);
        restUri.setProperty("patternString", "/.rest/*");
        if (session.nodeExists(ADD_CORS_HEADERS_FILTER_PATH)) {
            final Node filterNode = session.getNode(ADD_CORS_HEADERS_FILTER_PATH);
            if (filterNode.hasNode("headers")) {
                final PropertyIterator iterator = new JCRMgnlPropertiesFilteringNodeWrapper(filterNode.getNode("headers")).getProperties();
                while (iterator.hasNext()) {
                    final Property property = iterator.nextProperty();
                    final String propertyName = property.getName();
                    if (propertyName.equals(ACCESS_CONTROL_ALLOW_ORIGIN)) {
                        final Node allowedOrigins = corsNode.addNode("allowedOrigins", NodeTypes.ContentNode.NAME);
                        setNodeProperties(allowedOrigins, property.getString());
                    } else if (propertyName.equals(ACCESS_CONTROL_ALLOW_HEADERS)) {
                        final Node allowedHeaders = corsNode.addNode("allowedHeaders", NodeTypes.ContentNode.NAME);
                        setNodeProperties(allowedHeaders, property.getString());
                    } else if (propertyName.equals(ACCESS_CONTROL_ALLOW_METHODS)) {
                        final Node allowedMethods = corsNode.addNode("allowedMethods", NodeTypes.ContentNode.NAME);
                        setNodeProperties(allowedMethods, property.getString());
                    } else if (propertyName.equals(ACCESS_CONTROL_ALLOW_CREDENTIALS)) {
                        corsNode.setProperty("supportsCredentials", property.getBoolean());
                    }
                }
            }
        }
    }

    private void setNodeProperties(final Node node, final String value) throws RepositoryException {
        final String[] values = StringUtils.split(value, ',');
        for (int i = 0; i < values.length; i++) {
            node.setProperty(String.valueOf(i), StringUtils.trim(values[i]));
        }
    }
}
