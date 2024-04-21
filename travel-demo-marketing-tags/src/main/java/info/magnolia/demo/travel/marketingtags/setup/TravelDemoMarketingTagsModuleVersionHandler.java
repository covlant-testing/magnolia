/**
 * This file Copyright (c) 2015-2024 Magnolia International
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
package info.magnolia.demo.travel.marketingtags.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.BootstrapSingleResource;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;

/**
 * Install the travel-demo-marketing-tags module.
 */
public class TravelDemoMarketingTagsModuleVersionHandler extends DefaultModuleVersionHandler {

    private NodeExistsDelegateTask cookieConsentPluginSetup = new NodeExistsDelegateTask("Setup Cookie Consent", "Adds Cookie Consent to the Travel demo.", RepositoryConstants.WEBSITE, "/travel", new ArrayDelegateTask("",
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "bannerbackground", "#000"),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "buttonbackground", "#ef6155"),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "buttontext", "#fff"),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "complianceType", "info"),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "dismiss", "Got it!"),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "allow", "Allow cookies"),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "deny", "Decline cookies"),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "layout", "block"),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "link", "Learn more ..."),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "header", "Cookies are used on this website!"),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "message", "This website uses cookies to ensure you get the best experience on our website."),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "position", "bottom"),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "readMoreLink", "external"),
            new SetPropertyTask(RepositoryConstants.WEBSITE, "/travel", "readMoreLinkexternal", "https://cookiesandyou.com/"))
    );

    public TravelDemoMarketingTagsModuleVersionHandler() {
        register(DeltaBuilder.update("1.3", "")
                .addTask(cookieConsentPluginSetup)
                .addTask(new BootstrapSingleResource("Bootstrap Clicky sample to Travel Demo.", "", "/mgnl-bootstrap-samples/travel-demo-marketing-tags/marketing-tags.Clicky-for-Travel-Demo.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
                .addTask(new BootstrapSingleResource("Bootstrap Google Analytics sample to Travel Demo.", "", "/mgnl-bootstrap-samples/travel-demo-marketing-tags/marketing-tags.Google-Analytics-for-Travel-Demo.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
                .addTask(new BootstrapSingleResource("Bootstrap configuration of Google Analytics cookie to cookie manager.", "", "/mgnl-bootstrap-samples/travel-demo-marketing-tags/config.modules.cookie-manager.config.cookies.google_analytics.xml", ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING))
        );
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<>(super.getExtraInstallTasks(installContext));
        tasks.add(cookieConsentPluginSetup);
        return tasks;
    }
}
