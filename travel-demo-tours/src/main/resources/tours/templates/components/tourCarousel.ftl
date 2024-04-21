[#-------------- ASSIGNMENTS --------------]
[#include "/tours/templates/macros/image.ftl" /]
[#include "/tours/templates/macros/tourTypeIcon.ftl" /]

[#assign tours = model.tours]
[#assign showTourTypes = content.showTourTypes!true]

[#-------------- RENDERING --------------]

<!-- Tour Carousel -->
<div id="myCarousel" class="carousel slide">
    <!-- Indicators -->
    <ol class="carousel-indicators">
    [#list tours as tour]
        [#assign activeClass=""]
        [#if tour_index == 0 ]
            [#assign activeClass="active"]
        [/#if]
        <li data-target="#myCarousel" data-slide-to="${tour_index}" class="${activeClass}"></li>
    [/#list]
    </ol>

    <!-- Actual Carousel List -->
    <div class="carousel-inner" role="listbox">
    [#list tours as tour]
        [#assign activeClass=""]
        [#if tour_index == 0 ]
            [#assign activeClass="active"]
        [/#if]
        [#assign rendition = damfn.getRendition(tour.image, "1366")!]
        <div class="item ${activeClass}"${backgroundImage(rendition)}>
            <div class="container">
                <a class="carousel-link" href="${tour.link!}">
                    <div class="carousel-caption">
                        <h1>${tour.name!}</h1>
                        [#if showTourTypes]
                            <div class="category-icons">
                                [#list tour.tourTypes as tourType]
                                    <div class="category-icon absolute-center-container">
                                        [@tourTypeIcon tourType.icon tourType.name "" /]
                                    </div>
                                [/#list]
                            </div>
                        [/#if]
                        <button class="btn btn-lg btn-primary">${i18n['tour.view']}</button>
                    </div>
                </a>
            </div>
        </div>
    [/#list]
    </div>

    <!-- Carousel Controls -->
    <a class="left carousel-control" href="#myCarousel" data-slide="prev">
        <span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>
        <span class="sr-only">${i18n['tour.previous']}</span>
    </a>
    <a class="right carousel-control" href="#myCarousel" data-slide="next">
        <span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
        <span class="sr-only">${i18n['tour.next']}</span>
    </a>
</div>
