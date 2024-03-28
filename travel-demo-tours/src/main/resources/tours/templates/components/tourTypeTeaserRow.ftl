[#-------------- ASSIGNMENTS --------------]
[#include "/tours/templates/macros/tourTypeIcon.ftl" /]

[#assign tourTypes = model.tours]


[#-------------- RENDERING --------------]
<!-- TourType Teaser Row -->
<div class="container category-card-row after-lead-image">
    <div class="row">

        <h2>${content.title!}</h2>
        <p>${content.body!}</p>

        [#list tourTypes as tourType]
            <a class="category-card-anchor" href="${tourType.link!'#'}">
                <div class="col-md-4 category-card">
                    <div class="category-icon absolute-center-container">
                        [@tourTypeIcon tourType.icon tourType.name "absolute-center" /]
                    </div>
                    <h3>${tourType.name!}</h3>
                    <div class="category-card-content">
                        <p>${tourType.description!}</p>
                    </div>
                </div>
            </a>
        [/#list]

    </div>
</div>
