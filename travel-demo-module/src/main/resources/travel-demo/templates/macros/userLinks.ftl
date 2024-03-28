[#macro userLinks username logoutLink loginPageLink profilePageLink registrationPageLink ]

<div id="user-links">
        [#if username != ""]
            [#if logoutLink != ""]<a href="${logoutLink}">${i18n['navigation.logout']}</a>[/#if]
            [#if profilePageLink != ""]<a href="${profilePageLink!}">${username}</a>[/#if]
        [#else]
            [#if loginPageLink != ""]<a href="${loginPageLink!}">${i18n['navigation.login']}</a>[/#if]
            [#if registrationPageLink != ""]<a href="${registrationPageLink!}">${i18n['navigation.registration']}</a>[/#if]
        [/#if]
</div>

[/#macro]
