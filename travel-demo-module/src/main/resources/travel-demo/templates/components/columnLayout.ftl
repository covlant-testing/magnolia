[#-------------- ASSIGNMENTS --------------]
[#assign layout = content.layout!"6x6"]
[#assign layouts = layout?split("x")]
[#assign columnCount = layouts?size]

[#-------------- RENDERING --------------]
<div class="row">
    [#list 1..columnCount as columnIndex]
    <div class="col-sm-${layouts[columnIndex - 1]}">
        [@cms.area name="column${columnIndex}"/]
    </div>
    [/#list]
</div>
