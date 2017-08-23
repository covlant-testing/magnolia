/**
 * This file Copyright (c) 2015-2017 Magnolia International
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
package info.magnolia.demo.travel.tours.setup;

import info.magnolia.demo.travel.setup.AddDemoTravelPermissionTask;
import info.magnolia.demo.travel.setup.CopySiteToMultiSiteAndMakeItFallback;
import info.magnolia.demo.travel.setup.FolderBootstrapTask;
import info.magnolia.demo.travel.setup.SetPageAsPublishedTask;
import info.magnolia.demo.travel.tours.TourTemplatingFunctions;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AddRoleToUserTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CopyNodeTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsInstallSamplesTask;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.OrderNodeBeforeTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.rendering.module.setup.InstallRendererContextAttributeTask;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;

/**
 * {@link DefaultModuleVersionHandler} of the {@link info.magnolia.demo.travel.tours.ToursModule}.
 */
public class ToursModuleVersionHandler extends DefaultModuleVersionHandler {

    protected static final String TRAVEL_DEMO_TOUR_EDITOR_ROLE = "travel-demo-tour-editor";
    protected static final String DAM_PERMISSIONS_ROLES = "/modules/dam-app/apps/assets/permissions/roles";

    private final Task orderPageNodes = new ArrayDelegateTask("Order travel pages before the 'about' page", "",
            new OrderNodeBeforeTask("", "", RepositoryConstants.WEBSITE, "/travel/tour-type", "about"),
            new OrderNodeBeforeTask("", "", RepositoryConstants.WEBSITE, "/travel/destination", "about"),
            new OrderNodeBeforeTask("", "", RepositoryConstants.WEBSITE, "/travel/tour", "about"));

    public ToursModuleVersionHandler() {
        register(DeltaBuilder.update("1.1.5", "")
                .addTask(new FolderBootstrapTask("/mgnl-bootstrap/tours/travel-demo/"))
                .addTask(new IsInstallSamplesTask("Re-Bootstrap website content for travel pages", "Re-bootstrap website content to account for all changes",
                        new ArrayDelegateTask("",
                                new FolderBootstrapTask("/mgnl-bootstrap-samples/tours/website/"),
                                new ArrayDelegateTask("Re-Bootstrap category content for travel tours", "Re-bootstrap category content to account for all changes",
                                        new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/tours/category.destinations.yaml"),
                                        new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/tours/category.tour-types.yaml")),
                                new BootstrapSingleResource("Re bootstrap tours content", "", "/mgnl-bootstrap-samples/tours/tours.magnolia-travels.yaml"),
                                new FolderBootstrapTask("/mgnl-bootstrap-samples/tours/assets/"),
                                new OrderNodeBeforeTask("Order careers zeroFive node before zeroFix", "", RepositoryConstants.WEBSITE, "/travel/about/careers/main/05", "06"))))
                .addTask(new BootstrapSingleModuleResource("config.modules.tours.apps.tourCategories.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
                .addTask(new BootstrapSingleModuleResource("config.modules.tours.apps.tours.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
                .addTask(new IsModuleInstalledOrRegistered("Enable travel site in multisite configuration", "multisite",
                        new NodeExistsDelegateTask("Check whether multisite can be enabled for travel demo", "/modules/travel-demo/config/travel",
                                new CopySiteToMultiSiteAndMakeItFallback(true))))
                .addTask(new NodeExistsDelegateTask("Add permission for access to Dam app", DAM_PERMISSIONS_ROLES,
                        new SetPropertyTask(RepositoryConstants.CONFIG, DAM_PERMISSIONS_ROLES, TRAVEL_DEMO_TOUR_EDITOR_ROLE, TRAVEL_DEMO_TOUR_EDITOR_ROLE)))

                .addTask(orderPageNodes)
                .addTask(new SetPageAsPublishedTask("/travel", true))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<>();

        tasks.addAll(super.getExtraInstallTasks(installContext));

        tasks.add(new InstallRendererContextAttributeTask("rendering", "freemarker", "tourfn", TourTemplatingFunctions.class.getName()));

        /* Order bootstrapped pages accordingly */
        tasks.add(orderPageNodes);
        tasks.add(new OrderNodeBeforeTask("Order careers zeroFive node before zeroFix", "", RepositoryConstants.WEBSITE, "/travel/about/careers/main/05", "06"));

        /* Add travel-demo-base role to user anonymous */
        tasks.add(new AddRoleToUserTask("Adds role 'travel-demo-base' to user 'anonymous'", "anonymous", "travel-demo-base"));
        tasks.add(new AddDemoTravelPermissionTask(DAM_PERMISSIONS_ROLES, TRAVEL_DEMO_TOUR_EDITOR_ROLE));

        tasks.add(new IsModuleInstalledOrRegistered("Copy template availability and navigation areas from site definition to multisite module", "multisite",
                new ArrayDelegateTask("",
                        new CopyNodeTask("Copy tour template",
                                "/modules/travel-demo/config/travel/templates/availability/templates/tour", "/modules/multisite/config/sites/travel/templates/availability/templates/tour", false),
                        new CopyNodeTask("Copy categoryOverview template",
                                "/modules/travel-demo/config/travel/templates/availability/templates/categoryOverview", "/modules/multisite/config/sites/travel/templates/availability/templates/categoryOverview", false),
                        new CopyNodeTask("Copy categoryOverview template",
                                "/modules/travel-demo/config/travel/templates/availability/templates/destinationCatOverview", "/modules/multisite/config/sites/travel/templates/availability/templates/destinationCatOverview", false))));
        tasks.add(new SetPageAsPublishedTask("/travel", true));
        return tasks;
    }

}
