[#-- Displays an overview list of the stories --]

[#-------------- ASSIGNMENTS --------------]
[#include "/travel-demo/templates/macros/editorAlert.ftl"]
[#include "/travel-demo-stories-app/templates/shared/functions.ftl"]


[#macro storyLead story cssClass]
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
    [#else]
        [@editorAlert i18n['stories.page.no.story.given'] /]
    [/#if]
[/#macro]

[#macro mainStory story]
<div class="col-md-7">
    [@storyLead story "story-main" /]
</div>
[/#macro]

[#macro stories stories]
    [#list stories]
    <div class="col-md-5">
        [#items as story]
            [#if story?index == 0]
            [#assign horizontal = true]
        [#else]
            [#assign horizontal = false]
        [/#if]
            [@storyLead story horizontal?then("story-horizontal", "story-vertical") /]
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
