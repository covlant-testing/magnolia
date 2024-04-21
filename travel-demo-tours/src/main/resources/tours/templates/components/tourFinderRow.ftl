[#-------------- ASSIGNMENTS --------------]
[#if content.tourFinder??]
    [#assign tourFinderLink = cmsfn.link(cmsfn.contentById(content.tourFinder))]
[/#if]


[#-------------- RENDERING --------------]
<!-- Tour Finder -->
<div class="container">
    <div class="row">
        <div class="finder-home col-md-12">
            <form action="${tourFinderLink!'/'}" method="get"
                onsubmit="location.href=this.action+'#!/?q='+this.searchQuery.value+'&duration='+this.duration.value+'&tourTypes='+this.tourTypes.value; return false">
            <div class="finder-home-segment-icon"><img src="${ctx.contextPath}/.resources/tours/webresources/img/map_red.png"></div>
            <div class="finder-home-segment" style="flex:1; min-width:200px">
                <div>${i18n['tourFinder.title']}</div>
                <input class="finder-search-home" placeholder="${i18n['tourFinder.search.placeholder']}" type="text" name="searchQuery">
            </div>
            <div class="finder-home-segment">
                <div>${i18n['tourFinderRow.duration']}</div>
                <select name="duration">
                    <option value="">${i18n['tourFinderRow.duration.placeholder']}</option>
                    <option value="2">${i18n['tourFinder.duration.options.2-days']}</option>
                    <option value="7">${i18n['tourFinder.duration.options.7-days']}</option>
                    <option value="14">${i18n['tourFinder.duration.options.14-days']}</option>
                    <option value="21">${i18n['tourFinder.duration.options.21-days']}</option>
                </select>
            </div>
            <div class="finder-home-segment">
                <div>${i18n['tourFinderRow.type']}</div>
                <select name="tourTypes">
                    <option value="">${i18n['tourFinderRow.type.placeholder']}</option>
                    [#assign allTourTypes = navfn.navItemsFromApp("category", "/tour-types", "mgnl:category")]
                    [#list allTourTypes as tourType]
                       <option value="${tourType.@uuid}">${tourType['displayName_' + cmsfn.language()]!tourType.displayName}</option>
                    [/#list]
                </select>
            </div>
            <div class="finder-home-segment-icon">
                <button type="submit" class="btn btn-primary">${i18n['tourFinderRow.submit']}</button>
            </div>
            </form>
        </div>
    </div>
</div>
