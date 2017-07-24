[#-------------- ASSIGNMENTS --------------]

[#include "/mtk/templates/macros/image.ftl"]
[#include "/travel-demo-stories-app/templates/shared/functions.ftl"]

[#assign imageBlocks = getAllBlocksOfType('image')!]


[#-------------- RENDERING --------------]

<div class="stories-header">
    <h1>${i18n['stories.page.discover']}</h1>
    <img src="${ctx.contextPath}/.resources/travel-demo-stories-app/webresources/img/icon_safari.svg" alt="${i18n['stories.page.discover']}">
</div>

<div class="image-universe-container" id="image-universe-${content.@id}">
    <div class="row">
        <div class="col-md-12">

            <div class="image-universe mosaicflow"
                 data-item-height-calculation="attribute"
                 data-min-item-width="300">

            [#list imageBlocks as blockNode]
                [#assign parentNode = blockNode.getParent()]
                [#assign parentContentMap = cmsfn.asContentMap(parentNode)]
                [#assign block = cmsfn.asContentMap(blockNode)]
                [#assign rendition = damfn.getRendition(block.image, "320")]

                <div class="mosaicflow__item iu-image-parent">
                    <a href="${storyLink(content, parentContentMap)!"#"}">
                        [@image rendition block "" false {"imageWrapperClass": "image-block"} true /]
                        <div class="iu-image-overlay ${model.getColor()}">
                            <span class="shout-out">
                                ${model.getShoutOut()} ${block.imageCaption?hasContent?then("&gt;<br>", "")}
                            </span>
                            ${block.imageCaption!}
                        </div>
                    </a>
                </div>

                [#if blockNode_index == 50]
                    [#break]
                [/#if]
            [/#list]

            </div>

        </div>
    </div>
</div>
