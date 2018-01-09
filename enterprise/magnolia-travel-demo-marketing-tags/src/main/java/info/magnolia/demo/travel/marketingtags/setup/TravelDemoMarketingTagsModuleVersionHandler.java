/**
 * This file Copyright (c) 2015-2018 Magnolia International
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
package info.magnolia.demo.travel.marketingtags.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;

import javax.jcr.ImportUUIDBehavior;

/**
 * Install the travel-demo-marketing-tags module.
 */
public class TravelDemoMarketingTagsModuleVersionHandler extends DefaultModuleVersionHandler {

    public TravelDemoMarketingTagsModuleVersionHandler() {
        // We re-bootstrap every config/content item upon update
        register(DeltaBuilder.update("1.1.5", "")
                .addTask(new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/travel-demo-marketing-tags/tags.Clicky-for-Travel-Demo.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
                .addTask(new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/travel-demo-marketing-tags/tags.Google-Analytics-for-Travel-Demo.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
        );
    }

}
