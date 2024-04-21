[#macro tourTeaser tour additionalWrapperClass="col-md-6"]

    [#include "/travel-demo/templates/macros/imageResponsive.ftl"]
    [#include "/tours/templates/macros/tourTypeIcon.ftl" /]
    [#assign imageHtml][@responsiveImageTravel tour.image "" "" "tour-card-image" "data-ratio='1.33'" true /][/#assign]

    <!-- Tour Teaser -->
    <div class="${additionalWrapperClass} tour-card card">
        <div class="tour-card-background">
        ${imageHtml}
        </div>
        <a class="tour-card-anchor" href="${tour.link!}">
            <div class="tour-card-content-shader"></div>
            <div class="tour-card-content">
                <h3>${tour.name!}</h3>
                <div class="category-icons">
                    [#list tour.tourTypes as tourType]
                        <div class="category-icon absolute-center-container">
                            [@tourTypeIcon tourType.icon tourType.name "" /]
                        </div>
                    [/#list]
                </div>
                <div class="card-button">
                    <div class="btn btn-primary">${i18n['tour.view']}</div>
                </div>
            </div>
        </a>
    </div>

[/#macro]
