(function () {
  var ENDPOINT = '/.rest/chatbot/v1/turn';
  var STORAGE_KEY = 'mgnl-chatbot-transcript';

  function el(tag, cls, txt) {
    var e = document.createElement(tag);
    if (cls) e.className = cls;
    if (txt) e.textContent = txt;
    return e;
  }

  function loadTranscript() {
    try { return JSON.parse(sessionStorage.getItem(STORAGE_KEY) || '[]'); } catch (e) { return []; }
  }

  function saveTranscript(arr) {
    try { sessionStorage.setItem(STORAGE_KEY, JSON.stringify(arr)); } catch (e) {}
  }

  function appendBubble(panel, role, text) {
    var b = el('div', 'mgnl-chatbot-msg mgnl-chatbot-msg--' + role);
    b.appendChild(el('div', 'mgnl-chatbot-msg__text', text));
    var log = panel.querySelector('.mgnl-chatbot-log');
    log.appendChild(b);
    log.scrollTop = log.scrollHeight;
  }

  function send(panel, message) {
    var log = panel.querySelector('.mgnl-chatbot-log');
    var thinking = el('div', 'mgnl-chatbot-msg mgnl-chatbot-msg--thinking', panel.dataset.thinking || '...');
    log.appendChild(thinking);
    fetch(ENDPOINT, {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userMessage: message })
    }).then(function (r) {
      thinking.remove();
      if (!r.ok) {
        appendBubble(panel, 'error', panel.dataset.errorGeneric || 'Something went wrong.');
        return;
      }
      return r.json();
    }).then(function (data) {
      if (!data) return;
      appendBubble(panel, 'assistant', data.assistantMessage || '');
      var transcript = loadTranscript();
      transcript.push({ role: 'user', text: message });
      transcript.push({ role: 'assistant', text: data.assistantMessage });
      saveTranscript(transcript);
    }).catch(function () {
      thinking.remove();
      appendBubble(panel, 'error', panel.dataset.errorGeneric || 'Something went wrong.');
    });
  }

  function buildPanel(opts) {
    var panel = el('div', 'mgnl-chatbot mgnl-chatbot--' + opts.mode);
    panel.dataset.locale = opts.locale || 'en';
    panel.dataset.thinking = opts.thinkingLabel || '...';
    panel.dataset.errorGeneric = opts.errorLabel || 'Something went wrong.';
    panel.appendChild(el('div', 'mgnl-chatbot-log'));
    var form = el('form', 'mgnl-chatbot-form');
    var input = el('input', 'mgnl-chatbot-input');
    input.type = 'text';
    input.placeholder = opts.placeholder || 'Ask anything...';
    var button = el('button', 'mgnl-chatbot-submit', opts.sendLabel || 'Send');
    button.type = 'submit';
    form.appendChild(input);
    form.appendChild(button);
    panel.appendChild(form);
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      var v = input.value.trim();
      if (!v) return;
      input.value = '';
      appendBubble(panel, 'user', v);
      send(panel, v);
    });
    var transcript = loadTranscript();
    for (var i = 0; i < transcript.length; i++) {
      appendBubble(panel, transcript[i].role, transcript[i].text);
    }
    return panel;
  }

  window.MagnoliaChatbot = {
    init: function (opts) {
      var mount = document.querySelector(opts.mountSelector);
      if (!mount) return;
      var panel = buildPanel(opts);
      if (opts.mode === 'widget') {
        var launcher = el('button', 'mgnl-chatbot-launcher', '\uD83D\uDCAC');
        launcher.type = 'button';
        var wrap = el('div', 'mgnl-chatbot-widget');
        wrap.style.display = 'none';
        wrap.appendChild(panel);
        mount.appendChild(launcher);
        mount.appendChild(wrap);
        launcher.addEventListener('click', function () {
          wrap.style.display = wrap.style.display === 'none' ? 'block' : 'none';
        });
      } else {
        mount.appendChild(panel);
      }
    }
  };
})();
