[#-------------- ASSIGNMENTS --------------]
[#assign homeLink = cmsfn.link(cmsfn.siteRoot(content))!"/"]
[#assign site = sitefn.site()!]
[#assign theme = sitefn.theme(site)!]

[#-------------- RENDERING --------------]
<!-- FOOTER -->
<footer>

    <div class="footer-1">
        <div class="container">
            <div class="row">
            [#if def.parameters?? && def.parameters.columns?? && def.parameters.columns > 1]
                [#list 1..def.parameters.columns as columnIndex]
                    [@cms.area name="footer${columnIndex}"/]
                [/#list]
            [#else]
                [@cms.area name="footer1"/]
            [/#if]
            </div>

            <div class="row">
                <div class="col-lg-12">
                    <div class="footer-last-links">
                        &copy; 2015-${.now?string['yyyy']} Magnolia Travels, Inc.
                        [@cms.area name="lastLinks"/]
                    </div>
                </div>
            </div>

        </div>
    </div>

    <div class="container">
        <div class="row">
            <div class="col-lg-12">
                <p class="footer-logo">
                    <a class="home" href="${homeLink}">
                        <img src="${ctx.contextPath}/.resources/${theme.name}/img/logo-white.png" alt=""/>
                    </a>
                </p>
            </div>
        </div>
    </div>

    <div class="footer-2"></div>

</footer>
