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
package info.magnolia.demo.travel.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.IsAdminInstanceDelegateTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.google.common.collect.Lists;

/**
 * {@link DefaultModuleVersionHandler} for travel demo module.
 */
public class TravelDemoModuleVersionHandler extends DefaultModuleVersionHandler {

    private static final String DEFAULT_URI_NODEPATH = "/modules/ui-admincentral/virtualUriMappings/default";
    private static final String DEFAULT_URI = "redirect:/travel.html";

    private final Task setDefaultUriOnPublicInstance = new IsAdminInstanceDelegateTask("Set default URI to home page", String.format("Set default URI to point to '%s'", DEFAULT_URI), null,
                    new NodeExistsDelegateTask("", DEFAULT_URI_NODEPATH,
                            new SetPropertyTask(RepositoryConstants.CONFIG, DEFAULT_URI_NODEPATH, "toUri", DEFAULT_URI),
                            new WarnTask("Set default URI to home page", "Could not set default URI to home travel page, default mapping was not found.")));

    private final Task setupAccessPermissionsForDemoUsers = new SetupRoleBasedAccessPermissionsTask("Deny access permissions to apps", "Deny access permissions to Contacts app, Web Dev group, Set Up group for travel-demo-admincentral role",
            Lists.newArrayList("travel-demo-admincentral"), false, "/modules/contacts/apps/contacts", "/modules/ui-admincentral/config/appLauncherLayout/groups/stk", "/modules/ui-admincentral/config/appLauncherLayout/groups/manage");

    private final Task setupTargetAppGroupAccessPermissions = new SetupRoleBasedAccessPermissionsTask("Allow access to Target app group", "Allow access to Target app group only to travel-demo-editor and travel-demo-publisher roles",
            Lists.newArrayList("travel-demo-editor", "travel-demo-publisher"), true, "/modules/ui-admincentral/config/appLauncherLayout/groups/target");

    private final Task addUsernameValidation = new AbstractRepositoryTask("Add username validation", "") {
        @Override
        protected void doExecute(InstallContext ctx) throws RepositoryException {
            final Session session = ctx.getJCRSession(RepositoryConstants.WEBSITE);
            final Node node = session.getNode("/travel/members/registration/main/0/fieldsets/0/fields/0");
            if (node.hasProperty("validation")) {
                node.getProperty("validation").remove();
            }
            node.setProperty("validation", new String[] { "username" });
        }
    };

    private final Task addEmailValidation = new AbstractRepositoryTask("Add unique email validation", "") {
        @Override
        protected void doExecute(InstallContext ctx) throws RepositoryException {
            final Session session = ctx.getJCRSession(RepositoryConstants.WEBSITE);
            final Node node = session.getNode("/travel/members/registration/main/0/fieldsets/0/fields/02");
            if (node.hasProperty("validation")) {
                node.getProperty("validation").remove();
            }
            node.setProperty("validation", new String[] { "email", "uniqueEmail" });
        }
    };

    private final InstallPurSamplesTask installPurSamples = new InstallPurSamplesTask();

    public TravelDemoModuleVersionHandler() {
        register(DeltaBuilder.checkPrecondition("1.6", "2.0"));

        register(DeltaBuilder.update("1.6.4", "")
                .addTask(new RemoveNodeTask("Remove i18n filter bypass", "/server/filters/i18n/bypasses"))
        );
        register(DeltaBuilder.update("1.6.10", "")
                .addTask(new NodeExistsDelegateTask("Add unique username validation to the username field", "", RepositoryConstants.WEBSITE, "/travel/members/registration/main/0/fieldsets/0/fields/0", addUsernameValidation))
                .addTask(new NodeExistsDelegateTask("Add unique email validation to the email field", "", RepositoryConstants.WEBSITE, "/travel/members/registration/main/0/fieldsets/0/fields/02", addEmailValidation))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        final List<Task> tasks = new ArrayList<>(super.getExtraInstallTasks(installContext));
        tasks.add(setDefaultUriOnPublicInstance);

        tasks.add(installPurSamples);

        tasks.add(new SetupDemoRolesAndGroupsTask());
        tasks.add(setupAccessPermissionsForDemoUsers);
        tasks.add(setupTargetAppGroupAccessPermissions);
        return tasks;
    }

}
