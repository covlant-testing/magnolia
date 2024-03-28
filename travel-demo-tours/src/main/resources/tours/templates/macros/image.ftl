[#-- Macro that renders an image with an optional credit tag --]
[#macro tourImage assetRendition assetCredit="" altTitle="" cssClass="img-responsive"]
    [#if assetRendition?has_content]
        <img class="${cssClass}" src="${assetRendition.link}" alt="Image: ${altTitle}" />
        [#if assetCredit?has_content]
            <span class="image-credit">${assetCredit}&nbsp;
            [#assign license=assetRendition.asset.copyright!]
            [#if license?has_content]
                <a target="_blank" href="https://creativecommons.org/licenses/${license}">${license}</a>
            [/#if]
            </span>
        [/#if]
    [/#if]
[/#macro]

[#-- Function that returns a style attribute with background-image and -size for given asset rendition --]
[#function backgroundImage assetRendition]
    [#if assetRendition?exists && assetRendition?has_content]
        [#return ' style="background-image: url(${assetRendition.link}); background-size: cover; background-position:center;"' /]
    [#else]
        [#return "" /]
    [/#if]
[/#function]
