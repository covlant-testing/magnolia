/**
 * This file Copyright (c) 2015-2024 Magnolia International
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

import static info.magnolia.repository.RepositoryConstants.*;

import info.magnolia.demo.travel.setup.AddDemoTravelPermissionTask;
import info.magnolia.demo.travel.setup.SetPageAsPublishedTask;
import info.magnolia.demo.travel.tours.TourTemplatingFunctions;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AddRoleToUserTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.OrderFilterBeforeTask;
import info.magnolia.module.delta.OrderNodeBeforeTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.rendering.module.setup.InstallRendererContextAttributeTask;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link DefaultModuleVersionHandler} of the {@link info.magnolia.demo.travel.tours.ToursModule}.
 */
public class ToursModuleVersionHandler extends DefaultModuleVersionHandler {

    protected static final String TRAVEL_DEMO_TOUR_EDITOR_ROLE = "travel-demo-tour-editor";
    protected static final String DAM_PERMISSIONS_ROLES = "/modules/dam-app/apps/assets/permissions/roles";

    private final Task orderPageNodes = new ArrayDelegateTask("Order travel pages before the 'about' page", "",
            new OrderNodeBeforeTask("", "", WEBSITE, "/travel/tour-type", "about"),
            new OrderNodeBeforeTask("", "", WEBSITE, "/travel/destination", "about"),
            new OrderNodeBeforeTask("", "", WEBSITE, "/travel/tour", "about"));

    private final Task reorderVirtualUriAndI18nFilters = new OrderFilterBeforeTask("virtualURI", new String[] { "i18n" });

    public ToursModuleVersionHandler() {
        register(DeltaBuilder.checkPrecondition("1.6", "2.0"));

        register(DeltaBuilder.update("1.6.4", "")
                .addTask(new RemoveNodeTask("Remove virtualUriMappings from JCR configuration", "/modules/tours/virtualUriMappings"))
                .addTask(new NodeExistsDelegateTask("Reorder virtualURI filter before i18n filter", "/server/filters/virtualURI", reorderVirtualUriAndI18nFilters))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<>(super.getExtraInstallTasks(installContext));

        tasks.add(new InstallRendererContextAttributeTask("rendering", "freemarker", "tourfn", TourTemplatingFunctions.class.getName()));

        /* Order bootstrapped pages accordingly */
        tasks.add(orderPageNodes);
        tasks.add(new OrderNodeBeforeTask("Order careers zeroFive node before zeroFix", "Order careers zeroFive node before zeroFix", WEBSITE, "/travel/about/careers/main/05", "06"));
        tasks.add(new OrderNodeBeforeTask("Place Tour Finder component on travel-demo home page", "Place Tour Finder component on the correct position on the home page.", WEBSITE, "/travel/main/01", "00"));

        /* Add travel-demo-base role to user anonymous */
        tasks.add(new AddRoleToUserTask("Adds role 'travel-demo-base' to user 'anonymous'", "anonymous", "travel-demo-base"));
        tasks.add(new AddDemoTravelPermissionTask(DAM_PERMISSIONS_ROLES, TRAVEL_DEMO_TOUR_EDITOR_ROLE));

        tasks.add(new SetPageAsPublishedTask("/travel", true));
        tasks.add(reorderVirtualUriAndI18nFilters);
        return tasks;
    }

}
