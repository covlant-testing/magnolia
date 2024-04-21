<div class="finder-background" style="background-image: url(${ctx.contextPath}/.resources/tours/webresources/img/tour-finder-background-ross-parmly-25230.jpg);"></div>

<div class="finder-container" ng-app="TourFinder">
    <div ng-view>
    </div>
</div>

<script type="text/javascript">

[#assign keys = ["tourFinder.search.placeholder",
                 "tourFinder.search.resultsFound",
                 "tourFinder.search.noResults1",
                 "tourFinder.search.noResults2",
                 "tourFinder.search.noResults3",
                 "tourFinder.filter.duration",
                 "tourFinder.filter.destination",
                 "tourFinder.filter.type",
                 "tourFinder.duration.options.2-days",
                 "tourFinder.duration.options.7-days",
                 "tourFinder.duration.options.14-days",
                 "tourFinder.duration.options.21-days",
                 "tour.view",
                 "tourFinder.title"] ]

var translations = {
[#list keys as key]
    "${key}" : "${i18n[key]}",
[/#list]
}

TourFinder.init({ contextPath: "${ctx.contextPath}", restBase: "${ctx.contextPath}/.rest/delivery", language: "${cmsfn.language()}", i18n: translations });

</script>
