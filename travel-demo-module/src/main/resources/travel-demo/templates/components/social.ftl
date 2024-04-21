[#-------------- INCLUDE AND ASSIGN PART --------------]
[#include "/mtk2/templates/includes/init.ftl"]

[#assign size = content.size!'32']
[#assign style = content.vertical?boolean?string('a2a_vertical_style', 'a2a_default_style')]
[#assign floating = '']
[#if content.floating?boolean]
    [#assign floating = 'a2a_floating_style']
    [#if content.vertical?boolean]
        [#assign additionalFloatingStyles = "left:0px; top:150px;"]
    [#else]
        [#assign additionalFloatingStyles = "bottom:0px; right:0px;"]
    [/#if]
[/#if]
[#assign services = content.services![]]
[#if !content.rounded?boolean]
  [#assign square_icons = 'square-icons']
[/#if]

<!-- AddToAny BEGIN -->
<div class="a2a_kit a2a_kit_size_${size} ${floating} ${style} social-sharing ${square_icons!}" style="${additionalFloatingStyles!}">
<a class="a2a_dd" href="https://www.addtoany.com/share_save"></a>
[#list services as service]
  <a class="a2a_button_${service}"></a>
[/#list]
</div>
<!-- AddToAny END -->
