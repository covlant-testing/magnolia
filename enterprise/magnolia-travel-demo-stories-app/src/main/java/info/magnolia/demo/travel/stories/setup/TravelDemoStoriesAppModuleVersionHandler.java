/**
 * This file Copyright (c) 2017 Magnolia International
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
package info.magnolia.demo.travel.stories.setup;

import info.magnolia.demo.travel.setup.FolderBootstrapTask;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.OrderNodeBeforeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Version handler for the Magnolia travel demo stories app module.
 */
public class TravelDemoStoriesAppModuleVersionHandler extends DefaultModuleVersionHandler {

    private final Task orderPageNodes = new ArrayDelegateTask("Move the stories page before the 'about' page", "",
            new OrderNodeBeforeTask("", "", RepositoryConstants.WEBSITE, "/travel/stories", "about"));

    public TravelDemoStoriesAppModuleVersionHandler() {
        register(DeltaBuilder.update("1.1.5", "")
                .addTask(new FolderBootstrapTask("/mgnl-bootstrap-samples/travel-demo-stories-app/"))
                .addTask(orderPageNodes)
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> installTasks = new ArrayList<>();

        installTasks.add(orderPageNodes);

        return installTasks;
    }
}
