/**
 * This file Copyright (c) 2016-2024 Magnolia International
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
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsInstallSamplesTask;
import info.magnolia.module.delta.RemoveNodesTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;

/**
 * {@link DefaultModuleVersionHandler} for travel-demo personalization module.
 */
public class TravelDemoComponentPersonalizationModuleVersionHandler extends DefaultModuleVersionHandler {

    public TravelDemoComponentPersonalizationModuleVersionHandler() {
        register(DeltaBuilder.update("1.2.2", "")
                .addTask(new IsInstallSamplesTask("Re-Bootstrap website and configuration specific to personalization", "Re-Bootstraps website and configuration specific to personalization to account for all changes", new ArrayDelegateTask("", "",
                        new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/travel-demo-component-personalization/website.travel.main.0.yaml"),
                        new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/travel-demo-component-personalization/website.travel.main.00.yaml"),
                        new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/travel-demo-component-personalization/config.modules.personalization-traits.traits.cookies.ruleField.fields.tourType.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING),
                        new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/travel-demo-component-personalization/config.modules.personalization-traits.traits.cookies.ruleField.options.tourType.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING),
                        new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/travel-demo-component-personalization/config.modules.personalization-traits.traits.cookies.valueField.options.tourTypeAny.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING)
                )))
                .addTask(new AddPermissionTask("Add permission", "travel-demo-admincentral", "personas", "/*", Permission.READ, true))
                .addTask(new SetPageAsPublishedTask("/travel", true))
                .addTask(new ArrayDelegateTask("Remove tour types from traits.", "",
                        new RemoveNodesTask("", RepositoryConstants.CONFIG, Arrays.asList(
                                "/modules/personalization-traits/traits/cookies/valueField/options/tourTypeActive",
                                "/modules/personalization-traits/traits/cookies/valueField/options/tourTypeOffbeat",
                                "/modules/personalization-traits/traits/cookies/valueField/options/tourTypeCultural"), false)))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<Task>();

        tasks.addAll(super.getExtraInstallTasks(installContext));

        // Add variant mixin to travel/contact page - so that adminCentral gives it the proper behaviour.
        tasks.add(new AddPermissionTask("Add permission", "travel-demo-admincentral", "personas", "/*", Permission.READ, true));
        tasks.add(new SetPageAsPublishedTask("/travel", true));

        return tasks;
    }

}
