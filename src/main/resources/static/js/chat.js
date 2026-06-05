let messages = [];
let pendingSettings = null;
let isWaiting = false;

async function callAPI(userMessage, showUserMsg) {
    if (isWaiting) return;
    isWaiting = true;
    setInputEnabled(false);

    if (showUserMsg) addMessage('user', userMessage);
    showTyping();

    try {
        const res = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ messages, newMessage: userMessage })
        });
        if (!res.ok) throw new Error('server error');
        const data = await res.json();

        messages.push({ role: 'user',      content: userMessage });
        messages.push({ role: 'assistant', content: data.reply  });

        removeTyping();
        addMessage('ai', data.reply);

        if (data.settingsReady && data.settings) {
            pendingSettings = data.settings;
            document.getElementById('apply-wrap').style.display = 'block';
            return;
        }
    } catch {
        removeTyping();
        addMessage('ai', '연결 오류가 발생했어요. 잠시 후 다시 시도해주세요 😅');
    }

    isWaiting = false;
    setInputEnabled(true);
}

function sendMessage() {
    const input = document.getElementById('chat-input');
    const text = input.value.trim();
    if (!text || isWaiting) return;
    input.value = '';
    callAPI(text, true);
}

function setInputEnabled(on) {
    const btn = document.getElementById('send-btn');
    const inp = document.getElementById('chat-input');
    if (btn) btn.disabled = !on;
    if (inp) inp.disabled = !on;
}

function addMessage(type, content) {
    const body = document.getElementById('chat-body');
    const div  = document.createElement('div');
    div.className = `chat-msg chat-msg-${type}`;
    const safe = content
        .replace(/&/g,  '&amp;')
        .replace(/</g,  '&lt;')
        .replace(/>/g,  '&gt;')
        .replace(/\n/g, '<br>');
    div.innerHTML = `<div class="chat-bubble">${safe}</div>`;
    body.appendChild(div);
    body.scrollTop = body.scrollHeight;
}

function showTyping() {
    const body = document.getElementById('chat-body');
    const div  = document.createElement('div');
    div.className = 'chat-msg chat-msg-ai';
    div.id = 'typing-indicator';
    div.innerHTML = '<div class="chat-bubble chat-typing"><span></span><span></span><span></span></div>';
    body.appendChild(div);
    body.scrollTop = body.scrollHeight;
}

function removeTyping() {
    const el = document.getElementById('typing-indicator');
    if (el) el.remove();
}

function applySettings() {
    if (!pendingSettings) return;
    localStorage.setItem('only_settings', JSON.stringify(pendingSettings));
    location.href = '/main';
}

function startChat(topic) {
    const picker = document.getElementById('concern-picker');
    const body = document.getElementById('chat-body');
    const inputArea = document.getElementById('chat-input-area');
    if (picker) picker.style.display = 'none';
    if (body) body.style.display = 'flex';
    if (inputArea) inputArea.style.display = 'flex';
    setInputEnabled(false);
    callAPI('__START__:' + topic, false);
}

window.addEventListener('DOMContentLoaded', () => {
    const topic = new URLSearchParams(location.search).get('topic');
    if (topic) {
        startChat(topic);
    }
});
