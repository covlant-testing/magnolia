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
package info.magnolia.demo.travel.personalization.setup;

import info.magnolia.cms.security.Permission;
import info.magnolia.demo.travel.setup.SetPageAsPublishedTask;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AddPermissionTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsInstallSamplesTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.personalization.variant.VariantManager;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DefaultModuleVersionHandler} for travel-demo personalization module.
 */
public class TravelDemoPersonalizationModuleVersionHandler extends DefaultModuleVersionHandler {
    // Add variant mixin to travel/contact page - so that adminCentral gives it the proper behaviour.
    private static final Task addMixinToTravelContact = new AddMixinTask("/travel/contact", RepositoryConstants.WEBSITE, VariantManager.HAS_VARIANT_MIXIN);

    public TravelDemoPersonalizationModuleVersionHandler() {
        register(DeltaBuilder.update("1.1.5", "")
                .addTask(new NodeExistsDelegateTask("Remove variants from home page", "Removes variants from home page. Variants have now moved to contacts page",  RepositoryConstants.WEBSITE, "/travel/variants", new RemoveNodeTask("", "",  RepositoryConstants.WEBSITE, "/travel/variants")))
                .addTask(new IsInstallSamplesTask("Re-Bootstrap website variants for contact pages", "Re-bootstrap website variants to account for all changes",
                        new BootstrapSingleResource("Re-Bootstrap variants", "", "/mgnl-bootstrap-samples/travel-demo-personalization/website.travel.contact.variants.yaml")))
                .addTask(new AddPermissionTask("Add permission", "travel-demo-admincentral", "personas", "/*", Permission.READ, true))
                .addTask(new RemoveMixinTask("/travel", RepositoryConstants.WEBSITE, VariantManager.HAS_VARIANT_MIXIN))

                .addTask(addMixinToTravelContact)
                .addTask(new SetPageAsPublishedTask("/travel", true))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();

        tasks.addAll(super.getExtraInstallTasks(installContext));

        // Add variant mixin to travel/contact page - so that adminCentral gives it the proper behaviour.
        tasks.add(addMixinToTravelContact);
        tasks.add(new AddPermissionTask("Add permission", "travel-demo-admincentral", "personas", "/*", Permission.READ, true));
        tasks.add(new SetPageAsPublishedTask("/travel", true));

        return tasks;
    }

}
