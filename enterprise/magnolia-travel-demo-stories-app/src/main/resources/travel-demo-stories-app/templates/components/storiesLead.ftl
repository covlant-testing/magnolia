[#-- Displays an overview list of the stories --]

[#-------------- ASSIGNMENTS --------------]
[#include "/travel-demo/templates/macros/editorAlert.ftl"]
[#include "/travel-demo-stories-app/templates/shared/functions.ftl"]


[#macro storyLead storyId cssClass]
    [#if storyId?hasContent]
        [#assign story = cmsfn.contentById(storyId, getWorkspace())]

        [#if story?hasContent]

            [#assign background = "background-color: #efefef;"]
            [#if story.embedimage?hasContent]
                [#assign rendition = damfn.getRendition(story.embedimage, "original")]
                [#assign background = "background-image: url(${rendition.link}); background-size: cover; background-position: center;"]
            [/#if]

            <a href="${storyLink(content, story)!"#"}" class="story ${cssClass}" style="${background}">
                <h2>${story.title!}</h2>

                <div class="story-teaser">
                    <span class="triangle"><img src="${ctx.contextPath}/.resources/travel-demo-stories-app/webresources/img/icon_flight.svg" alt="${i18n['stories.page.discover']}"></span>
                    <div class="story-teaser-text">
                        [#assign lead = story.lead]
                        [#if lead?hasContent]
                            ${(lead?length>100)?then(lead[0..100]+"...",lead)}
                        [/#if]
                    </div>
                    <div class="story-teaser-call-to-action"><span>${i18n['stories.page.read.story']}</span></div>
                </div>
            </a>
        [/#if]
    [#else]
        [@editorAlert i18n['stories.page.no.story.given'] /]
    [/#if]
[/#macro]


[#-------------- RENDERING --------------]
<div class="stories-header">
    <h1>${cmsfn.page(content).title!i18n['stories.page.stories']}</h1>
</div>

<div class="row">
    <div class="col-md-7">
        [@storyLead content.story1 "story-main" /]
    </div>
    <div class="col-md-5">
        [@storyLead content.story2 "story-horizontal" /]
        [@storyLead content.story3 "story-vertical" /]
    </div>
</div>
