[#-- Displays an overview list of the stories --]

[#-------------- ASSIGNMENTS --------------]
[#include "/travel-demo/templates/macros/editorAlert.ftl"]
[#include "/travel-demo-stories-app/templates/shared/functions.ftl"]


[#macro storyLead story cssClass]

    [#if story?hasContent]
        [#assign background = "background-color: #efefef;"]
        [#assign cssVideoOrEmbed = ""]

        [#assign visualType = story.visualType!]
        [#if visualType?hasContent]
            [#assign embed = false]
            [#assign video = false]

            [#if visualType == "image" && story.imagesource?hasContent]
                [#assign background = cssBackground(story.imagesource)]

            [#elseIf visualType == "video" && story.videosource?hasContent]
                [#assign video = true]
                [#assign cssVideoOrEmbed = " video-or-embed"]
                [#assign videoRendition = damfn.getRendition(story.videosource, "original")!]

            [#elseIf visualType == "embed"]
                [#assign embedBackground = story.embedimage?hasContent?then(cssBackground(story.embedimage), background)]

                [#if story.embedsource?hasContent]
                    [#assign embed = true]
                    [#assign cssVideoOrEmbed = " video-or-embed"]
                [/#if]
            [/#if]
            [#if video || embed]
                <div class="story-wrapper">
            [/#if]
            <a href="${storyLink(content, story)!"#"}" class="story ${cssClass}${cssVideoOrEmbed}" style="${background}">
                [#if embed == true]
                    <div class="story-video-base" style="${embedBackground!}">
                        <div class="responsive-wrapper-16x9 video-background">
                            ${story.embedsource!}
                        </div>
                    </div>
                [#elseIf video == true]
                <div class="story-video-base">
                    <video autoplay loop>
                        <source src="${videoRendition?hasContent?then(videoRendition.link!, "")}">
                        [#if story.videoimage?hasContent]
                            <img src="${damfn.getAssetLink(story.videoimage)}"/>
                        [#else]
                            ${i18n['stories.page.browser.not.support.video.tag']}
                        [/#if]
                    </video>
                </div>
                [/#if]

            <div class="title-wrapper">
                <h2>${story.title!}</h2>
            </div>
                <div class="story-teaser">
                    <span class="triangle"><img src="${ctx.contextPath}/.resources/travel-demo-stories-app/webresources/img/icon_flight.svg" alt="${i18n['stories.page.discover']}"></span>
                    <div class="story-teaser-text">
                        [#assign lead = story.lead!]
                        [#if lead?hasContent]
                        <div>
                            ${(lead?length>100)?then(lead[0..100]+"...",lead)}
                        </div>
                        [/#if]
                        <div class="story-teaser-call-to-action"><span>${i18n['stories.page.read.story']}</span></div>
                    </div>
                </div>
            </a>
            [#if video || embed]
            </div>
            [/#if]
            [/#if]
    [#else]
        [@editorAlert i18n['stories.page.no.story.given'] /]
    [/#if]
[/#macro]

[#function cssBackground image]
    [#assign rendition = damfn.getRendition(image, "original")!]
    [#return rendition?hasContent?then("background-image: url(${rendition.link!}); background-size: cover; background-position: center;", "")]
[/#function]

[#macro mainStory story]
<div class="col-md-7">
    [@storyLead story "story-main" /]
</div>
[/#macro]

[#macro stories stories]
    [#list stories]
    <div class="col-md-5">
        [#items as story]
            [@storyLead story "story-horizontal"/]
        [/#items]
    </div>
    [/#list]
[/#macro]

[#-------------- RENDERING --------------]
<div class="stories-header">
    <h1>${cmsfn.page(content).title!i18n['stories.page.stories']}</h1>
</div>

[#if content.storiesFolder?hasContent]
    [#assign storiesInFolder = cmsfn.children(cmsfn.contentById(content.storiesFolder, getWorkspace()) , "mgnl:composition")]
[/#if]

[#assign position = "left"]
[#if storiesInFolder?hasContent]
    [#list storiesInFolder?chunk(3) as row]
    <div class="row">
        [#if position == "left"]
            [@mainStory row[0] /]
            [@stories row[1..*2] /]
            [#assign position = "right"]
        [#else]
            [@stories row[0..*2] /]
            [#if row[2]?hasContent]
                [@mainStory row[2] /]
            [/#if]
            [#assign position = "left"]
        [/#if]
    </div>
    [/#list]
[/#if]

<script src="https://player.vimeo.com/api/player.js"></script>
<script language="javascript">

    (function () {
        const players = [];
        const iframes = document.getElementsByTagName('iframe');
        for (let i = 0; i < iframes.length; i++) {
            const iframe = iframes[i];
            const classList = iframe.classList;
            classList.add('opacity-zero');
            players[i] = new Vimeo.Player(iframe);

            players[i].on('timeupdate', function(event) {
                if (event.seconds > 0.1) {
                    classList.remove('opacity-zero');
                    classList.add('opacity-full');
                    players[i].off('timeupdate');
                }
            });
        }
    })()
</script>