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

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.CheckAndModifyPropertyValueTask;
import info.magnolia.module.delta.CopyNodeTask;
import info.magnolia.module.delta.CreateNodePathTask;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.HasPropertyDelegateTask;
import info.magnolia.module.delta.IsAdminInstanceDelegateTask;
import info.magnolia.module.delta.IsInstallSamplesTask;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.RemovePropertyTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.ValueOfPropertyDelegateTask;
import info.magnolia.module.delta.WarnTask;
import info.magnolia.module.site.setup.DefaultSiteExistsDelegateTask;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.templating.models.setup.MigrateTemplatesToMTK2;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;
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

    private final Task setDefaultUriOnPublicInstance = new ValueOfPropertyDelegateTask("Set default URI to home travel page, when current site is travel site", "/modules/site/config/site", "extends", "/modules/travel-demo/config/travel", false,
            new IsAdminInstanceDelegateTask("Set default URI to home page", String.format("Set default URI to point to '%s'", DEFAULT_URI), null,
                    new NodeExistsDelegateTask("", DEFAULT_URI_NODEPATH,
                            new SetPropertyTask(RepositoryConstants.CONFIG, DEFAULT_URI_NODEPATH, "toUri", DEFAULT_URI),
                            new WarnTask("Set default URI to home page", "Could not set default URI to home travel page, default mapping was not found."))));

    private final Task setupTravelSiteAsActiveSite = new NodeExistsDelegateTask("Set travel demo as an active site", "/modules/site/config/site",
            new HasPropertyDelegateTask("Check extends property and update or create it", "/modules/site/config/site", "extends",
                    new CheckAndModifyPropertyValueTask("/modules/site/config/site", "extends", "/modules/standard-templating-kit/config/site", "/modules/travel-demo/config/travel"),
                    new DefaultSiteExistsDelegateTask("", "",
                            new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/site/config/site", "extends", "/modules/travel-demo/config/travel"))),
            new ArrayDelegateTask("",
                    new CreateNodeTask("", "/modules/site/config", "site", NodeTypes.ContentNode.NAME),
                    new SetPropertyTask(RepositoryConstants.CONFIG, "/modules/site/config/site", "extends", "/modules/travel-demo/config/travel")
            ));

    private final Task copySiteToMultiSiteAndMakeItFallback = new CopySiteToMultiSiteAndMakeItFallback();

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
        register(DeltaBuilder.update("1.2", "")
                .addTask(new IsInstallSamplesTask("Re-Bootstrap website content for travel pages", "Re-bootstrap website content to account for all changes",
                        new ArrayDelegateTask("",
                                new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/travel-demo/website.travel.yaml"),
                                new BootstrapSingleResource("", "", "/mgnl-bootstrap-samples/travel-demo/dam.travel-demo.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))))
                // We re-bootstrap twice because a simple (and single) re-bootstrap (using ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING) would NOT
                // "move" an existing site definition (which might actually exist from a previous version) in the site module
                .addTask(new BootstrapSingleModuleResource("config.modules.travel-demo.config.travel.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING))
                .addTask(new BootstrapSingleModuleResource("config.modules.travel-demo.config.travel.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW))

                .addTask(new NodeExistsDelegateTask("Remove travel-demo-theme configuration from JCR", "/modules/site/config/themes/travel-demo-theme",
                        new RemoveNodeTask("", "/modules/site/config/themes/travel-demo-theme")))

                .addTask(setupTravelSiteAsActiveSite)
                .addTask(setDefaultUriOnPublicInstance)

                .addTask(installPurSamples)

                .addTask(new IsModuleInstalledOrRegistered("Enable travel site in multisite configuration", "multisite",
                        new NodeExistsDelegateTask("Check whether multisite can be enabled for travel demo", "/modules/travel-demo/config/travel",
                                new NodeExistsDelegateTask("Check whether travel demo was already copied in a previous version", "/modules/multisite/config/sites/default",
                                        new ArrayDelegateTask("", "",
                                                new RemoveTravelDemoSiteFromMultiSite(),
                                                copySiteToMultiSiteAndMakeItFallback),
                                        new NodeExistsDelegateTask("Check whether travel node in multisite does not exist.", "/modules/multisite/config/sites/travel", null, copySiteToMultiSiteAndMakeItFallback)))))

                .addTask(new NodeExistsDelegateTask("Configure permissions for access to Pages app", "/modules/pages/apps/pages",
                        new ArrayDelegateTask("Configure permissions for access to Pages app",
                                new CreateNodePathTask("", "", RepositoryConstants.CONFIG, "/modules/pages/apps/pages/permissions/roles", NodeTypes.ContentNode.NAME),
                                new SetPropertyTask(RepositoryConstants.CONFIG, SetupDemoRolesAndGroupsTask.PAGES_PERMISSIONS_ROLES, SetupDemoRolesAndGroupsTask.TRAVEL_DEMO_EDITOR_ROLE, SetupDemoRolesAndGroupsTask.TRAVEL_DEMO_EDITOR_ROLE),
                                new SetPropertyTask(RepositoryConstants.CONFIG, SetupDemoRolesAndGroupsTask.PAGES_PERMISSIONS_ROLES, SetupDemoRolesAndGroupsTask.TRAVEL_DEMO_PUBLISHER_ROLE, SetupDemoRolesAndGroupsTask.TRAVEL_DEMO_PUBLISHER_ROLE))))
                .addTask(setupAccessPermissionsForDemoUsers)

                .addTask(setupTargetAppGroupAccessPermissions)

                .addTask(new IsModuleInstalledOrRegistered("Copy changes in site definition to multisite if multisite is installed", "multisite",
                        new IsModuleInstalledOrRegistered("", "public-user-registration",
                                new CopyNodeTask("", "/modules/travel-demo/config/travel/templates/availability/templates/pur", "/modules/multisite/config/sites/travel/templates/availability/templates/pur", true))))
        );

        register(DeltaBuilder.update("1.4.1", "")
                .addTask(new RemoveNodeTask("Remove travel node from PUR module configuration in favor of YAML decoration", "/modules/public-user-registration/config/configurations/travel"))
        );

        register(DeltaBuilder.update("1.5.1", "")
                .addTask(new NodeExistsDelegateTask("Disable addCORSHeaders filter and migrate to new CORS configuration", "/server/filters/addCORSHeaders", new ArrayDelegateTask("",
                        new CheckAndModifyPropertyValueTask("/server/filters/addCORSHeaders", "enabled", "true", "false"),
                        new MigrateCorsFilterConfigurationToSiteCorsConfiguration("/modules/travel-demo/config/travel")
                )))
        );
        register(DeltaBuilder.update("1.5.2", "")
                .addTask(new NodeExistsDelegateTask("Make SPA templates available by default.", "/modules/travel-demo/config/travel/templates/availability/enableAllWithRenderType", new ArrayDelegateTask("",
                        new RemovePropertyTask("Remove JSP configuration", "", RepositoryConstants.CONFIG, "/modules/travel-demo/config/travel/templates/availability/enableAllWithRenderType", "jsp"),
                        new SetPropertyTask("Add SPA configuration", RepositoryConstants.CONFIG, "/modules/travel-demo/config/travel/templates/availability/enableAllWithRenderType", "spa", "spa")
                )))
        );
        register(DeltaBuilder.update("1.6", "")
                .addTask(new MigrateTemplatesToMTK2("/travel"))
        );
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
        final List<Task> tasks = new ArrayList<>();
        tasks.addAll(super.getExtraInstallTasks(installContext));
        tasks.add(setupTravelSiteAsActiveSite);
        tasks.add(setDefaultUriOnPublicInstance);

        tasks.add(installPurSamples);

        tasks.add(new IsModuleInstalledOrRegistered("Enable travel site in multisite configuration", "multisite",
                new NodeExistsDelegateTask("Check whether multisite can be enabled for travel demo", "/modules/travel-demo/config/travel",
                        copySiteToMultiSiteAndMakeItFallback)));
        tasks.add(new SetupDemoRolesAndGroupsTask());
        tasks.add(setupAccessPermissionsForDemoUsers);
        tasks.add(setupTargetAppGroupAccessPermissions);
        return tasks;
    }

}
