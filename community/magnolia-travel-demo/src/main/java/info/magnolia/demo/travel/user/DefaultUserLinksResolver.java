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
package info.magnolia.demo.travel.user;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.security.LogoutFilter;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.WebContext;
import info.magnolia.rendering.template.type.DefaultTemplateTypes;
import info.magnolia.templating.functions.TemplatingFunctions;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Resolver of user (profile, registration pages) links.
 */
public class DefaultUserLinksResolver implements UserLinksResolver {

    private boolean enabled = true;
    private String profilePageTemplateId;
    private String registrationPageTemplateId;
    private String loginPageTemplateId;
    private String rootTemplateType = DefaultTemplateTypes.SITE_ROOT;

    private final Provider<WebContext> webContextProvider;
    private final Provider<AggregationState> aggregationStateProvider;
    private final TemplatingFunctions templatingFunctions;

    @Inject
    public DefaultUserLinksResolver(Provider<WebContext> webContextProvider, Provider<AggregationState> aggregationStateProvider, TemplatingFunctions templatingFunctions) {
        this.webContextProvider = webContextProvider;
        this.aggregationStateProvider = aggregationStateProvider;
        this.templatingFunctions = templatingFunctions;
    }

    @Override
    public boolean useForCurrentPage() throws RepositoryException {
        return this.findPage(loginPageTemplateId) != null;
    }

    @Override
    public String getUsername() {
        final String username = webContextProvider.get().getUser().getName();
        return UserManager.ANONYMOUS_USER.equals(username) ? null : username;
    }

    @Override
    public String getProfilePageLink() throws RepositoryException {
        return profilePageTemplateId == null ? null : templatingFunctions.link(this.findPage(profilePageTemplateId));
    }

    @Override
    public String getRegistrationPageLink() throws RepositoryException {
        return registrationPageTemplateId == null ? null : templatingFunctions.link(this.findPage(registrationPageTemplateId));
    }

    @Override
    public String getLoginPageLink() throws RepositoryException {
        return templatingFunctions.link(this.findPage(loginPageTemplateId));
    }

    @Override
    public String getLogoutLink() throws RepositoryException {
        return this.getLoginPageLink() + "?" + LogoutFilter.PARAMETER_LOGOUT + "=true";
    }

    protected Node findPage(String templateId) throws RepositoryException {
        List<javax.jcr.Node> profilePages = templatingFunctions.contentListByTemplateIds(templatingFunctions.siteRoot(aggregationStateProvider.get().getMainContentNode(), rootTemplateType), Collections.singleton(templateId), 1, null, null);
        return profilePages.size() == 0 ? null : profilePages.get(0);
    }

    public String getProfilePageTemplateId() {
        return profilePageTemplateId;
    }

    public void setProfilePageTemplateId(String profilePageTemplateId) {
        this.profilePageTemplateId = profilePageTemplateId;
    }

    public String getRegistrationPageTemplateId() {
        return registrationPageTemplateId;
    }

    public void setRegistrationPageTemplateId(String registrationPageTemplateId) {
        this.registrationPageTemplateId = registrationPageTemplateId;
    }

    public String getLoginPageTemplateId() {
        return loginPageTemplateId;
    }

    public void setLoginPageTemplateId(String loginPageTemplateId) {
        this.loginPageTemplateId = loginPageTemplateId;
    }

    public String getRootTemplateType() {
        return rootTemplateType;
    }

    public void setRootTemplateType(String rootTemplateType) {
        this.rootTemplateType = rootTemplateType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected Provider<AggregationState> getAggregationStateProvider() {
        return aggregationStateProvider;
    }

    protected TemplatingFunctions getTemplatingFunctions() {
        return templatingFunctions;
    }
}
