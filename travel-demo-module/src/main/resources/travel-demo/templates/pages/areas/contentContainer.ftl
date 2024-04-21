[#-------------- ASSIGNMENTS --------------]
[#assign cssClass=def.parameters.cssClass!"col-lg-12"]

[#-------------- RENDERING --------------]
<div class="${cssClass}">
[#list components as component ]
    [@cms.component content=component /]
[/#list]
</div>
