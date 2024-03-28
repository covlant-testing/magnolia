[#-- Extends the basic template behavior by adding the ability to choose a personalized tour type --]

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
            <a class="category-card-anchor" href="${tourType.link!'#'}" onclick="setTourTypeAsCookie('${tourType.nodeName}')">
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
    <script type="text/javascript">
        function setTourTypeAsCookie(tourType) {
            [#-- Always remove cookie when we're about to set it or we might end up
            with multiple cookies with the same name.
            The empty path means remove for the whole site not for some specific path. --]
            Cookies.remove('tourType', { path: '' });

            [#if !cmsfn.editMode]
            [#-- Only set cookie when we're NOT in edit mode. --]
            Cookies.set('tourType', tourType)
            [/#if]
        }
    </script>
</div>
