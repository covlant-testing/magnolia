/**
 * This file Copyright (c) 2015-2024 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.demo.travel.multisite.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.RemoveNodeTask;

/**
 * Default {@link info.magnolia.module.ModuleVersionHandler} for travel-demo multi site example.
 */
public class TravelDemoMultiSiteModuleVersionHandler extends DefaultModuleVersionHandler {

    public TravelDemoMultiSiteModuleVersionHandler() {
        register(DeltaBuilder.checkPrecondition("1.6", "2.0"));

        register(DeltaBuilder.update("1.6.4", "")
                .addTask(new RemoveNodeTask("Remove virtualUriMappings from JCR configuration", "/modules/tours/virtualUriMappings"))
        );
    }
}
