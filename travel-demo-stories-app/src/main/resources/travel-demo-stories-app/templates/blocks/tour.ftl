[#-------------- ASSIGNMENTS --------------]

[#if content.tour?hasContent]

    [#include "/tours/templates/macros/tourTypeIcon.ftl"]
    [#include "/travel-demo-stories-app/templates/shared/functions.ftl"]

    [#assign tour = cmsfn.contentById(content.tour, "tours")!]
    [#assign destinations = tour.destination!]
    [#assign tourTypes = tour.tourTypes!]
    [#assign tourLink = cmsfn.link(tour)!]

    [#if tour.image?hasContent]
        [#assign rendition = damfn.getRendition(tour.image, "original")]
        [#assign imageLink = rendition.link]
    [/#if]


[#-------------- RENDERING --------------]

<div data-block-type="tour-block" class="tour-block block">
    <div class="row">
        <div class="title-badge-wrapper col-xs-12 col-sm-12 col-md-4 col-lg-4">
            <div class="title-badge" style="background-image: url(${imageLink!});">
                <h3>${tour.name!tour.@name}</h3>
                    [#assign tourType = tourTypes?first]
                    [#assign category = cmsfn.contentById(tourType, "category")!]
                <div class="tour-type">
                    [#assign rendition = damfn.getRendition(category.icon, "original")]
                    [#assign imageLink = rendition.link]
                    <img class="icon" src="${imageLink}" alt="${category.name!}" title="${category.name!}">
                </div>
            </div>
        </div>

        [#-- Translation come from tour module --]

        <div class="tour-meta col-xs-12 col-sm-12 col-md-6 col-lg-6">
            <div class="prop-label">${i18n['tour.property.startCity']}</div>
            <h4 class="prop-value">${tour.location!"-"}</h4>

            <div class="prop-label">${i18n['tour.property.duration']}</div>
            <h4 class="prop-value">${tour.duration!"-"} days</h4>

            <div class="prop-label">${i18n['tour.property.operator']}</div>
            <h4 class="prop-value">${tour.author!"-"}</h4>

            <p class="summary">${tour.description}</p>
                [#assign body = tour.body!]
                [#if body?hasContent]
                <p class="detail">${(body?length>285)?then(body[0..285]+"...",body)}</p>
                [/#if]
        </div>
        <div class="col-xs-12 col-sm-12 col-md-2 col-lg-2">
            <form action="${bookTourLink()}">
                <input type="hidden" name="location" value="${tour.location!}">
                <input class="btn btn-primary btn-lg book-button call-to-action" type="submit" value="${i18n['tour.book']}">
            </form>
        </div>
    </div>
</div>

[#else]

    [#include "/travel-demo/templates/macros/editorAlert.ftl"]
    [@editorAlert i18n['tour.block.no.tour.given'] /]

[/#if]
