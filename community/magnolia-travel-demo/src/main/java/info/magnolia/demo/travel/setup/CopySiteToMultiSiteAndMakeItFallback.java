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

import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.CopyNodeTask;
import info.magnolia.module.delta.PropertyExistsDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.repository.RepositoryConstants;

/**
 * Task that moves site definition to multi site module and tries to make it the fallback site.
 */
public class CopySiteToMultiSiteAndMakeItFallback extends ArrayDelegateTask {

    protected static final String TRAVEL_DEMO_SITE = "/modules/travel-demo/config/travel";
    protected static final String MULTISITE_TRAVEL_SITE = "/modules/multisite/config/sites/travel";
    protected static final String MULTISITE_FALLBACK_SITE = "/modules/multisite/config/sites/fallback";

    public CopySiteToMultiSiteAndMakeItFallback() {
        this(false);
    }

    public CopySiteToMultiSiteAndMakeItFallback(final boolean override) {
        super("Copy site definition to multisite", "Copies site definition to multisite and makes it fallback site",
                new CopyNodeTask("Copy site definition to multisite", TRAVEL_DEMO_SITE, MULTISITE_TRAVEL_SITE, override),
                new PropertyExistsDelegateTask("Set travel demo as fallback site if possible", MULTISITE_FALLBACK_SITE, "extends",
                        new CheckAndModifyPropertyValueTask(MULTISITE_FALLBACK_SITE, "extends", "../default", "../travel"),
                        new SetPropertyTask(RepositoryConstants.CONFIG, MULTISITE_FALLBACK_SITE, "extends", "../travel")));
    }

}
