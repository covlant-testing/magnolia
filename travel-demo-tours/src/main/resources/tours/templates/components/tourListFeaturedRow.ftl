[#-------------- ASSIGNMENTS --------------]
[#assign category = model.categoryByUrl!]


[#-------------- RENDERING --------------]
[#if category?has_content]
    <!-- Tour List - Featured Row -->
    [#include "/tours/templates/macros/relatedTours.ftl"]
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
