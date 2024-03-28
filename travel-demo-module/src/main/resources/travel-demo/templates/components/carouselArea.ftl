[#list components as component ]

    [#-- When in edit mode we use a 6x6 grid to simplify working with the carousel items --]
    [#if cmsfn.editMode]
        <div class="col-md-6">
    [#else]
        <div class="item">
    [/#if]

    [@cms.component content=component /]

    </div>

[/#list]
