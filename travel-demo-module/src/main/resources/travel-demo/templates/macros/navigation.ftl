[#--
Customized /mtk2/templates/macros/navigation.ftl macro for travel-demo project.
Uncommented and adjusted section for resolving menu for destination and travel from the category content app.
--]
[#macro navigation navParentItem depth=1 expandAll=false navClass="nav"]

    [#if navParentItem?has_content && depth > 0]
    <ul class="${navClass}">

        [#assign navItems = navfn.navItems(navParentItem)]
        [#list navItems as navItem]
            [#if navfn.hasTemplateSubtype(navItem, "destinationCatOverview")]
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                        ${navItem.navigationTitle!navItem.title!navItem.@name}
                        <span class="caret"></span>
                    </a>

                    <ul class="dropdown-menu" role="menu">
                        [#assign navContentItems = navfn.navItemsFromApp("category", "/destinations", "mgnl:category")]
                        [#list navContentItems as navContentItem]
                            <li><a href="${navfn.linkWithSelector(navItem, navContentItem)!"#"}">${navContentItem.displayName!navContentItem.@name}</a></li>
                        [/#list]
                    </ul>
                </li>
            [#elseif navfn.hasTemplateSubtype(navItem, "travelCatOverview")]
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                            ${navItem.navigationTitle!navItem.title!navItem.@name}
                            <span class="caret"></span>
                        </a>

                        <ul class="dropdown-menu" role="menu">
                            [#assign navContentItems = navfn.navItemsFromApp("category", "/tour-types", "mgnl:category")]
                            [#list navContentItems as navContentItem]
                                <li><a href="${navfn.linkWithSelector(navItem, navContentItem)!"#"}">${navContentItem.displayName!navContentItem.@name}</a></li>
                            [/#list]
                        </ul>
                    </li>
            [#else]
                [#assign activeNavItem = navfn.isActive(content, navItem)] [#-- Active navigation item is the one which is same as current page--]
                [#assign openNavItem = navfn.isOpen(content, navItem)] [#-- Open navigation item is the one which is ancestor of current page--]
                [#if activeNavItem]
                    <li class="active">
                [#elseif openNavItem]
                    <li class="open">
                [#else]
                    <li>
                [/#if]
                        <a href="${navfn.link(navItem)!"#"}">${navItem.navigationTitle!navItem.title!navItem.@name}</a>
                        [#if expandAll || activeNavItem || openNavItem]
                            [@navigation navItem depth-1 /]
                        [/#if]
                    </li>
            [/#if]
        [/#list]
    </ul>
    [/#if]
[/#macro]