[#-------------- ASSIGNMENTS --------------]
[#include "/travel-demo/templates/macros/editorAlert.ftl" /]

[#assign slideShowId = content.id!"carousel-${content.@id}"]
[#assign cssClass = "component-carousel"]

[#-------------- RENDERING --------------]

<!-- carousel start -->

[#-- We only want to render the javascript when not in edit mode --]
[#if !cmsfn.editMode]
<script type="text/javascript">
    $(document).ready(function () {
        $("#" + "${slideShowId}").slick({
            [#if content.slickConfig?has_content]
            ${content.slickConfig?replace("<br/>", " ")}
            [#else]
                dots: ${content.dots!"true"},
                arrows: ${content.arrows!"true"},
                fade: ${content.fade!"false"},
                variableWidth: ${content.variableWidth!"false"},
                slidesToShow: ${content.slidesToShow!"2"},
                autoplay: ${content.autoplay!"false"},
                autoplaySeconds: ${content.autoplaySeconds!"5"},
                nextArrow: '<button type="button" class="slick-next"><span class="glyphicon glyphicon-chevron-right"></span></button>',
                prevArrow: '<button type="button" class="slick-prev"><span class="glyphicon glyphicon-chevron-left"></span></button>'
            [/#if]
        });
        // Add lazypreload to all lazy images in carousel
        $("#" + "${slideShowId} img.lazyload").addClass("lazypreload");
    });
</script>
[#else]
    [#-- When in edit mode we use a 6x6 grid to simplify working with the carousel items --]
    [#-- Thus we have to add the class 'row' --]
    [#assign cssClass = "${cssClass} row"]
    [@editorAlert i18n['note.for.editors.carousel'] /]
[/#if]

<div id="${slideShowId}" class="${cssClass}">
    [@cms.area name="carouselItems" /]
</div><!-- carousel end -->
