[#-- Displays a row of featured tours. --]
[#macro relatedTours categoryName tours]

    [#include "/travel-demo/templates/macros/imageResponsive.ftl"]
    [#include "/travel-demo/templates/macros/editorAlert.ftl" /]
    [#include "/tours/templates/macros/tourTypeIcon.ftl" /]

    [#if tours?has_content || cmsfn.editMode]
    <div class="container after-category-header">

        [#-- get(key, args[]) requires the second parameter to be a sequence --]
        <h2>${i18n.get('tour.featured', [categoryName])}</h2>
        <div class="row featured-card-row">
            [#list tours as tour]
                [#assign name = tour.name!tour.@name /]
                [#assign description = tour.description!"" /]
                [#assign tourLink = tour.link /]
                [#assign imageHtml][@responsiveImageTravel tour.image "" "" "featured-image" "data-ratio='1.33'" true /][/#assign]

                <a class="featured-card-anchor" href="${tourLink!}">
                    <div class="col-md-4 featured-card card">
                        ${imageHtml}
                        <div class="featured-card-shader"></div>
                        <div class="featured-blaze"></div>

                        <div class="featured-blaze-text">${i18n['tour.featured.card']}</div>

                        <h3>${name!}</h3>
                        <div class="category-icons">
                            [#list tour.tourTypes as tourType]
                                <div class="category-icon">
                                    [@tourTypeIcon tourType.icon tourType.name "" /]
                                </div>
                            [/#list]
                        </div>

                        <div class="featured-card-content">
                            [#if description?has_content]
                                <p><span class="description">${description!}</span></p>
                            [/#if]
                        </div>
                        <div class="card-button">
                            <div class="btn btn-primary">${i18n['tour.view']}</div>
                        </div>
                    </div>
                </a>
            [/#list]
        </div>

        [@editorAlert i18n.get('note.for.editors.featured', [categoryName!""]) /]

    </div>

    <script>
        jQuery(".featured-image").objectFitCoverSimple();
    </script>
    [/#if]

[/#macro]
