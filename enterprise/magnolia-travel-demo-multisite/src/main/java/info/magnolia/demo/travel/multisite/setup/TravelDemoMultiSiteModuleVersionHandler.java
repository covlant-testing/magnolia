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
package info.magnolia.demo.travel.multisite.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapConditionally;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsInstallSamplesTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;

/**
 * Default {@link info.magnolia.module.ModuleVersionHandler} for travel-demo multi site example.
 */
public class TravelDemoMultiSiteModuleVersionHandler extends DefaultModuleVersionHandler {

    private final Task mappingAndDomainConfigurationTask = new ArrayDelegateTask("Add domain and mapping configuration to travel site definition in multisite", "",
            new BootstrapConditionally("Add domain configuration to travel site definition in multisite", "/info/magnolia/demo/travel/multisite/setup/config.modules.multisite.config.sites.travel.domains.xml"),
            new BootstrapConditionally("Add mapping configuration to travel site definition in multisite", "/info/magnolia/demo/travel/multisite/setup/config.modules.multisite.config.sites.travel.mappings.xml"));

    public TravelDemoMultiSiteModuleVersionHandler() {
        register(DeltaBuilder.update("1.1.5", "")
                .addTask(new BootstrapSingleResource("Re bootstrap sportstation site", "", "/mgnl-bootstrap/travel-demo-multisite/config.modules.multisite.config.sites.sportstation.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
                .addTask(mappingAndDomainConfigurationTask)
                .addTask(new NodeExistsDelegateTask("Remove sportstation-theme configuration from JCR", "/modules/site/config/themes/sportstation-theme",
                        new RemoveNodeTask("", "/modules/site/config/themes/sportstation-theme")))

                .addTask(new IsInstallSamplesTask("Re-Bootstrap website content for sportstation pages", "Re-bootstrap website content to account for all changes",
                        new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/travel-demo-multisite/website.sportstation.yaml")))
                .addTask(new BootstrapSingleResource("Re-Bootstrap virtual URI mapping for travel-demo multi-site module.", "", "/mgnl-bootstrap/travel-demo-multisite/config.modules.tours.virtualURIMapping.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<>();
        tasks.addAll(super.getExtraInstallTasks(installContext));
        tasks.add(new NodeExistsDelegateTask("Add mapping and domain configuration to travel site definition in multisite", "/modules/multisite/config/sites/travel", mappingAndDomainConfigurationTask));
        return tasks;
    }
}
