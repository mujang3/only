const results = { up: null, down: null };

function startCheck(id) {
    const anim = document.getElementById('anim-' + id);
    if (anim) { anim.style.display = 'flex'; }

    setTimeout(() => {
        if (anim) anim.style.display = 'none';
        const confirm = document.getElementById('confirm-' + id);
        if (confirm) confirm.style.display = 'block';
    }, 1000);
}

function setResult(id, ok) {
    results[id] = ok;

    document.getElementById('confirm-' + id).style.display = 'none';

    const badge = document.getElementById('badge-' + id);
    badge.textContent = ok ? '✓ 정상' : '✗ 이상';
    badge.className = 'ck-badge ' + (ok ? 'ok' : 'fail');

    const card = document.getElementById('card-' + id);
    card.className = 'ck-card ' + (ok ? 'done-ok' : 'done-fail');

    const result = document.getElementById('result-' + id);
    result.textContent = ok ? '정상 작동이 확인됐어요 👍' : '작동에 문제가 있어요. 핑거봇 위치를 확인해주세요.';
    result.className = 'ck-result ' + (ok ? 'ok' : 'fail');

    if (id === 'up') {
        setTimeout(() => {
            const cardDown = document.getElementById('card-down');
            const lockMsg = document.getElementById('lock-down');
            cardDown.className = 'ck-card running';
            lockMsg.style.display = 'none';
            setTimeout(() => startCheck('down'), 300);
        }, 600);
    }

    if (results.up !== null && results.down !== null) {
        const finish = document.getElementById('ck-finish');
        const msg = document.getElementById('ck-finish-msg');
        const allOk = results.up && results.down;
        msg.textContent = allOk
            ? '🎉 모든 점검 완료! 온리 : ONLY 준비됐어요.'
            : '⚠️ 일부 버튼에 문제가 있지만 계속 진행할 수 있어요.';
        msg.className = 'ck-finish-msg ' + (allOk ? 'ok' : 'warn');
        finish.style.display = 'block';
    }
}

function goMain() {
    window.location.href = '/main' + window.location.search;
}

window.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
        document.getElementById('card-up').classList.add('running');
        startCheck('up');
    }, 800);
});
