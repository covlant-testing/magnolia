[#-------------- ASSIGNMENTS --------------]
[#include "/travel-demo/templates/macros/imageResponsive.ftl"]

[#assign category = tourfn.categoryByUrl]
[#assign imageHtml][@responsiveImageTravel category.image "" "" "header-image" "data-ratio='1.33'" true /][/#assign]
[#assign assetCredit = category.image.caption!]

[#-------------- RENDERING --------------]
<!-- TourType Overview Header -->
<div class="category-header">
    <div class="navbar-spacer"></div>
    <div class="header-wrapper">
        ${imageHtml}
        <div class="lead-caption">
            <h1 class="category">${category.name!}</h1>

            [#if category.description?has_content]
                <div class="category-description">
                    ${category.description!}
                </div>
            [/#if]
        </div>
    </div>
</div>

<div class="container after-category-header-2"></div>

<script>
    jQuery(".header-image").objectFitCoverSimple();
</script>
