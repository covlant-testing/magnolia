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
package info.magnolia.demo.travel.setup;

import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * {@link RemoveNodeTask} that only removes the "default" site in multi site module when it points to the correct theme,
 * here {@code travel-demo-theme}, doesn't have the {@code class} property like any legacy site e.g. from STK and has
 * at least one available page template, here {@code travel-demo:pages/home} (which comes with the basic travel-demo
 * module).
 */
public class RemoveTravelDemoSiteFromMultiSite extends RemoveNodeTask {

    protected static final String PATH_TO_DEFAULT_SITE = "/modules/multisite/config/sites/default";

    public RemoveTravelDemoSiteFromMultiSite() {
        super("Remove old travel 'demo' site definition from multi site module", PATH_TO_DEFAULT_SITE);
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        final Session session = ctx.getConfigJCRSession();
        if (session.nodeExists(PATH_TO_DEFAULT_SITE)) {
            final Node siteNode = session.getNode(PATH_TO_DEFAULT_SITE);
            if (!siteNode.hasProperty("class") &&
                    "travel-demo-theme".equals(PropertyUtil.getString(siteNode, "theme/name")) &&
                    "travel-demo:pages/home".equals(PropertyUtil.getString(siteNode, "templates/availability/templates/home/id"))) {
                super.doExecute(ctx);
            }
        }
    }

}
