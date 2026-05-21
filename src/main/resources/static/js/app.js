// 온리 : ONLY 프론트엔드 — 계산은 서버(HeatingService)가 한다.
// 이 파일은 (1) 입력값 수집 (2) /api/calc 호출 (3) 결과로 화면 그리기 만 담당.

let outT = null, outH = 60, wind = 0;   // 날씨(외기온/습도/풍속)
let userPrefStep = 3;                    // 선호 쾌적 단계
let lastResult = null;
let savingsGoal = 30000;                 // 월 절약 목표 금액(원) 기본값
const MOCK_SAVE_BASE = 18400;            // 이번 달 누적 절감 목데이터

// ── 슬라이더 라벨 동기화 ──
function ss(id, vid, unit) {
    document.getElementById(vid).textContent = parseFloat(document.getElementById(id).value) + unit;
}
function sInsul() {
    const m = { '1': '불량 (외풍 심함)', '2': '보통', '3': '양호 (신축)' };
    document.getElementById('iInsul-v').textContent = m[document.getElementById('iInsul').value];
}
function setStep(step) {
    userPrefStep = step;
    for (let i = 1; i <= 5; i++) {
        const el = document.getElementById('pref' + i);
        if (el) el.classList.toggle('active', i === step);
    }
    calc();
}

// ── 핵심: 서버에 계산 요청 ──
async function calc() {
    const body = {
        indoorTemp: parseFloat(document.getElementById('iTemp').value),
        indoorHum: parseFloat(document.getElementById('iHum').value),
        outdoorTemp: outT,            // null 가능 (날씨 미연동)
        outdoorHum: outH,
        wind: wind || 0,
        area: window.__AREA__ || 10,
        insul: '2',
        prefStep: userPrefStep
    };

    try {
        const res = await fetch('/api/calc', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!res.ok) throw new Error('서버 응답 오류');
        lastResult = await res.json();
        render(lastResult);
    } catch (e) {
        console.error('계산 요청 실패:', e);
        const msg = document.getElementById('st-msg');
        if (msg) msg.textContent = '계산 서버 연결 실패';
    }
}

// ── 절약 목표 카드 업데이트 ──
function updateGoalCard(saveMonthly) {
    const total = (saveMonthly || 0) + MOCK_SAVE_BASE;
    const targetTxt = document.getElementById('goal-target-txt');
    const pctBadge = document.getElementById('goal-pct-badge');
    const barFill = document.getElementById('goal-bar-fill');
    const savedVal = document.getElementById('goal-saved-val');
    const remainVal = document.getElementById('goal-remain-val');
    const pct = Math.min(100, Math.round((total / savingsGoal) * 100));
    const remain = Math.max(0, savingsGoal - total);
    if (targetTxt) targetTxt.textContent = savingsGoal.toLocaleString() + '원';
    if (pctBadge) { pctBadge.textContent = pct + '%'; pctBadge.style.background = pct >= 80 ? 'var(--green-pale)' : pct >= 50 ? '#FEF9C3' : 'var(--warm-pale)'; pctBadge.style.color = pct >= 80 ? 'var(--green)' : pct >= 50 ? '#854D0E' : 'var(--warm)'; }
    if (barFill) { barFill.style.width = pct + '%'; barFill.style.background = pct >= 80 ? 'var(--green)' : pct >= 50 ? '#EAB308' : 'var(--warm)'; }
    if (savedVal) savedVal.textContent = total.toLocaleString() + '원';
    if (remainVal) { remainVal.textContent = remain > 0 ? remain.toLocaleString() + '원 남음' : '달성! 🎉'; remainVal.style.color = remain === 0 ? 'var(--green)' : 'var(--text)'; }
}

// ── 서버 결과로 화면 렌더링 ──
function render(r) {
    const optimal = r.optimalTemp;
    const myTarget = r.myTarget;
    const fl = r.feelsLike;
    const offsets = { 1: -2, 2: -1, 3: 0, 4: 1, 5: 2 };

    // 선호온도 라벨
    const tgtV = document.getElementById('iTgt-v');
    if (tgtV) tgtV.textContent = `${userPrefStep}단계 · ${myTarget.toFixed(1)}°C`;

    // 단계 버튼 sub 텍스트
    for (let i = 1; i <= 5; i++) {
        const sub = document.querySelector(`#pref${i} .pb-sub`);
        if (!sub) continue;
        const val = Math.round((optimal + offsets[i]) * 10) / 10;
        sub.textContent = i === 3 ? `${val.toFixed(1)}°C` : `${offsets[i] > 0 ? '+' : ''}${offsets[i]}°C · ${val.toFixed(1)}°C`;
    }

    setText('c-optimal', optimal.toFixed(1));
    setText('c-mytarget', myTarget.toFixed(1));
    setText('hero-feels', fl.toFixed(1));
    setText('hero-target', myTarget.toFixed(1));
    setHTML('r-fl', fl.toFixed(1) + '<span class="mu">°C</span>');
    setHTML('r-gap', (r.gap > 0 ? '+' : '') + r.gap.toFixed(1) + '<span class="mu">°C</span>');
    setHTML('r-rt', r.runtimeMin + '<span class="mu">분</span>');
    if (r.outdoorFeels != null) setText('w-fl', r.outdoorFeels.toFixed(1) + '°C');

    updateComfortUI(r);
    updateStatus(r);
}

// ── 쾌적 척도 5단계 UI ──
function updateComfortUI(r) {
    const step = r.comfortStep;
    for (let i = 1; i <= 5; i++) {
        const bar = document.getElementById('sb' + i);
        const lbl = document.getElementById('sl' + i);
        const active = i === step;
        if (bar) bar.className = 'scale-bar s' + i + (active ? ' active' : '');
        if (lbl) lbl.className = 'scale-lbl s' + i + (active ? ' active' : '');
    }
    setText('c-emoji', r.comfortEmoji);
    setText('c-label', `${step}단계 — ${r.comfortLabel}`);

    const toComfort = Math.max(0, Math.round((r.myTarget - 1 - r.feelsLike) * 10) / 10);
    const sub = step === 3 ? `내 목표온도(${r.myTarget.toFixed(1)}°C) 범위 내에 있어요`
        : step < 3 ? `목표온도까지 ${toComfort}°C 부족해요`
            : `목표온도보다 ${Math.abs(r.feelsLike - r.myTarget).toFixed(1)}°C 높아요`;
    setText('c-sub', sub);

    const badge = document.getElementById('c-badge');
    if (badge) {
        badge.textContent = step === 3 ? '쾌적 유지 중' : step < 3 ? `+${toComfort}°C 필요` : '과열 주의';
        badge.style.background = step === 3 ? 'var(--green-pale)' : step < 3 ? 'var(--navy-pale)' : 'var(--warm-pale)';
        badge.style.color = step === 3 ? 'var(--green)' : step < 3 ? 'var(--navy)' : 'var(--warm)';
    }
}

// ── 보일러 상태/캐릭터/절감액 ──
function updateStatus(r) {
    const rc = document.getElementById('result-card');
    const dot = document.getElementById('r-dot');
    const rst = document.getElementById('r-status');
    const rtime = document.getElementById('r-time');
    const fbled = document.getElementById('fb-led');
    const fbtxt = document.getElementById('fb-txt');
    const hdot = document.getElementById('hero-dot');
    const hstxt = document.getElementById('hero-status-txt');
    const hmsg = document.getElementById('hero-msg');
    const bubble = document.getElementById('char-bubble');
    const hero = document.getElementById('app-hero');

    if (r.boilerState === 'heating') {
        if (rc) rc.className = 'result-card hot';
        if (dot) dot.className = 'result-dot hot';
        if (rst) rst.textContent = `${r.comfortEmoji} ${r.comfortLabel} — 가열 필요`;
        if (rtime) { rtime.className = 'result-time'; rtime.innerHTML = r.runtimeMin + '<span>분</span>'; }
        colorRes('var(--warm)');
        if (fbled) fbled.className = 'fb-led on';
        if (fbtxt) fbtxt.textContent = r.boilerCommand || 'AI 제어 중 — 빠르게 가열';
        if (hdot) hdot.className = 'hero-status-dot red';
        if (hstxt) hstxt.textContent = '보일러 가동 중';
        if (hmsg) hmsg.textContent = `체감 ${r.feelsLike.toFixed(1)}°C\n가상목표 ${r.virtualTarget.toFixed(1)}°C까지 ${r.runtimeMin}분`;
        if (bubble) bubble.textContent = '따뜻하게 데울게요! 🔥';
        if (hero) { hero.classList.add('hero-heating'); hero.classList.remove('hero-eco'); }
        setCharImg('/images/fire.png');
    } else if (r.boilerState === 'approaching') {
        if (rc) rc.className = 'result-card cool';
        if (dot) dot.className = 'result-dot cool';
        if (rst) rst.textContent = '😐 도달 임박 — 잔열 브레이크';
        if (rtime) { rtime.className = 'result-time cool'; rtime.innerHTML = r.runtimeMin + '<span>분</span>'; }
        colorRes('var(--blue-dark)');
        if (fbled) fbled.className = 'fb-led ok';
        if (fbtxt) fbtxt.textContent = r.boilerCommand || '잔열 브레이크 — 설정 온도 낮춤';
        if (hdot) hdot.className = 'hero-status-dot';
        if (hstxt) hstxt.textContent = '목표 도달 임박';
        if (hmsg) hmsg.textContent = `체감 ${r.feelsLike.toFixed(1)}°C\n잔열로 부드럽게 안착 중`;
        if (bubble) bubble.textContent = '거의 다 왔어요! 🌡️';
        if (hero) { hero.classList.remove('hero-heating', 'hero-eco'); }
        setCharImg('/images/noew.png');
    } else {
        if (rc) rc.className = 'result-card cool';
        if (dot) dot.className = 'result-dot cool';
        if (rst) rst.textContent = '😊 목표 달성 — 가동 불필요';
        if (rtime) { rtime.className = 'result-time cool'; rtime.innerHTML = '0<span>분</span>'; }
        colorRes('var(--green)');
        if (fbled) fbled.className = 'fb-led';
        if (fbtxt) fbtxt.textContent = r.boilerCommand || '보일러 OFF — 잔열로 도달';
        if (hdot) hdot.className = 'hero-status-dot';
        if (hstxt) hstxt.textContent = '쾌적 유지 중 — 에너지 절약 중 🌿';
        if (hmsg) hmsg.textContent = `체감 ${r.feelsLike.toFixed(1)}°C\n오버슈트 없이 안착! ✨`;
        if (bubble) bubble.textContent = '에너지 절약 중이에요! 🌿';
        if (hero) { hero.classList.add('hero-eco'); hero.classList.remove('hero-heating'); }
        setCharImg('/images/happy.png');
    }

    // 습도 기반 가습기 상태 (hero)
    const humIcon = document.getElementById('hero-hum-icon');
    const humTxt = document.getElementById('hero-hum-txt');
    const humStatus = document.getElementById('hero-hum-status');
    if (r.alertType === 'humidify') {
        if (humIcon) humIcon.textContent = '💧';
        if (humTxt) humTxt.textContent = '가습기 미가동';
        if (humStatus) humStatus.className = 'hero-hum-status warn';
    } else {
        if (humIcon) humIcon.textContent = '💧';
        if (humTxt) humTxt.textContent = '가습기 가동중';
        if (humStatus) humStatus.className = 'hero-hum-status on';
    }

    // 알림 카드 + 습도 배너
    checkNotifs(r);
    showAlert(r.alertType, r.alertMsg);

    setText('s-rate', (r.boilerState === 'heating' ? r.saveRate + '%' : (r.boilerState === 'comfort' ? '35%+' : '40%+')));
    setText('s-day', (r.saveDaily + 620).toLocaleString() + '원');
    setText('s-mon', ((r.saveMonthly || 0) + MOCK_SAVE_BASE).toLocaleString() + '원');
    updateGoalCard(r.saveMonthly || 0);
}

// ── 역지오코딩: 좌표 → 한국 행정구역명 ──
async function reverseGeocode(lat, lon) {
    try {
        const res = await fetch(
            `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}&accept-language=ko`,
            { headers: { 'Accept-Language': 'ko' } }
        );
        const data = await res.json();
        const addr = data.address || {};
        const stateMap = {
            '경상북도': '경북', '경상남도': '경남', '전라북도': '전북',
            '전북특별자치도': '전북', '전라남도': '전남', '충청북도': '충북',
            '충청남도': '충남', '강원도': '강원', '강원특별자치도': '강원',
            '경기도': '경기', '제주특별자치도': '제주', '서울특별시': '서울',
            '부산광역시': '부산', '대구광역시': '대구', '인천광역시': '인천',
            '광주광역시': '광주', '대전광역시': '대전', '울산광역시': '울산',
            '세종특별자치시': '세종',
        };
        const state = stateMap[addr.province || addr.state] || addr.province || addr.state || '';
        const city = addr.county || addr.city || addr.town || addr.village || '';
        return [state, city].filter(Boolean).join(' ') || null;
    } catch {
        return null;
    }
}

// ── 날씨 가져오기 (Open-Meteo, GPS) ──
async function fetchWeather() {
    const btn = document.getElementById('fetchBtn');
    const msg = document.getElementById('st-msg');
    btn.disabled = true; msg.textContent = '📍 위치 확인 중...';
    if (!navigator.geolocation) { msg.textContent = 'GPS 미지원'; btn.disabled = false; return; }
    navigator.geolocation.getCurrentPosition(async pos => {
        const { latitude: lat, longitude: lon } = pos.coords;
        msg.textContent = '🌐 날씨 불러오는 중...';
        try {
            const [weatherRes, locationName] = await Promise.all([
                fetch(`https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code&wind_speed_unit=ms`).then(r => r.json()),
                reverseGeocode(lat, lon)
            ]);
            const cur = weatherRes.current;
            outT = cur.temperature_2m; outH = cur.relative_humidity_2m; wind = cur.wind_speed_10m;
            const wc = cur.weather_code;
            let icon = '🌤️', desc = '맑음';
            if (wc === 0) { icon = '☀️'; desc = '맑음'; } else if (wc <= 3) { icon = '⛅'; desc = '구름'; }
            else if (wc <= 45) { icon = '🌫️'; desc = '안개'; } else if (wc <= 67) { icon = '🌧️'; desc = '비'; }
            else if (wc <= 77) { icon = '❄️'; desc = '눈'; } else { icon = '⛈️'; desc = '뇌우'; }
            setText('w-icon', icon);
            setHTML('w-temp', outT.toFixed(1) + '<sup>°</sup>');
            setText('w-desc', desc);
            setText('w-hum', outH + '%');
            setText('w-wind', wind.toFixed(1) + ' m/s');
            setText('loc-pill', locationName ? `📍 ${locationName}` : `📍 ${lat.toFixed(2)}°N, ${lon.toFixed(2)}°E`);
            msg.textContent = '✓ 날씨 연결 완료 (Open-Meteo API)';
            btn.textContent = '🔄 새로고침'; btn.disabled = false; calc();
        } catch (e) { msg.textContent = '네트워크 오류'; btn.disabled = false; }
    }, () => {
        outT = -2; outH = 60; wind = 2;
        setHTML('w-temp', '-2<sup>°</sup>');
        setText('w-desc', '기본값'); setText('w-hum', '60%'); setText('w-wind', '2.0 m/s');
        setText('loc-pill', '📍 기본값 (GPS 거부됨)');
        msg.textContent = 'GPS 거부 — 기본값 적용 (외기 -2°C)';
        btn.disabled = false; calc();
    });
}

// ── 캐릭터 이미지 교체 ──
function setCharImg(src) {
    const wrap = document.getElementById('char-svg-wrap');
    if (wrap) wrap.innerHTML = `<img src="${src}" style="width:110px;" alt="character">`;
}

// ── 캐릭터 표정 SVG ──
const faces = {
    happy: `<path d="M30 33 Q34 29 38 33" stroke="#00E5FF" stroke-width="2" fill="none" stroke-linecap="round"/><path d="M42 33 Q46 29 50 33" stroke="#00E5FF" stroke-width="2" fill="none" stroke-linecap="round"/><path d="M32 40 Q40 46 48 40" stroke="#00E5FF" stroke-width="1.8" fill="none" stroke-linecap="round"/>`,
    cold: `<circle cx="32" cy="33" r="4" fill="#00E5FF" opacity="0.7"/><circle cx="48" cy="33" r="4" fill="#00E5FF" opacity="0.7"/><path d="M35 42 Q40 38 45 42" stroke="#00E5FF" stroke-width="1.8" fill="none" stroke-linecap="round"/>`,
    working: `<path d="M30 33 Q34 28 38 33" stroke="#00E5FF" stroke-width="2" fill="none" stroke-linecap="round"/><path d="M42 33 Q46 28 50 33" stroke="#00E5FF" stroke-width="2" fill="none" stroke-linecap="round"/><path d="M34 41 Q40 45 46 41" stroke="#00E5FF" stroke-width="1.8" fill="none" stroke-linecap="round"/>`,
    perfect: `<path d="M28 33 Q32 28 36 33" stroke="#00E5FF" stroke-width="2" fill="none" stroke-linecap="round"/><path d="M44 33 Q48 28 52 33" stroke="#00E5FF" stroke-width="2" fill="none" stroke-linecap="round"/><path d="M30 40 Q40 48 50 40" stroke="#00E5FF" stroke-width="2" fill="none" stroke-linecap="round"/>`,
};
function setChar(state) {
    const arm_default = `<ellipse cx="15" cy="58" rx="6" ry="4" fill="white" stroke="#DDE8F0" stroke-width="1" transform="rotate(-30,15,58)"/><ellipse cx="65" cy="60" rx="6" ry="4" fill="white" stroke="#DDE8F0" stroke-width="1" transform="rotate(15,65,60)"/>`;
    const arm_press = `<ellipse cx="12" cy="55" rx="6" ry="4" fill="white" stroke="#DDE8F0" stroke-width="1" transform="rotate(-50,12,55)"/><ellipse cx="65" cy="60" rx="6" ry="4" fill="white" stroke="#DDE8F0" stroke-width="1" transform="rotate(15,65,60)"/><circle cx="8" cy="53" r="3" fill="#FF6B35" opacity="0.7"/>`;
    const arm_up = `<ellipse cx="14" cy="52" rx="6" ry="4" fill="white" stroke="#DDE8F0" stroke-width="1" transform="rotate(-60,14,52)"/><ellipse cx="66" cy="52" rx="6" ry="4" fill="white" stroke="#DDE8F0" stroke-width="1" transform="rotate(60,66,52)"/>`;
    const arms = state === 'working' ? arm_press : state === 'perfect' ? arm_up : arm_default;
    const wrap = document.getElementById('char-svg-wrap');
    if (!wrap) return;
    wrap.innerHTML = `<svg width="110" height="120" viewBox="0 0 80 88"><rect x="24" y="54" width="32" height="24" rx="9" fill="white" stroke="#DDE8F0" stroke-width="1.2"/><circle cx="40" cy="68" r="7" fill="#E8F5FD" stroke="#4DADE8" stroke-width="1"/><rect x="30" y="76" width="7" height="7" rx="3.5" fill="#DDE8F0"/><rect x="43" y="76" width="7" height="7" rx="3.5" fill="#DDE8F0"/>${arms}<circle cx="40" cy="34" r="23" fill="white" stroke="#DDE8F0" stroke-width="1.2"/><line x1="40" y1="11" x2="40" y2="3" stroke="#4DADE8" stroke-width="2" stroke-linecap="round"/><circle cx="40" cy="2.5" r="3.5" fill="#4DADE8"/><rect x="20" y="20" width="40" height="29" rx="9" fill="#111820"/>${faces[state] || faces.happy}</svg>`;
}

// ── AI 알림 시스템 ──
const _activeNotifs = new Set();

// inline=true → AI 결과 카드 위, false → 히어로 아래
function addNotif(id, type, icon, title, msg, actionLabel, actionFn, inline) {
    if (_activeNotifs.has(id)) return;
    _activeNotifs.add(id);
    const containerId = inline ? 'notif-inline' : 'notif-container';
    const container = document.getElementById(containerId);
    if (!container) return;
    const card = document.createElement('div');
    card.className = `notif-card notif-${type}`;
    card.id = 'notif-' + id;
    card.innerHTML = `
        <div class="notif-icon">${icon}</div>
        <div class="notif-body">
            <div class="notif-title">${title}</div>
            <div class="notif-msg">${msg}</div>
            ${actionLabel ? `<button class="notif-action" onclick="(${actionFn})()">${actionLabel}</button>` : ''}
        </div>
        <button class="notif-close" onclick="removeNotif('${id}')">×</button>`;
    container.appendChild(card);
}

function removeNotif(id) {
    if (!_activeNotifs.has(id)) return;
    _activeNotifs.delete(id);
    const el = document.getElementById('notif-' + id);
    if (!el) return;
    el.style.animation = 'notif-out 0.25s ease forwards';
    setTimeout(() => el.remove(), 250);
}

function checkNotifs(r) {
    const hour = new Date().getHours();

    // 습도 기반 (AI 결과 카드 위 inline)
    if (r.alertType === 'humidify') {
        addNotif('humid-low', 'teal', '💧', '가습기를 켜보세요',
            '실내 습도가 낮아요. 가습하면 체감온도가 올라 가스비를 절약할 수 있어요.', null, null, true);
    } else removeNotif('humid-low');

    if (r.alertType === 'ventilate') {
        addNotif('humid-high', 'blue', '🌬️', '환기를 권장해요',
            '습도가 높아요. 잠깐 환기하면 쾌적해지고 곰팡이도 예방돼요.', null, null, true);
    } else removeNotif('humid-high');

    // 잔열 활용
    if (r.boilerState === 'approaching') {
        addNotif('residual', 'green', '⚡', '잔열로 목표 온도에 도달해요',
            '지금 보일러를 꺼도 잔열로 목표에 도달할 수 있어요. 에너지를 아껴요!');
    } else removeNotif('residual');

    // 외기 날씨 기반
    if (outT !== null) {
        if (outT > 10) {
            addNotif('warm-day', 'green', '🌿', '오늘 날씨가 따뜻해요!',
                `외기 ${outT.toFixed(1)}°C — 에코 모드로 절약하며 난방하는 걸 추천해요.`,
                '설정에서 켜기', () => switchTab('settings'));
        } else removeNotif('warm-day');

        if (outT < -5) {
            addNotif('cold-snap', 'orange', '🥶', '한파예요! 사전 예열을 권장해요',
                `외기 ${outT.toFixed(1)}°C — 귀가 전 미리 예열을 시작하면 좋아요.`);
        } else removeNotif('cold-snap');

        if (wind > 6) {
            addNotif('strong-wind', 'blue', '💨', '강풍으로 체감온도가 낮아졌어요',
                `풍속 ${wind.toFixed(1)}m/s — 외풍 차단과 난방 강도를 높이는 걸 추천해요.`);
        } else removeNotif('strong-wind');
    }

    // 시간대 기반
    if (hour >= 22 || hour < 6) {
        addNotif('sleep-mode', 'blue', '🌙', '취침 시간이에요',
            '수면 중 체감 18~19°C가 숙면에 좋아요. 목표 온도를 낮춰보세요.');
    } else removeNotif('sleep-mode');

}

// ── 헬퍼 ──
function showAlert(type, msg) {
    let el = document.getElementById('humid-alert');
    if (!el) {
        // 알림 배너가 없으면 결과 카드 위에 동적 생성
        const anchor = document.getElementById('result-card');
        if (!anchor || !anchor.parentNode) return;
        el = document.createElement('div');
        el.id = 'humid-alert';
        el.style.cssText = 'margin:12px 0;padding:14px 16px;border-radius:14px;font-size:14px;font-weight:600;display:none;align-items:center;gap:8px;';
        anchor.parentNode.insertBefore(el, anchor);
    }
    if (!type || !msg) { el.style.display = 'none'; return; }
    el.style.display = 'flex';
    if (type === 'humidify') {
        el.style.background = 'var(--gold-pale)';
        el.style.color = 'var(--gold)';
        el.textContent = '💧 ' + msg;
    } else {
        el.style.background = 'var(--blue-light)';
        el.style.color = 'var(--blue-dark)';
        el.textContent = '🌬️ ' + msg;
    }
}

function setText(id, v) { const el = document.getElementById(id); if (el) el.textContent = v; }
function setHTML(id, v) { const el = document.getElementById(id); if (el) el.innerHTML = v; }
function colorRes(c) {
    const a = document.getElementById('r-fl'), b = document.getElementById('r-gap');
    if (a) a.style.color = c; if (b) b.style.color = c;
}

// ── 탭 전환 ──
function switchTab(tab) {
    document.getElementById('sc-app').classList.toggle('active', tab === 'home');
    document.getElementById('sc-stats').classList.toggle('active', tab === 'stats');
    document.getElementById('sc-settings').classList.toggle('active', tab === 'settings');
    ['home','stats','settings'].forEach(t => {
        [1,2,3].forEach(n => {
            const suffix = n === 1 ? '' : String(n);
            const el = document.getElementById('nav-' + t + suffix);
            if (el) el.classList.toggle('active', t === tab);
        });
    });
}

// ── 채팅 ──
let _chatSending = false;

function toggleChat() {
    const panel = document.getElementById('chatPanel');
    panel.classList.toggle('open');
    if (panel.classList.contains('open')) {
        setTimeout(() => document.getElementById('chatInput').focus(), 350);
    }
}

async function sendChat() {
    if (_chatSending) return;
    const input = document.getElementById('chatInput');
    const msg = input.value.trim();
    if (!msg) return;
    _chatSending = true;

    appendChatMsg('user', msg);
    input.value = '';

    const sendBtn = document.getElementById('chatSendBtn');
    sendBtn.disabled = true;
    const typingEl = appendChatMsg('bot', '입력 중...', true);

    try {
        const body = {
            message: msg,
            feelsLike: lastResult ? lastResult.feelsLike : null,
            targetTemp: lastResult ? lastResult.myTarget : null,
            boilerState: lastResult ? lastResult.boilerState : null,
            outdoorTemp: outT,
            indoorHum: parseFloat(document.getElementById('iHum').value) || null,
            runtimeMin: lastResult ? lastResult.runtimeMin : null,
            prefStep: userPrefStep,
            saveMonthly: lastResult ? lastResult.saveMonthly : null
        };

        const res = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        const data = await res.json();
        typingEl.remove();

        if (data.suggestedStep && data.suggestedStep !== userPrefStep) {
            appendChatMsg('bot', data.reply, false, data.suggestedStep);
        } else {
            appendChatMsg('bot', data.reply);
        }
    } catch (e) {
        typingEl.remove();
        appendChatMsg('bot', '연결 오류가 발생했어요. 다시 시도해주세요.');
    } finally {
        sendBtn.disabled = false;
        _chatSending = false;
        document.getElementById('chatInput').focus();
    }
}


function appendChatMsg(role, text, isTyping = false, suggestedStep = null) {
    const container = document.getElementById('chatMessages');
    const div = document.createElement('div');
    div.className = 'chat-msg ' + role;

    let inner = `<div class="chat-bubble${isTyping ? ' chat-typing' : ''}">${text.replace(/\n/g, '<br>')}`;
    if (suggestedStep) {
        inner += `<br><span class="chat-step-badge" onclick="applyStep(${suggestedStep})">${suggestedStep}단계로 바꾸기 →</span>`;
    }
    inner += `</div>`;
    div.innerHTML = inner;

    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
    return div;
}

function applyStep(step) {
    setStep(step);
    appendChatMsg('bot', `${step}단계로 변경했어요! ✅`);
}

// ── 초기화 ──
window.addEventListener('DOMContentLoaded', () => {
    const q = new URLSearchParams(window.location.search);
    if (q.get('area')) window.__AREA__ = parseInt(q.get('area'));
    if (q.get('savingsGoal')) savingsGoal = parseInt(q.get('savingsGoal'));
    sInsul();
    setCharImg('/images/turn.png');
    calc();
});
