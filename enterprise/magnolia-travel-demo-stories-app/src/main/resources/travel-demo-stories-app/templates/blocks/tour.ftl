[#-------------- ASSIGNMENTS --------------]

[#if content.tour?has_content]

    [#include "/tours/templates/macros/tourTypeIcon.ftl"]
    [#include "/travel-demo-stories-app/templates/shared/functions.ftl"]

    [#assign tour = cmsfn.contentById(content.tour, "tours")!]
    [#assign destinations = tour.destination!]
    [#assign tourTypes = tour.tourTypes!]
    [#assign tourLink = cmsfn.link(tour)!]

    [#if tour.image?has_content]
        [#assign rendition = damfn.getRendition(tour.image, "original")]
        [#assign imageLink = rendition.link]
    [/#if]


[#-------------- RENDERING --------------]

<div data-block-type="tour-block" class="tour-block block">
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

    [#-- Translation come from tour module --]

    <div class="tour-meta">
        <div class="prop-label">${i18n['tour.property.startCity']}</div>
        <h4 class="prop-value">${tour.location!"-"}</h4>

        <div class="prop-label">${i18n['tour.property.duration']}</div>
        <h4 class="prop-value">${tour.duration!"-"} days</h4>

        <div class="prop-label">${i18n['tour.property.operator']}</div>
        <h4 class="prop-value">${tour.author!"-"}</h4>

        <p class="summary">${tour.description}</p>
        <p class="detail">${tour.body}</p>
    </div>

    <form action="${bookTourLink()}">
        <input type="hidden" name="location" value="${tour.location!}">
        <input class="btn btn-primary btn-lg book-button call-to-action" type="submit" value="${i18n['tour.book']}">
    </form>
</div>

[#else]

    [#include "/travel-demo/templates/macros/editorAlert.ftl"]
    [@editorAlert i18n['tour.block.no.tour.given'] /]

[/#if]
