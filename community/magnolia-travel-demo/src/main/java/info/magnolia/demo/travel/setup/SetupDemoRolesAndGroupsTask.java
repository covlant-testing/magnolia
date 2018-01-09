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

import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.IsModuleInstalledOrRegistered;
import info.magnolia.module.delta.NewPropertyTask;

/**
 * Gives basic permissions to the roles and groups defined by this project.
 */
public class SetupDemoRolesAndGroupsTask extends ArrayDelegateTask {

    public static final String TRAVEL_DEMO_PUBLISHER_ROLE = "travel-demo-publisher";
    public static final String TRAVEL_DEMO_EDITOR_ROLE = "travel-demo-editor";
    public static final String TRAVEL_DEMO_ADMINCENTRAL_ROLE = "travel-demo-admincentral";

    public static final String TRAVEL_DEMO_PUBLISHERS_GROUP = "travel-demo-publishers";
    public static final String PAGES_ACTIVATE_ACCESS_ROLES = "/modules/pages/apps/pages/subApps/browser/actions/activate/availability/access/roles";
    public static final String DAM_ACTIVATE_ACCESS_ROLES = "/modules/dam-app/apps/assets/subApps/browser/actions/activate/availability/access/roles";
    public static final String PAGES_PERMISSIONS_ROLES = "/modules/pages/apps/pages/permissions/roles";
    public static final String DAM_PERMISSIONS_ROLES = "/modules/dam-app/apps/assets/permissions/roles";
    public static final String WORKFLOW_JBPM_PUBLISH_GROUPS = "/modules/workflow-jbpm/tasks/publish/groups";
    public static final String ENTERPRISE_MODULE = "enterprise";
    public static final String WORKFLOW_JBPM_MODULE = "workflow-jbpm";

    public SetupDemoRolesAndGroupsTask() {
        super("Set permissions for the travel-demo-editor(s) and travel-demo-publisher(s) roles and groups");

        addTask(new AddDemoTravelPermissionTask(PAGES_PERMISSIONS_ROLES, TRAVEL_DEMO_EDITOR_ROLE));
        addTask(new AddDemoTravelPermissionTask(PAGES_PERMISSIONS_ROLES, TRAVEL_DEMO_PUBLISHER_ROLE));

        addTask(new AddDemoTravelPermissionTask(DAM_PERMISSIONS_ROLES, TRAVEL_DEMO_EDITOR_ROLE));
        addTask(new AddDemoTravelPermissionTask(DAM_PERMISSIONS_ROLES, TRAVEL_DEMO_PUBLISHER_ROLE));

        addTask(new AddDemoTravelPermissionTask(PAGES_ACTIVATE_ACCESS_ROLES, TRAVEL_DEMO_PUBLISHER_ROLE));
        addTask(new IsModuleInstalledOrRegistered("If on EE, give editors publishing rights on website", ENTERPRISE_MODULE, new AddDemoTravelPermissionTask(PAGES_ACTIVATE_ACCESS_ROLES, TRAVEL_DEMO_EDITOR_ROLE)));

        addTask(new AddDemoTravelPermissionTask(DAM_ACTIVATE_ACCESS_ROLES, TRAVEL_DEMO_PUBLISHER_ROLE));
        addTask(new AddDemoTravelPermissionTask(DAM_ACTIVATE_ACCESS_ROLES, TRAVEL_DEMO_EDITOR_ROLE));

        addTask(new IsModuleInstalledOrRegistered("If workflow-jbpm module is installed, add workflow permissions for " + TRAVEL_DEMO_PUBLISHERS_GROUP, WORKFLOW_JBPM_MODULE, new NewPropertyTask("", WORKFLOW_JBPM_PUBLISH_GROUPS, TRAVEL_DEMO_PUBLISHERS_GROUP, TRAVEL_DEMO_PUBLISHERS_GROUP)));
    }
}
