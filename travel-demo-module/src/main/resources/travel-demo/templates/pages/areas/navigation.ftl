[#-------------- ASSIGNMENTS --------------]
[#include "/travel-demo/templates/macros/searchForm.ftl"]
[#include "/travel-demo/templates/macros/userLinks.ftl"]
[#include "/travel-demo/templates/macros/navigation.ftl"]

[#assign siteRoot = cmsfn.siteRoot(content)]
[#assign homeLink = cmsfn.link(siteRoot)!"/" /]

[#assign spaceClass = "navbar-spacer" /]

[#assign searchProperty = siteRoot.searchResultPage! /]
[#if searchProperty?has_content]
    [#assign searchResultPage = cmsfn.link(cmsfn.contentById(searchProperty)) /]
[/#if]

[#assign site = sitefn.site()!]
[#assign theme = sitefn.theme(site)!]

[#assign aboutPage=model.aboutDemoLink!]

[#-------------- RENDERING --------------]
<nav class="navbar navbar-default navbar-fixed-top " role="navigation" style="clear:both;">

    <div class="container">
        [#if aboutPage?has_content]<div id="about-link"><a href="${aboutPage}">${i18n['navigation.aboutDemo']}</a></div>[/#if]

        [@userLinks model.username!"" model.logoutLink!"" model.loginPageLink!"" model.profilePageLink!"" model.registrationPageLink!"" /]

        [#assign localizedLinks = cmsfn.localizedLinks()!]
        [#if localizedLinks?has_content]
            [#assign languages = localizedLinks?keys]
            <div id="language-link">
                <ul>
                    [#-- Current locale should not be linked. --]
                    [#-- Use "compress" to put "li" and "a" on one line to prevent white spaces from interfering with layout. --]
                    [#list languages as lang]
                        [#assign current = cmsfn.isCurrentLocale(lang)]
                        <li[#if current] class="active"[/#if]>[@compress single_line=true]
                            [#if !current]<a href="${localizedLinks[lang]!'#'}" title="${model.getLocale(lang).getDisplayName()!lang!}">[#else]<span>[/#if]${model.getLocale(lang).getDisplayLanguage()!lang?upper_case!}[#if !current]</a>[#else]</span>[/#if]
                        [/@compress]</li>
                    [/#list]
                </ul>
            </div>
        [/#if]

        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar"
                    aria-expanded="false" aria-controls="navbar" title="${i18n['navigation.toggle']}">
                <span class="sr-only">${i18n['navigation.toggle']}</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="${homeLink}">
                <img src="${ctx.contextPath}/.resources/${theme.name}/img/logo-white.png" alt=""/>
            </a>
        </div>

        <div id="navbar" class="navbar-collapse collapse">
            <div class="navbar-right">
                [@navigation navParentItem=siteRoot navClass="nav navbar-nav" /]
                [#-- Only when the search result page was set should the form be displayed --]
                [#if searchResultPage?exists]
                    [@searchForm action=searchResultPage! inputName="queryStr" placeholder=i18n['search.placeholder'] /]
                [/#if]

            </div>
        </div>

        [#assign subnavigationRootPage = navfn.ancestorPageAtLevel(content, 2)!]
        [#if subnavigationRootPage?has_content && navfn.navItems(subnavigationRootPage)?size > 0]
            <div id="navbar-secondary" class="navbar-collapse collapse">
                [@navigation navParentItem=subnavigationRootPage navClass="nav navbar-nav" /]
            </div>

            [#assign spaceClass = "navbar-spacer-children" /]
        [/#if]

    </div>

</nav>

<div class="${spaceClass}"></div>
