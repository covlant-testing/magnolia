[#assign locale = ctx.locale!"en"]
[#assign mid = "mgnl-chatbot-component-" + content.@uuid?html]
<link rel="stylesheet" href="${ctx.contextPath}/.resources/travel-demo-chatbot-theme/css/chatbot.css"/>
<div id="${mid}"></div>
<script src="${ctx.contextPath}/.resources/travel-demo-chatbot-theme/js/chatbot.js"></script>
<script>
  MagnoliaChatbot.init({
    mountSelector: '#${mid}',
    mode: 'component',
    locale: '${locale}',
    placeholder: '${i18n["chatbot.placeholder"]!""?js_string}',
    sendLabel: '${i18n["chatbot.send"]!""?js_string}',
    thinkingLabel: '${i18n["chatbot.thinking"]!""?js_string}',
    errorLabel: '${i18n["chatbot.error"]!""?js_string}'
  });
</script>
