[#-------------- ASSIGNMENTS --------------]
[#assign url = content.url!]

[#if def.parameters.divClass?has_content ]
    [#assign divClass = def.parameters.divClass]
[#else]
    [#assign divClass = "external-link"]
[/#if]


[#if url?has_content]

    [#assign peek = peekfn.unfurl(url!)!]

    [#if peek?has_content && peek.imageSource?has_content]
        [#assign divClass = "${divClass} has-image"]
    [/#if]

    [#-------------- RENDERING  --------------]
    <div class="row-wrapper">
        <div class="${divClass} row">
        [#if peek?has_content]
            <div class="link-preview">
            [#if peek.imageSource?has_content]
                <div class="peek-image col-xs-12 col-sm-12 col-md-3 col-lg-2">
                    <img alt="${i18n.get('link-unfurl.peek.thumbnail-for', [url!'link'])}" src="${peek.imageSource!}">
                </div>
            [/#if]
                <div class="peek col-xs-12 col-sm-12 col-md-9 col-lg-10">
                    <p><span><a href="${url!}" target="_blank">${url!}</a></span></p>
                    <h3>${peek.title!}</h3>
                    <p>${peek.description!}</p>
                </div>
            </div>
        [#else]
            <div class="peek-url col-lg-3 col-xs-12 col-sm-12 col-md-3">
                <p><a href="${url!}" target="_blank">${url!}</a></p>
            </div>
        [/#if]
        </div>
    </div>

[/#if]
