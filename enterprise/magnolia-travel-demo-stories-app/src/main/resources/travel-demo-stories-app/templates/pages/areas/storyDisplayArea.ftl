[#-------------- ASSIGNMENTS --------------]
[#-- Displays a single story by resolving story-by-path from given selector --]

[#include "/travel-demo-stories-app/templates/shared/functions.ftl"]


[#-- Displays a 'list' of related stories. --]
[#macro relatedStories stories]

    [#list stories as relatedStoryID]
        [#assign relatedStory = cmsfn.contentById(relatedStoryID, "stories")]
        [#assign rendition = damfn.getRendition(relatedStory.embedimage, "original")]
        [#assign link = storyLink(content, relatedStory)!"#" /]
    <div class="related-story float-container">
        <a href="${link}" style="background-image: url('${rendition.link!}');">
            <h2>${relatedStory.title}</h2>
            <div class="call-to-action">${i18n['story.page.related.stories.view']}</div>
        </a>
    </div>
    [/#list]

[/#macro]


[#assign selector = state.selector!]

[#-- Single story view (when selector is provided) --]
[#if selector?hasContent]
    [#assign story = cmsfn.contentByPath(getStoryFolder() + selector, getWorkspace())]
[/#if]

[#-- Fall back to first story in workspace if none was selected --]
[#if !(story?hasContent)]
    [#assign folder = cmsfn.contentByPath(getStoryFolder(), getWorkspace())]
    [#assign story = cmsfn.children(folder, "mgnl:composition")[0]!]
[/#if]

[#assign storiesLink = storyBackLink(content)!"#"]


[#-------------- RENDERING --------------]

[#if story?hasContent]

    <a href="${storiesLink}"><img class="close-button" alt="close" src="${ctx.contextPath}/.resources/travel-demo-stories-app/webresources/img/icon_close_button.svg"></a>


    [#-------------- ASSIGNMENTS --------------]

    [#assign blocks = cmsfn.children(story, "mgnl:block")]

    [#-- Get the first text block--]
    [#list blocks as block]
        [#if block["mgnl:type"] == "text"]
            [#assign firstTextBlock = block]
            [#break]
        [/#if]
    [/#list]

    [#if firstTextBlock?hasContent]
        [#assign firstText = firstTextBlock.text]
        [#if firstText?hasContent]
            [#assign firstTextTruncated = firstText[0..*75]!]
        [/#if]
    [/#if]


    [#-------------- RENDERING --------------]

    [#-- Something to take up the proper height of the responsive video size. --]
    <div class="responsive-wrapper-16x9 video-spacer">
        <div class="video-background-color"></div>
    </div>

    <div class="story-header-videos">
        <div class="story-video-base">
            <div class="responsive-wrapper-16x9 video-background">
            ${story.embedsource}
            </div>
        </div>
    </div>

    <div class="story-header responsive-wrapper-16x9">
        <div class="responsive-element">

            <h1 class="story-title">${story.title}</h1>

            <div class="story-circle-line">
                <figure class="clock">
                    <svg preserveAspectRatio="xMinYMin meet" viewBox="0 0 640 360">
                        <circle cx="320" cy="180" r="65"/>
                    </svg>
                    <div class="clock-text-wrapper">
                        <div class="clock-date clock-text">
                            <span class="clock-month">Jan</span> <span class="clock-day odometer">17</span>
                        </div>
                        <div class="clock-time clock-text">
                            <span class="clock-hour odometer">17</span>:<span class="clock-minutes odometer">00</span>
                        </div>
                        <div class="clock-intro clock-text">
                            <div class="clock-intro-inner">
                            ${firstTextTruncated!}...
                            </div>
                        </div>
                    </div>
                </figure>
            </div>

            <div class="story-credits">
                <div class="story-author">${i18n['story.page.published.by']} ${story.author!}</div>
                <div class="story-author-meta">
                    <time datetime="${story.created?isoLocal}">${story.created?date?string.long}</time> / ${story.location!}
                </div>
            </div>

        </div>
    </div>

    <!--story-header-->

    <figure id="clock-widget" class="clock-side">
        <svg preserveAspectRatio="xMinYMin meet" viewBox="0 0 100 100">
            <circle class="clock-gauge-track" cx="50" cy="50" r="45"/>
            <circle class="clock-gauge" cx="50" cy="50" r="45"/>
        </svg>
        <div class="clock-text-wrapper">
            <div class="clock-date clock-text">
                <span class="clock-month">Jan</span> <span class="clock-day odometer">17</span>
            </div>
            <div class="clock-time clock-text">
                <span class="clock-hour odometer">17</span>:<span class="clock-minutes odometer">00</span>
            </div>
        </div>
    </figure>

    <div class="story-line"></div>

    <div class="author">
        [#-- Lead image (uses mtk image macro) --]
        [#if story.authorImage?hasContent]
            [#include "/mtk/templates/macros/image.ftl"]
            [#assign rendition = damfn.getRendition(story.authorImage, "author")]
            [@image rendition story "author-image" false {} /]
        [/#if]

        ${story.authorBio!}
    </div>


    <div class="story-content">

        <div class="story-lead">
        ${story.lead!}
        </div>

        [#list blocks as block]
            [@cms.block content=block /]
        [/#list]

    </div>

    [#if story.tours?hasContent]

        [#include "/tours/templates/macros/tourTeaser.ftl" /]

        <section class="related-stories">
            <h3>${i18n['story.page.related.tours']}</h3>

            [#list story.tours as tourId]
                [#assign tourNode = cmsfn.contentById(tourId, "tours")]
                [#assign tour = tourfn.marshallTourNode(tourNode)]
                <div class="related-tour">
                    [@tourTeaser tour "" /]
                </div>
            [/#list]
        </section>

    [/#if]


    [#if story.stories?hasContent]
        <section class="related-stories">
            <h3>${i18n['story.page.related.stories']}</h3>

            [@relatedStories story.stories /]
        </section>
    [/#if]

[#else]

    <p>${i18n['story.page.no.story.given']}</p>

[/#if]


<p class="return-link">
    <a href="${storiesLink}">${i18n['story.page.back.link']}</a>
</p>

[#-- This is such a simple footer, let's just put it here: --]
<footer>
    <a href="${storiesLink}">
        <img class="close-button-footer" alt="close" src="${ctx.contextPath}/.resources/travel-demo-stories-app/webresources/img/icon_close_button.svg">
    </a>
</footer>
