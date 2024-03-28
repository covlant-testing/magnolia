[#-- Renders an image (asset) rendition --]
[#macro imageResponsive image content imageClass="content-image" useOriginal=false definitionParameters={}]
    [#if image?has_content]
        [#-- Fallback text for alt text --]
        [#assign assetTitle = i18n['image.no.alt']]
        [#if image.asset?? && image.asset.title?has_content]
            [#assign assetTitle = image.asset.title]
        [/#if]

        [#-- Alt text and title --]
        [#assign imageAlt = content.imageAltText!content.imageTitle!assetTitle!]
        [#assign imageTitle = content.imageTitle!content.imageAltText!assetTitle!]

        [#assign imageLink = image.link]
        [#-- For PNGs/GIFs it might be useful to use render the original asset and therefore bypassing imaging --]
        [#if useOriginal]
            [#assign imageLink = image.asset.link]
        [/#if]

        [#-- Image caption / credit; Falls back to asset's properties --]
        [#assign imageCaption = content.imageCaption!image.asset.caption!""]
        [#assign dc = damfn.getAssetMap(image.asset).metadata.dc!]
        [#assign imageCredit = content.imageCredit!dc.contributor?first!""]

        [#-- CSS --]
        [#-- Image class is used from def.parameters, otherwise falls back to given parameter --]
        [#assign divWrapperClass = definitionParameters.imageWrapperClass!"content-image-wrapper"]
        [#assign imgClass = imageClass][#-- Using another variable here as the macro parameter cannot be re-assinged --]
        [#if definitionParameters.imageClass?has_content]
        [#-- Adding custom parameters as img class when specified --]
            [#assign imgClass = "${imgClass} ${definitionParameters.imageClass}"]
        [/#if]

        [#-------------- RENDERING  --------------]
        [#-- Using a wrapper to be able to position caption+credit nicely --]
        [#if imageCaption?has_content || imageCredit?has_content]
        <!-- image with caption/credit -->
        <div class="${divWrapperClass}">
        [/#if]

        [#assign constrainAspectRatio = (content.constrainAspectRatio!false)]
        [@responsiveImageTravel image.asset imageAlt imageTitle imgClass "" constrainAspectRatio /]

        [#if imageCaption?has_content || imageCredit?has_content]
            [#if imageCaption?has_content]
                <span class="image-caption">${imageCaption}</span>
            [/#if]
            [#if imageCredit?has_content]
                <span class="image-credit">${imageCredit}</span>
            [/#if]
        </div><!-- end image with caption/credit -->
        [/#if]
    [/#if]
[/#macro]


[#-- Macro to render a responsive image with the variations configured in the theme. --]
[#macro responsiveImageTravel asset  alt="" title="" cssClass="" additional="" constrainAspectRatio=false]

    [#if constrainAspectRatio ]
        [#assign srcs = [
            {"name":"480x360", "width":"480"},
            {"name":"960x720", "width":"960"},
            {"name":"1366x1024","width":"1366"},
            {"name":"1600x1200", "width":"1600"}]]

        [#assign fallback="960x720"]

        [@responsiveImageLazySizes asset alt title cssClass srcs fallback additional /]
    [#else]
        [#assign srcs = [
            {"name":"480", "width":"480"},
            {"name":"960", "width":"960"},
            {"name":"1366","width":"1366"},
            {"name":"1600","width":"1600"}]]

        [#assign fallback="960"]

        [@responsiveImageLazySizes asset alt title cssClass srcs fallback additional /]
    [/#if]
[/#macro]

[#-- Macro to render responsive image using lazysizes javascript library.
        Use data-srcset attribute to only load the size of image that the current image width requires --]
[#macro responsiveImageLazySizes asset  alt="" title="" cssClass="" srcs="" fallbackName="" additional="" ]
    [#if asset?exists]
        [#assign cssClass = cssClass + " lazyload"]
        [#assign rendition = damfn.getRendition(asset, fallbackName)!]
        <noscript>
            <img class="${cssClass}" src="${rendition.link}" alt="${alt}" title="${title}" ${additional} />
        </noscript>
        <img data-sizes="auto" class="${cssClass} lazyload" ${additional} src="data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==" alt="${alt}" title="${title}"
             data-srcset="[#compress]
                [#list srcs as src]
                    [#assign rendition = damfn.getRendition(asset, src.name)!]
                    [#if rendition?exists && rendition?has_content]
                        ${rendition.link} ${src.width}w,
                    [/#if]
                [/#list]
            [/#compress]" />
    [/#if]
[/#macro]
