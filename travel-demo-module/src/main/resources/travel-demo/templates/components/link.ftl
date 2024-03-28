[#-- This custom link component can include an image. --]
[#-------------- ASSIGNMENTS --------------]
[#include "/mtk2/templates/includes/init.ftl"]

[#-- Custom image macro for links only --]
[#-- Renders an image (asset) rendition --]
[#macro image image content imageClass="img-responsive" useOriginal=false]
    [#if image?has_content]
        [#-- Fallback text for alt text --]
        [#assign assetTitle=i18n['image.alt.unavailable']!]
        [#if image.asset?? && image.asset.title?has_content]
            [#assign assetTitle=image.asset.title]
        [/#if]

        [#-- Alt text and title --]
        [#assign imageAlt=content.altText!content.title!i18n['image.alt.prefix']+": "+assetTitle]
        [#assign imageTitle=content.title!content.altText!i18n['image.alt.prefix']+": "+assetTitle]

        [#assign imageLink=image.link /]
        [#-- For PNGs it might be useful to use render the original asset and therefore bypassing imaging --]
        [#if useOriginal]
            [#assign imageLink=image.asset.link /]
        [/#if]

        [#-- Render the image --]
    <img src="${imageLink}" alt="${imageAlt}" title="${imageTitle}" class="${imageClass}"/>
    [/#if]
[/#macro]

[#assign hasImage=false]
[#if content.image?exists && content.image?has_content]
    [#assign hasImage=true]
[/#if]

[#assign title = content.title!]
[#assign linkType = content.linkType!]
[#assign resolveError = false]
[#assign linkTarget = ""]

[#if linkType?has_content]
    [#assign divClass = "${linkType} ${divClass}"]
[/#if]


[#-------------- ASSIGNMENTS FOR EACH TYPE --------------]
[#if linkType=="page"]
    [#include "/mtk2/templates/includes/linkPage.ftl"]
[#elseif linkType=="external"]
    [#include "/mtk2/templates/includes/linkExternal.ftl"]
[#elseif linkType=="download"]
    [#include "/mtk2/templates/includes/linkDownload.ftl"]
[/#if]

[#if resolveError && cmsfn.editMode]
    [#assign divClass = "${divClass} note-for-editor"]
[/#if]


[#-------------- RENDERING --------------]
[#if cmsfn.editMode && resolveError]

    <span class="${divClass!}"${divID}>${i18n["reference.resolveError"]}</span>

[#else]

    <span class="${divClass!}"${divID}><a href="${link!""}"${linkTarget!}>
        [#if hasImage]
            [#assign assetRendition=damfn.getRendition(content.image, "original")]
            [@image assetRendition content "list-image" true /]
        [#else]
            ${title}
        [/#if]
    </a></span>

[/#if]
