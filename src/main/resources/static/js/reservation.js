let rvPrefStep = 3;
let previewTimer = null;

// ── 슬라이더 레이블 동기화 ──
function onSlider() {
    const t = parseFloat(document.getElementById('rv-temp').value);
    const o = parseFloat(document.getElementById('rv-out').value);
    document.getElementById('rv-temp-v').textContent = t + '°C';
    document.getElementById('rv-out-v').textContent = (o >= 0 ? '' : '') + o + '°C';
    schedulePreview();
}

// ── 선호 단계 선택 ──
function setRvPref(step) {
    rvPrefStep = step;
    for (let i = 1; i <= 5; i++) {
        const el = document.getElementById('rvp' + i);
        if (el) el.classList.toggle('active', i === step);
    }
    schedulePreview();
}

// ── 프리뷰 디바운스 (400ms) ──
function schedulePreview() {
    clearTimeout(previewTimer);
    previewTimer = setTimeout(fetchPreview, 400);
}

// ── 서버에 계산 요청 → 프리뷰 표시 ──
async function fetchPreview() {
    const body = {
        indoorTemp: parseFloat(document.getElementById('rv-temp').value),
        indoorHum: 45,
        outdoorTemp: parseFloat(document.getElementById('rv-out').value),
        outdoorHum: 60,
        wind: 0,
        area: 30,
        insul: '2',
        prefStep: rvPrefStep
    };
    try {
        const res = await fetch('/api/calc', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!res.ok) throw new Error();
        showPreview(await res.json());
    } catch {
        document.getElementById('rv-preview').innerHTML =
            '<div class="rv-preview-err">서버 연결 오류 — 앱을 실행 중인지 확인하세요</div>';
    }
}

// ── 프리뷰 렌더링 ──
function showPreview(r) {
    const arrival = document.getElementById('rv-arrival').value || '18:30';
    const [h, m] = arrival.split(':').map(Number);
    const runtime = Math.max(15, r.runtimeMin);
    const buffer = 10;
    const sd = new Date();
    sd.setHours(h, m - runtime - buffer, 0, 0);
    const startStr = String(sd.getHours()).padStart(2, '0') + ':' + String(sd.getMinutes()).padStart(2, '0');

    const stateColor = { heating: 'var(--warm)', approaching: 'var(--accent)', reached: 'var(--green)' };
    const stateLabel = { heating: '난방 필요', approaching: '도달 임박', reached: '이미 쾌적' };
    const statePillClass = { heating: '', approaching: 'blue', reached: 'green' };
    const color = stateColor[r.boilerState] || 'var(--accent)';

    document.getElementById('rv-preview').innerHTML = `
        <div class="rv-preview-box">
            <div class="rv-prev-row">
                <div class="rv-prev-item">
                    <div class="rv-prev-label">현재 체감온도</div>
                    <div class="rv-prev-val" style="color:${color}">${r.feelsLike.toFixed(1)}°C</div>
                </div>
                <div class="rv-prev-arrow">→</div>
                <div class="rv-prev-item">
                    <div class="rv-prev-label">목표 온도</div>
                    <div class="rv-prev-val">${r.myTarget.toFixed(1)}°C</div>
                </div>
            </div>
            <div class="rv-prev-result">
                <div class="rv-prev-clock">🕐 <strong>${startStr}</strong>에 보일러를 켜세요</div>
                <div class="rv-prev-sub">${runtime}분 가동 + ${buffer}분 여유 → ${arrival} 도착 시 쾌적</div>
            </div>
            <div class="rv-prev-state">
                <span class="rv-state-pill ${statePillClass[r.boilerState] || ''}">${stateLabel[r.boilerState] || r.boilerState}</span>
                <span class="rv-save-pill">일 절감 ${(r.saveDaily + 620).toLocaleString()}원 예상</span>
            </div>
        </div>`;
}

// ── 예약 추가 ──
async function addReservation() {
    const btn = document.getElementById('rv-add-btn');
    btn.disabled = true;
    btn.textContent = '추가 중...';

    const body = {
        arrivalTime: document.getElementById('rv-arrival').value || '18:30',
        prefStep: rvPrefStep,
        indoorTemp: parseFloat(document.getElementById('rv-temp').value),
        indoorHum: 45,
        outdoorTemp: parseFloat(document.getElementById('rv-out').value),
        area: 30,
        insul: '2',
        label: document.getElementById('rv-label-input').value.trim() || '귀가 예약'
    };

    try {
        const res = await fetch('/api/reservation', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!res.ok) throw new Error();
        await loadReservations();
        document.getElementById('rv-label-input').value = '';
        btn.textContent = '✓ 예약 추가됨!';
        setTimeout(() => { btn.textContent = '예약 추가하기'; btn.disabled = false; }, 1600);
    } catch {
        btn.textContent = '오류 — 다시 시도';
        btn.disabled = false;
    }
}

// ── 예약 삭제 ──
async function deleteReservation(id) {
    try {
        await fetch(`/api/reservation/${id}`, { method: 'DELETE' });
        await loadReservations();
    } catch (e) {
        console.error('삭제 실패', e);
    }
}

// ── 예약 목록 렌더링 ──
async function loadReservations() {
    const res = await fetch('/api/reservations');
    const list = await res.json();
    const container = document.getElementById('rv-list');

    if (!list.length) {
        container.innerHTML = `
            <div class="rv-empty">
                <div class="rv-empty-icon">📅</div>
                <div class="rv-empty-msg">예약이 없습니다<br>위에서 귀가 시간을 설정하고<br>예약을 추가해 보세요</div>
            </div>`;
        return;
    }

    const prefIcons = ['', '❄️', '🌿', '✨', '☀️', '🔥'];
    container.innerHTML = list.map(r => `
        <div class="rv-item">
            <div class="rv-item-top">
                <div class="rv-item-icon">🏠</div>
                <div class="rv-item-info">
                    <div class="rv-item-title">${escHtml(r.label)}</div>
                    <div class="rv-item-sub">${r.scheduledDate} &middot; ${prefIcons[r.prefStep] || ''} ${r.prefStep}단계</div>
                </div>
                <button class="rv-delete-btn" onclick="deleteReservation('${r.id}')">삭제</button>
            </div>
            <div class="rv-item-times">
                <div class="rv-time-chip start">
                    <div class="rv-tc-label">보일러 시작</div>
                    <div class="rv-tc-val">${r.startTime}</div>
                </div>
                <div class="rv-time-arrow">→</div>
                <div class="rv-time-chip arrive">
                    <div class="rv-tc-label">귀가 시간</div>
                    <div class="rv-tc-val">${r.arrivalTime}</div>
                </div>
            </div>
            <div class="rv-item-footer">
                ⏱️ ${r.runtimeMin}분 가동 + ${r.bufferMin}분 여유
            </div>
        </div>`).join('');
}

// ── 통계 로드 ──
async function loadStats() {
    try {
        const s = await fetch('/api/stats').then(r => r.json());

        document.getElementById('stat-runtime').textContent = s.todayRuntimeMin + '분';
        document.getElementById('stat-save').textContent = s.todaySave.toLocaleString() + '원';
        document.getElementById('stat-logs').textContent = s.todayLogs + '회';

        if (s.lastBoilerState && s.lastBoilerState !== 'none') {
            const labels = { heating: '보일러 가동 중', approaching: '목표 도달 임박', reached: '목표 달성' };
            const colors = { heating: 'var(--warm)', approaching: 'var(--accent)', reached: 'var(--green)' };
            const el = document.getElementById('rv-last-state');
            if (el) el.style.display = 'flex';
            const dot = document.getElementById('rv-last-dot');
            if (dot) dot.style.background = colors[s.lastBoilerState] || '#9AB8CC';
            const lbl = document.getElementById('rv-last-label');
            if (lbl) lbl.textContent = labels[s.lastBoilerState] || s.lastBoilerState;
            const time = document.getElementById('rv-last-time');
            if (time && s.recent && s.recent.length > 0) time.textContent = s.recent[0].loggedAt;
        }

        renderHistory(s.recent);
    } catch (e) {
        console.error('통계 로드 실패', e);
    }
}

// ── 히스토리 렌더링 ──
function renderHistory(history) {
    const container = document.getElementById('rv-history');
    if (!history || !history.length) {
        container.innerHTML = `
            <div class="rv-empty">
                <div class="rv-empty-icon">🕐</div>
                <div class="rv-empty-msg">메인 화면에서 계산하면<br>이력이 여기 표시됩니다</div>
            </div>`;
        return;
    }

    const colors = { heating: 'var(--warm)', approaching: 'var(--accent)', reached: 'var(--green)' };
    const labels = { heating: '가열 중', approaching: '도달 임박', reached: '목표 달성' };

    container.innerHTML = `
        <div class="rv-history-list">
            ${history.map(h => `
                <div class="rv-hist-item">
                    <div class="rv-hist-dot" style="background:${colors[h.boilerState] || '#9AB8CC'}"></div>
                    <div class="rv-hist-info">
                        <div class="rv-hist-main">${labels[h.boilerState] || h.boilerState} &middot; 체감 ${h.feelsLike.toFixed(1)}°C → 목표 ${h.myTarget.toFixed(1)}°C</div>
                        <div class="rv-hist-sub">${h.runtimeMin}분 &middot; ${h.loggedAt}</div>
                    </div>
                    <div class="rv-hist-save">+${h.saveDaily.toLocaleString()}원</div>
                </div>`).join('')}
        </div>`;
}

function escHtml(str) {
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

// ── 초기화 ──
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('rv-arrival').addEventListener('change', schedulePreview);
    fetchPreview();
    loadReservations();
    loadStats();
    setInterval(loadStats, 30000);
});
