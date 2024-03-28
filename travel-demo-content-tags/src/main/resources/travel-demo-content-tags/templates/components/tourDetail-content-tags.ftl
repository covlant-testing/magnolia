[#-------------- ASSIGNMENTS --------------]
[#include "/travel-demo/templates/macros/imageResponsive.ftl"]
[#include "/tours/templates/macros/tourTypeIcon.ftl" /]
[#include "/tours/templates/macros/sampleTourText.ftl" /]

[#assign tour = model.tour]
[#assign asset = tour.image!]
[#assign dc = damfn.getAssetMap(asset).metadata.dc!]
[#if asset?exists]
    [#assign assetCredit = dc.contributor?first!]
    [#assign imageHtml][@responsiveImageTravel asset "" "" "header-image" "data-ratio='1.33'" true /][/#assign]
[/#if]

[#if def.parameters.showTourTypes?? && def.parameters.showTourTypes == false]
    [#assign showTourTypes = false]
[#else]
    [#assign showTourTypes = true]
    [#assign relatedTourTypes = tour.tourTypes!]
[/#if]

[#assign tourListLink = cmsfn.link("website", content.tourListLink!"")!""]
[#if def.parameters.showTourTags?? && def.parameters.showTourTags == false]
    [#assign showTourTags = false]
[#else]
    [#assign showTourTags = true]
[/#if]


[#if def.parameters.showDestinations?? && def.parameters.showDestinations == false]
    [#assign showDestinations = false]
[#else]
    [#assign showDestinations = true]
    [#assign relatedDestinations = tour.destinations!]
[/#if]


[#-------------- RENDERING --------------]
<!-- TourDetail -->
<div class="product-header">
    <div class="navbar-spacer"></div>
    <div class="header-wrapper">
        ${imageHtml}
        <div class="lead-caption">
            <h1>${tour.name}</h1>

            [#if showTourTypes]
                <div class="category-icons">
                    [#list relatedTourTypes as tourType]
                        <div class="category-icon absolute-center-container">
                            <a href="${tourfn.getTourTypeLink(content, tourType.nodeName)!'#'}">
                                [@tourTypeIcon tourType.icon tourType.name "absolute-center" /]
                            </a>
                        </div>
                    [/#list]
                </div>
            [/#if]
        </div>
    </div>
</div>

<script>
    jQuery(".header-image").objectFitCoverSimple();
</script>

<div class="product-header-spacer"></div>
<div class="container after-product-header">

    <div class="row product-info product-summary">

            <div class="product-location">
            [#if showDestinations]
                [#list relatedDestinations as destination]
                    <div class="category-icon absolute-center-container">
                        <a href="${tourfn.getDestinationLink(content, destination.nodeName)!'#'}">
                            [@tourTypeIcon destination.icon destination.name "absolute-center" /]
                        </a>
                    </div>
                [/#list]
            [/#if]
            </div>

        <div class="product-properties col-xs-10 col-xs-push-1">
            <div class="product-property">
                <div class="property-label">${i18n.get('tour.property.startCity')}</div>
                <div class="property-value">${tour.location!}</div>
            </div>
            <div class="product-property">
                <div class="property-label">${i18n.get('tour.property.duration')}</div>
                <div class="property-value">${i18n.get('tour.duration', [tour.duration!])}</div>
            </div>
            <div class="product-property">
                <div class="property-label">${i18n.get('tour.property.operator')}</div>
                <div class="property-value">${tour.author!}</div>
            </div>
            <div class="product-property ">
            [#if showTourTypes]
                <div class="property-label">${i18n.get('tour.property.tourTypes')}</div>
                <div class="property-value product-categories">
                    <div class="category-icons">
                        [#list relatedTourTypes as tourType]
                            <a href="${tourfn.getTourTypeLink(content, tourType.nodeName)!'#'}">
                                <div class="category-icon absolute-center-container">
                                    [@tourTypeIcon tourType.icon tourType.name  "absolute-center" /]
                                </div></a>
                        [/#list]
                    </div>
                </div>
            [/#if]
            </div>
            <div class="product-property ">
            [#assign tags = tagfn.getTags(cmsfn.contentById(tour.identifier, "tours"))]
            [#if showTourTags && tags??]
                <div class="property-label">${i18n.get('tour.property.tags')}</div>
                <div class="property-value product-categories">
                    <div class="tokenfield">
                        [#list tags as tag]
                            <div class="tokenfield-wrapper">
                                <a href="${tourListLink?keep_before_last(".")}~${tag}~.${tourListLink?keep_after_last(".")}">
                                    <span>${tag}</span>
                                </a>
                            </div>
                        [/#list]
                    </div>
                </div>
            [/#if]
            </div>

        </div>

        <div class="product-action">
        [#assign bookNode = cmsfn.contentByPath("/travel/book-tour")]
            <form action="${cmsfn.link(bookNode)}">
                <input type="hidden" name="location" value="${tour.location!}">
                <input class="btn btn-primary btn-lg book-button" type="submit" value="${i18n['tour.book']}">
            </form>
        </div>
    </div>

    <div class="row product-info">
        <div class="col-xs-10 col-xs-push-1 product-property">
            <p class="summary">${tour.description}</p>

            [#if tour.body?has_content]
                <div class="body">${tour.body!}</div>
            [/#if]
            [@cms.area name="summary"/]
        </div>
    </div>

    <!-- Additional sample text for the purposes of demonstrating what a full page could look like -->
    <div class="row product-info">
        <div class="col-xs-10 col-xs-push-1 product-property">
            <hr style="margin-top:0px;"/>
            <div class="body">[@sampleTourText /]</div>
        </div>
    </div>

    [#if assetCredit?has_content]
        <div class="row product-info ">
            <div class="col-xs-10 col-xs-push-1 product-image-credit">
                <hr style="margin-top:0px;">
                <div class="body">${i18n['credit.leadImage']} ${assetCredit}
                    [#assign license=asset.copyright!]
                    [#if license?has_content]
                        &nbsp;<a target="_blank" href="https://creativecommons.org/licenses/${license}">${license}</a>
                    [/#if]
                </div>
            </div>
        </div>
    [/#if]

</div>

<!-- Book Tour Dialog -->
<div class="modal fade book-tour-not-implemented">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="${i18n['tour.book.notImplementedDialog.close']}"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">${i18n['tour.book.notImplementedDialog.title']}</h4>
            </div>
            <div class="modal-body">
                <p>${i18n['tour.book.notImplementedDialog.body']}</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">${i18n['tour.book.notImplementedDialog.close']}</button>
            </div>
        </div>
    </div>
</div>