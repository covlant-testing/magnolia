[#-------------- ASSIGNMENTS --------------]
[#include "/tours/templates/macros/tourTeaser.ftl"]

[#assign title = content.title!]
[#if tagfn.getTags(content)?has_content]
    [#assign tours = tagfn.getContentByTags("tours", "/magnolia-travels", content.logicalOperand!"OR", tagfn.getTags(content))]
[#elseif state.getSelector()?has_content]
    [#assign tours = tagfn.getContentByTags("tours", "/magnolia-travels", state.getSelector())]
[#else]
    [#assign tours = cmsfn.children(cmsfn.contentByPath("/magnolia-travels", "tours"))?chunk(10)?first]
[/#if]

[#-------------- RENDERING --------------]
<!-- Tour List -->
<div class="container tour-list">

    <h2>${title}</h2>

    <div class="row">
        [#list tours as tour]
            [#include "/travel-demo/templates/macros/imageResponsive.ftl"]
            [#assign imageHtml][@responsiveImageTravel tour.image "" "" "tour-card-image" "data-ratio='1.33'" true /][/#assign]
            <!-- Tour Teaser -->
            <div class="col-md-6 tour-card card" >
                <div class="tour-card-background">
                ${imageHtml}
                </div>
                <a class="tour-card-anchor" href="${tourfn.getTourLink(tour)!}">
                    <div class="tour-card-content-shader"></div>
                    <div class="tour-card-content">
                        <h3>${tour.name!}</h3>
                        <div class="card-button">
                            <div class="btn btn-primary"}">${i18n['tour.view']}</div>
                        </div>
                    </div>
                </a>
            </div>
        [/#list]
    </div>

</div>

<script>
    jQuery(".tour-card-image").objectFitCoverSimple();
</script>