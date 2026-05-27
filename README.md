# 온리 : ONLY — AI 스마트 난방 매니저

> AI가 판단하고, 핑거봇이 실행해요!

체감온도 기반으로 보일러를 자동 제어하는 Spring Boot 웹 앱입니다. 핑거봇(물리 버튼 로봇)과 연동해 구형 보일러도 교체 없이 AI가 제어하며, 가스비를 15~20% 절감합니다.

---

## 주요 기능

| 기능 | 설명 |
|------|------|
| 체감온도 계산 | 실내 온도·습도·외기·평수를 결합한 6변수 다항식으로 체감온도 산출 |
| 오버슈트 방지 | 보일러 잔열을 고려해 목표보다 1°C 낮은 가상 목표를 추적, 부드럽게 도달 |
| 3구간 보일러 제어 | 가열 중 / 목표 도달 임박 / 목표 도달(OFF) 3단계 자동 전환 |
| 습도 알림 | 습도 낮을 때 가습 권고, 높을 때 환기 권고 |
| 가스비 절감 예측 | 일일·월별 예상 절감액 계산 |

---

## 기술 스택

- **Java 21** / **Spring Boot 3.4.5**
- **Thymeleaf** (서버 사이드 렌더링)
- **Vanilla JS** (프론트엔드)
- **Gradle** (빌드)
- **Heroku** (배포, Procfile 포함)

---

## 화면 구성

```
/            → 온보딩 (3단계 초기 설정)
/main        → 메인 대시보드 (실시간 체감온도·보일러 제어)
/check       → 체크 화면
```

### 온보딩 단계

1. **주거 환경** — 평수(5~50평), 건물 연식 입력
2. **난방 목표** — 지난달 가스비, 가스 요금제(도시가스/LPG) 입력
3. **절약 목표** — 월 절약 목표 금액 설정

---

## 난방 알고리즘

### 체감온도 (FeelsLike)

```
C = B0 + B1*T + B2*H + B3*(T×H) + B4*E + B5*A
```

| 변수 | 의미 |
|------|------|
| T | 실내 온도(°C) |
| H | 실내 습도(%) |
| E | 외기 온도(°C) |
| A | 면적(㎡) |

가습기를 켜서 H가 오르면 B3 시너지 항으로 체감온도가 빠르게 상승합니다.

### 쾌적 단계 (1~5)

| 단계 | 목표 오프셋 | 설명 |
|------|------------|------|
| 1 | -2°C | 절약형 |
| 2 | -1°C | 약간 절약 |
| 3 | 0°C (22°C) | 표준 |
| 4 | +1°C | 약간 따뜻 |
| 5 | +2°C | 따뜻형 |

### 보일러 제어 로직

```
ΔC = 가상목표(C_target - 1°C) - 현재 체감온도

ΔC ≥ 1.0  → heating    : 보일러 ON (C_target + 1°C 설정)
ΔC ≥ 0.0  → approaching: 잔열 브레이크 (C_target - 1°C 설정)
ΔC < 0.0  → reached    : 보일러 OFF
```

---

## 시작하기

### 사전 요구사항

- Java 21

### 로컬 실행

```bash
./gradlew bootRun
```

브라우저에서 `http://localhost:8080` 접속

### 빌드 & 실행

```bash
./gradlew build
java -jar build/libs/fingerbot-0.0.1-SNAPSHOT.jar
```

---

## API

### `POST /api/calc` — 난방 계산

**Request**

```json
{
  "indoorTemp": 18.5,
  "indoorHum": 45,
  "outdoorTemp": -3.0,
  "outdoorHum": 60,
  "area": 30,
  "insul": "2",
  "prefStep": 3
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| indoorTemp | double | 실내 온도(°C) |
| indoorHum | double | 실내 습도(%) |
| outdoorTemp | Double | 외기 온도(°C), null 허용 |
| area | int | 평수 |
| insul | String | 단열등급: "1"(불량) / "2"(보통) / "3"(양호) |
| prefStep | int | 쾌적 단계 1~5 |

**Response**

```json
{
  "feelsLike": 19.2,
  "myTarget": 22.0,
  "virtualTarget": 21.0,
  "boilerState": "heating",
  "boilerSetTemp": 23.0,
  "boilerCommand": "보일러 설정 23.0도 — 빠르게 가열",
  "runtimeMin": 18,
  "saveRate": 22,
  "saveDaily": 150,
  "saveMonthly": 4500,
  "alertType": "humidify",
  "alertMsg": "가습기를 켜면 더 빨리 따뜻해져요!"
}
```

## 프로젝트 구조

```
only/
├── src/main/java/yu/aihackerton/fingerbot/
│   ├── FingerbotApplication.java
│   ├── controller/
│   │   └── MainController.java
│   ├── service/
│   │   └── HeatingService.java      # 핵심 난방 알고리즘
│   └── dto/
│       ├── CalcRequestDto.java
│       └── HeatingResultDto.java
├── src/main/resources/
│   ├── templates/                   # Thymeleaf HTML
│   │   ├── onboarding.html
│   │   ├── index.html
│   │   └── check.html
│   ├── static/
│   │   ├── css/
│   │   ├── js/
│   │   └── images/
│   └── application.properties
├── build.gradle
└── Procfile                         # Heroku 배포
```

---

## 배포 (Railway)

1. [Railway](https://railway.app)에서 새 프로젝트 생성 후 GitHub 저장소 연결
2. 환경변수 설정:
   ```
   FACTCHAT_API_KEY=YOUR_API_KEY
   PORT=8080
   ```
3. Railway가 `Procfile`을 감지해 자동 빌드·배포
