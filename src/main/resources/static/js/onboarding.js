// 온보딩 전용 스크립트. 입력을 모아 메인(/)으로 넘긴다.
const P = { area: 10, yearBand: '2000년대', targetTemp: 22, lastGas: null, gasType: null, savingsGoal: 30000 };

function goOb(id) {
    document.querySelectorAll('.ob-panel').forEach(p => p.classList.remove('active'));
    const el = document.getElementById(id);
    if (el) el.classList.add('active');
    window.scrollTo(0, 0);
}
function selOpt(el) {
    const g = el.dataset.g;
    document.querySelectorAll(`[data-g="${g}"]`).forEach(o => o.classList.remove('sel'));
    el.classList.add('sel');
    P[g] = el.dataset.v;
}
function selTime(el, gid) {
    document.querySelectorAll(`#${gid} .time-btn`).forEach(b => b.classList.remove('sel'));
    el.classList.add('sel');
    if (el.dataset.g) P[el.dataset.g] = parseInt(el.dataset.v);
}
function syncYear(v) {
    const m = { '1': '~90년대', '2': '2000년대', '3': '2010년대', '4': '2020년대~' };
    document.getElementById('ob-year-v').textContent = m[v];
    P.yearBand = m[v];
}
function syncGoal(v) {
    document.getElementById('ob-goal-v').textContent = parseInt(v).toLocaleString() + '원';
    P.savingsGoal = parseInt(v);
}

// 완료 → 입력값을 쿼리스트링으로 메인에 전달
function finishOb() {
    const areaEl = document.getElementById('ob-area');
    const tgtEl = document.getElementById('ob-tgt');
    const gasEl = document.getElementById('ob-gas');
    const goalEl = document.getElementById('ob-goal');
    if (areaEl) P.area = parseInt(areaEl.value);
    if (tgtEl) P.targetTemp = parseFloat(tgtEl.value);
    if (gasEl) P.lastGas = gasEl.value ? parseInt(gasEl.value) : null;
    if (goalEl) P.savingsGoal = parseInt(goalEl.value);

    const rows = [
        ['평수', P.area + '평'],
        ['건물 연식', P.yearBand],
        ['선호 온도', P.targetTemp + '°C'],
        ['지난달 가스비', P.lastGas ? P.lastGas.toLocaleString() + '원' : '미입력'],
        ['월 절약 목표', P.savingsGoal.toLocaleString() + '원'],
    ];
    const box = document.getElementById('profile-box');
    if (box) box.innerHTML = rows.map(([k, v]) =>
        `<div class="prow"><span class="pk">${k}</span><span class="pv">${v}</span></div>`).join('');
    goOb('ob-done');
}

function skipAll() { goToApp(); }

// 메인 화면으로 이동하며 프로필을 쿼리로 전달
function goToApp() {
    const q = new URLSearchParams();
    q.set('area', P.area);
    if (P.lastGas) q.set('lastGas', P.lastGas);
    if (P.savingsGoal) q.set('savingsGoal', P.savingsGoal);
    window.location.href = '/check?' + q.toString();
}

function sInsul() {} // 온보딩에선 미사용 (호환용 빈 함수)
