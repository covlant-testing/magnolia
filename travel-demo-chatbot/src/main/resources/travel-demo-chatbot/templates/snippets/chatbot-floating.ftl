[#assign locale = ctx.locale!"en"]
<link rel="stylesheet" href="${ctx.contextPath}/.resources/travel-demo-chatbot-theme/css/chatbot.css"/>
<div id="mgnl-chatbot-floating"></div>
<script src="${ctx.contextPath}/.resources/travel-demo-chatbot-theme/js/chatbot.js"></script>
<script>
  MagnoliaChatbot.init({
    mountSelector: '#mgnl-chatbot-floating',
    mode: 'widget',
    locale: '${locale}',
    placeholder: '${i18n["chatbot.placeholder"]!""?js_string}',
    sendLabel: '${i18n["chatbot.send"]!""?js_string}',
    thinkingLabel: '${i18n["chatbot.thinking"]!""?js_string}',
    errorLabel: '${i18n["chatbot.error"]!""?js_string}',
    endpointUrl: '${ctx.contextPath}/.rest/chatbot/v1/turn'
  });
</script>
