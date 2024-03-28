[#-------------- ASSIGNMENTS --------------]
[#assign cssClass=def.parameters.cssClass!"col-lg-12"]

[#-------------- RENDERING --------------]
<div class="${cssClass}">
[@cms.component content=component /]
</div>
