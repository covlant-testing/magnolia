[#-- Extends the basic template behavior by getting  featured tours by personalization cookie trait --]
[#-------------- ASSIGNMENTS --------------]
[#include "/travel-demo-component-personalization/templates/macros/detectCookie.ftl" /]

[#assign cookie = detectCookie("tourType")! /]

[#-- Disable caching because the content is generated dynamically according to the tour type cookie. --]
${ctx.response.setHeader("Cache-Control", "no-cache")}

[#assign page = cmsfn.page(content)]
[#if cookie?has_content && cmsfn.hasTemplateOfType(page, "home")]
    [#assign category = model.getCategoryByName(cookie)!]
[#else]
    [#assign category = model.getCategoryByUrl()!]
[/#if]


[#-------------- RENDERING --------------]
[#if category?has_content]
<!-- Tour List - Featured Row -->
    [#include "/travel-demo-component-personalization/templates/macros/personalizedRelatedTours.ftl"]
    [#assign tours = model.getRelatedToursByCategory(category.identifier)]
    [@relatedTours category.name tours /]

<div class="container category-overview">
    [#if category.body?has_content]
        <div class="category-body">
        ${category.body}
        </div>
    [/#if]
</div>
[/#if]
