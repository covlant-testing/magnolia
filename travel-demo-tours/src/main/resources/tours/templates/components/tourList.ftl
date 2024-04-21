[#-------------- ASSIGNMENTS --------------]
[#include "/tours/templates/macros/tourTeaser.ftl"]
[#include "/travel-demo/templates/macros/editorAlert.ftl" /]

[#if def.parameters.tourType??]
    [#assign category = model.getCategoryByName(def.parameters.tourType)]
[#else]
    [#assign category = model.getCategoryByUrl()!]
[/#if]

[#assign tours = model.getToursByCategory(category.identifier)]
[#assign title = content.title!i18n.get('tour.all.tours', [category.name!""])!]

[#-------------- RENDERING --------------]
<!-- Tour List -->
<div class="container tour-list">

    <h2>${title}</h2>

    <div class="row">
        [#list tours as tour]
            [@tourTeaser tour /]
        [/#list]
    </div>

    [@editorAlert i18n.get('note.for.editors.assign.category', [category.name!""]) /]
</div>

<script>
    jQuery(".tour-card-image").objectFitCoverSimple();
</script>
