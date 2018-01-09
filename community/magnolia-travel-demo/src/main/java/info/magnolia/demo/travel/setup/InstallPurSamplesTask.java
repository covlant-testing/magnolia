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

import info.magnolia.cms.security.UserManager;
import info.magnolia.module.delta.AddURIPermissionTask;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.IsAuthorInstanceDelegateTask;
import info.magnolia.module.delta.OrderNodeBeforeTask;
import info.magnolia.module.delta.Task;

import java.util.Arrays;
import java.util.List;

/**
 * {@link ArrayDelegateTask} which install samples for Public User Registration.
 */
public class InstallPurSamplesTask extends ArrayDelegateTask {

    protected static final String PUR_SAMPLE_ROOT_PAGE_NAME = "members";

    protected static final List<String> PROTECTED_PAGES_NAMES = Arrays.asList(
            "protected",
            "profile-update"
    );

    protected static final List<String> PROTECTED_PAGES_PATHS = Arrays.asList(
            "/travel/" + PUR_SAMPLE_ROOT_PAGE_NAME + "/" + PROTECTED_PAGES_NAMES.get(0) + "*",
            "/travel/" + PUR_SAMPLE_ROOT_PAGE_NAME + "/" + PROTECTED_PAGES_NAMES.get(1) + "*",
            "<travel>/" + PUR_SAMPLE_ROOT_PAGE_NAME + "/" + PROTECTED_PAGES_NAMES.get(0) + "*",
            "<travel>/" + PUR_SAMPLE_ROOT_PAGE_NAME + "/" + PROTECTED_PAGES_NAMES.get(1) + "*"
    );

    protected static final String PASSWORD_CHANGE_PAGE_PATH = "travel/" + PUR_SAMPLE_ROOT_PAGE_NAME + "/forgotten-password/password-change";

    public InstallPurSamplesTask() {
        super("Install PUR samples if public-user-registration module is installed");
        this.addTask(new BootstrapSingleModuleResource("config.server.filters.securityCallback.clientCallbacks.travel-demo-pur.xml"));
        this.addTask(new BootstrapSingleModuleResource("config.modules.public-user-registration.config.configurations.travel.xml"));
        this.addTask(new BootstrapSingleResource("Install user role for PUR", "", "/mgnl-bootstrap-samples/travel-demo/userroles.travel-demo-pur.xml"));
        this.addTask(new BootstrapSingleResource("Install user group for PUR", "", "/mgnl-bootstrap-samples/travel-demo/usergroups.travel-demo-pur.xml"));
        this.addTask(new ArrayDelegateTask("",
                new IsAuthorInstanceDelegateTask("", (Task) null, this.getPermissionTasks()),
                new OrderNodeBeforeTask("/server/filters/securityCallback/clientCallbacks/travel-demo-pur", "form")
        ));
    }

    private ArrayDelegateTask getPermissionTasks() {
        ArrayDelegateTask task = new ArrayDelegateTask("");
        for (String page : PROTECTED_PAGES_PATHS) {
            task.addTask(new AddURIPermissionTask("", UserManager.ANONYMOUS_USER, page, AddURIPermissionTask.DENY));
        }
        return task;
    }
}