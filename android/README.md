# 데일리 브리핑 (Android)

매일 09:30 생성되는 경제·코인·금·뉴스 브리핑을 **스마트폰 앱**으로 읽는 클라이언트입니다.
백엔드는 이미 운영 중인 Supabase(`daily_briefings` / `briefing_images`)를 그대로 쓰고, 앱은 읽기 전용으로 붙습니다.

- **버전**: `0.5.0` (versionCode `1`)
- **패키지**: `com.clghks86.dailybriefing` (debug는 `.debug` suffix)
- **minSdk 26 / targetSdk 35 / compileSdk 35**
- **광고 없음**: AdMob은 **1.0.0부터** 추가하기로 했고, 이 버전에는 관련 의존성·코드·메타데이터가 **하나도** 들어 있지 않습니다.

---

## 1. 한눈에 보는 기능

| 탭 | 내용 |
|---|---|
| **홈** | 날짜 칩(DB에 실제 있는 날짜만) · 당겨서 새로고침 · **관심 지표 핀 고정 티커** · 오늘의 요약 카드(커버 이미지 + 헤드라인 + 요약 + 핵심 이벤트 칩) · 핵심 지표 2열 그리드(상승 빨강/하락 파랑) · 공포·탐욕 게이지 · 시장 메모 · 북마크(저장한 날짜) |
| **추이** | Compose Canvas 인터랙티브 라인 차트 · 지수/금/코인/환율·금리 탭 · **첫날=100 환산 ↔ 실제 값** 토글 · 좌우 드래그 툴팁(그날 값) |
| **뉴스** | `details.news` 카드 목록(원문은 CustomTabs) · 섹션 이미지(sort_order=3) · 시장 메모 · 전문 보기 |
| **전문** | `raw_markdown` 뷰어(제목·목록·인용·구분선, **굵게**/`코드` 인라인) · 코인/금 이미지(sort_order=2) |
| **설정** | 브리핑 알림 on/off · 테스트 알림 · 목표가 알림 CRUD · 위젯 새로고침 · 관심 지표 핀 · 테마(시스템/다크/라이트) · 정보 |

앱 전용 기능(서버 없이 단독 동작):

- **매일 브리핑 알림** — WorkManager가 09:35 기준으로 하루 1회, 새 `briefing_date`를 감지하면 오늘의 헤드라인을 로컬 알림으로 띄웁니다. 같은 날짜로 두 번 알리지 않습니다(`last_notified_date`). 09:35가 지난 뒤 앱을 열면 한 번 "따라잡기" 체크를 합니다.
- **목표가 알림** — 코스피 / KRX 금(원/g) / BTC(USD·KRW) / 원/달러 목표가를 (이상·이하)로 등록하면 15분 주기(WorkManager 최소 주기)로 확인해 닿으면 알립니다. 목표 하나당 하루 1회.
- **홈 화면 위젯(Glance)** — 코스피 · BTC · KRX 금 + 헤드라인 2줄 + 새로고침 버튼. 값은 캐시(SharedPreferences)에서 그리고, 새로고침은 네트워크를 다시 당깁니다.

---

## 2. 기술 스택

| 구분 | 사용 |
|---|---|
| 언어/UI | Kotlin 2.0.21, Jetpack Compose (Material 3), Compose BOM 2024.12.01 |
| 아키텍처 | MVVM + Repository (data / domain / ui / work / widget), 수동 DI(`ServiceLocator`) |
| 네트워크 | Retrofit 2.11 + OkHttp + **Kotlinx Serialization** (PostgREST `select=*`) |
| 이미지 | Coil 2.7 |
| 로컬 저장 | DataStore(Preferences) — 핀·북마크·목표가·테마·알림 상태 |
| 백그라운드 | WorkManager 2.10 (CoroutineWorker) |
| 위젯 | Jetpack Glance 1.1 (appwidget, material3) |
| 링크 | androidx.browser CustomTabs |
| 테스트 | JUnit4 + kotlinx-coroutines-test (48개) |

- **다크 테마가 기본**, 시스템/라이트 지원. 금색 포인트 + 한국 관례 등락 색(상승 빨강 `#F2545B` / 하락 파랑 `#4C8DFF`).

---

## 3. 프로젝트 구조

```
work/daily-briefing-android/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/libs.versions.toml            # 버전 카탈로그
├── gradle/wrapper/ · gradlew            # Gradle 8.10.2 wrapper
├── local.properties                     # sdk.dir (git 제외)
└── app/
    ├── build.gradle.kts                 # 버전 0.5.0, BuildConfig(Supabase), sb_secret_ 차단
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── res/ (테마·아이콘·위젯 배경·문자열)
        │   └── java/com/clghks86/dailybriefing/
        │       ├── DailyBriefingApp.kt   # ServiceLocator 초기화, 알림 채널, WorkManager 예약
        │       ├── MainActivity.kt       # edge-to-edge, 따라잡기 체크
        │       ├── core/                 # ServiceLocator, SupabaseConfig
        │       ├── data/
        │       │   ├── remote/           # Retrofit API, OkHttp(apikey 인터셉터), DTO
        │       │   ├── local/            # DataStore 설정, 위젯 스냅숏
        │       │   └── repository/       # TTL 메모리 캐시 + 실패 시 stale 캐시
        │       ├── domain/
        │       │   ├── model/            # IndicatorId(21개 지표 메타), Briefing, PriceGoal, 포매터
        │       │   ├── mapper/           # DTO → 도메인 (details JSONB 파싱)
        │       │   ├── chart/            # 날짜 정렬·첫값 100 환산 시리즈 빌더
        │       │   ├── goal/             # 목표가 판정(순수 함수)
        │       │   ├── markdown/         # raw_markdown 최소 파서
        │       │   └── repository/       # 인터페이스
        │       ├── ui/
        │       │   ├── AppRoot.kt        # 하단 4탭 + article/{date} 라우트
        │       │   ├── theme/            # 색·타이포·다크/라이트·MarketColors
        │       │   ├── components/       # 공통 칩·상태·MarkdownText
        │       │   ├── home/ trend/ news/ settings/  # 화면 + ViewModel
        │       │   └── util/             # DateLabels, LinkOpener
        │       ├── work/                 # 알림 헬퍼, DailyBriefingWorker, PriceGoalWorker, WorkScheduler
        │       └── widget/               # Glance 위젯 + 새로고침 ActionCallback + Receiver
        └── test/
            ├── resources/                # 실제 Supabase 응답 스냅숏(briefings.json, images.json)
            └── java/...                  # 매퍼·포매터·차트·목표가·마크다운·날짜·스케줄 테스트
```

규모: **Kotlin 52개 파일 / 약 5,450줄**, 테스트 8개 파일 / 약 680줄.

---

## 4. 데이터 연동

`work/daily-briefing/index.html` 상단에서 읽은 값을 그대로 `gradle.properties` → `BuildConfig`로 주입합니다.

```properties
dailyBriefing.supabaseUrl=https://lxgdotpfpjbpxdknpwwl.supabase.co
dailyBriefing.supabasePublishableKey=sb_publishable_…   # 공개 키
```

- `BuildConfig.SUPABASE_URL` / `SUPABASE_PUBLISHABLE_KEY` → `core/SupabaseConfig.kt` → OkHttp 인터셉터가 `apikey`·`Authorization` 헤더로 붙임.
- **`sb_secret_`(service_role) 키는 넣을 수 없습니다**: `app/build.gradle.kts`가 키가 `sb_secret_`로 시작하면 **빌드 자체를 실패**시킵니다. 앱은 `SELECT`만 합니다.
- 읽는 컬럼: `daily_briefings` 전체 + `briefing_images`(id, briefing_date, section, image_url, caption, sort_order).
- `details` JSONB에서 꺼내는 키: `as_of_kst`, `key_events[]`, `news[]`(title·url·source·summary·category), `kr_market_note`, `dxy`, `vix`, `wti_usd`, `gold_etf{}`. 모르는 키가 늘어나도 `ignoreUnknownKeys`로 무시합니다.
- 이미지 규칙(대시보드와 동일): `sort_order=1` 커버, `2` 코인/금, `3` 섹션.
- 5분 TTL 메모리 캐시를 두고, 네트워크가 끊기면 마지막 캐시를 그대로 보여 줍니다.

---

## 5. 빌드 · 실행

```bash
# 요구 사항: JDK 17+ (이 저장소는 JDK 21에서 검증), Android SDK Platform 35
cd work/daily-briefing-android
echo "sdk.dir=/path/to/Android/sdk" > local.properties     # 또는 ANDROID_HOME

./gradlew testDebugUnitTest        # 단위 테스트 48개
./gradlew assembleDebug           # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease         # app/build/outputs/apk/release/app-release-unsigned.apk (서명 전)
```

Android Studio: **File → Open → `work/daily-briefing-android`** → Gradle sync → 실행.

> 이 저장소는 `local.properties`에 이 작업 환경의 SDK 경로가 들어 있습니다. PC에서 열 때 자기 경로로 바꾸거나 지우고 Android Studio가 만들게 두세요. (`local.properties`는 git에서 제외)

### 검증 결과 (이 환경)

| 항목 | 결과 |
|---|---|
| 환경 | Linux 2코어, JDK 21.0.13 (Temurin), Android SDK 35 + build-tools 35.0.0 + platform-tools (명령줄 도구로 설치), Gradle 8.10.2 |
| `testDebugUnitTest` | **성공 — 48개 테스트, 실패 0** |
| `assembleDebug` | **성공** — `app/build/outputs/apk/debug/app-debug.apk` (21.2MB, 바로 설치 가능) |
| `assembleRelease` | **성공** — `app/build/outputs/apk/release/app-release-unsigned.apk` (13.8MB, 서명 전), lintVital 통과 |
| `lintDebug` | **성공 — 0 errors, 46 warnings** (대부분 "새 버전이 나왔다"는 의존성 알림과 API 31+ 위젯 속성 안내) |
| APK 메타데이터 | release: `package com.clghks86.dailybriefing`, `versionCode=1`, `versionName=0.5.0`, minSdk 26, targetSdk 35 |
| 실기기 실행 | **미검증** — 이 환경에는 에뮬레이터/기기가 없어 화면 렌더링·알림·위젯은 실제 기기 확인이 필요합니다 |

---

## 6. 테스트가 지키는 것

| 테스트 | 검증 내용 |
|---|---|
| `BriefingMapperTest` | **실제 Supabase 응답 스냅숏**을 그대로 넣어 컬럼 매핑·정렬·이미지 연결·`details` 파싱·빈 값 처리 |
| `IndicatorFormatterTest` | 천 단위·소수 자릿수·접두/접미(₩, $, %, 원)·등락 부호·만원/억원 축약 |
| `ChartSeriesTest` | 날짜 오름차순 정렬, 값 정렬(빈 날짜 null), 첫값 100 환산, 실제 값 모드, 값 없는 시리즈 제외, min/max |
| `PriceGoalEvaluatorTest` | 이상/이하 판정, 경계값, 사용 중지, 같은 날짜 중복 알림 방지, 값 없음, 발송 표시 |
| `MarkdownParserTest` | 제목 레벨, 문단 병합, 목록/번호, 인용, 구분선, **굵게**/`코드` 런, 닫히지 않은 표기 |
| `DateLabelsTest` | 날짜 칩·오늘/어제·전체 라벨·축 라벨·잘못된 날짜 폴백 |
| `ScheduleTimesTest` | 09:35 이전/이후 다음 실행 지연, 따라잡기 필요 여부 |
| `WidgetSnapshotTest` | 위젯에 넘길 4개 값·시각 라벨·헤드라인 |

---

## 7. 알림·위젯 동작 방식

- 채널: `briefing_v1`(기본 중요도), `goal_v1`(높음). 알림 탭하면 앱이 열립니다.
- 예약: `WorkScheduler.ensureScheduled()`가 앱 시작 시 `UPDATE` 정책으로 중복 없이 다시 잡습니다.
  - 브리핑: `PeriodicWork(1일, 초기 지연=다음 09:35)` + 앱 실행 시 1회성 따라잡기
  - 목표가: `PeriodicWork(15분)` — WorkManager 최소 주기라서 실제로는 15분~수십 분 지연될 수 있습니다(Doze/배터리 최적화 영향).
- 알림 권한(Android 13+): 설정에서 알림을 켤 때 `POST_NOTIFICATIONS`를 요청합니다. 권한이 없으면 알림을 "보냈다"고 기록하지 않아, 나중에 권한을 주면 그날 브리핑을 다시 알려 줍니다.
- 위젯: 홈 화면 길게 누르기 → 위젯 → 데일리 브리핑. `updatePeriodMillis` 30분 + 새로고침 버튼.

---

## 8. Play Store 출시 전 체크리스트

이 버전(0.5.0)은 **스토어 업로드 전 상태**입니다. 1.0.0으로 올리기 전에:

1. **서명 키 만들기** — `keytool -genkey -v -keystore daily-briefing-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias daily-briefing`
   → `keystore.properties`(git 제외) + `signingConfigs.release` 연결 → Play 앱 서명 등록.
2. **버전 올리기** — `versionCode`는 업로드마다 +1, `versionName`은 `1.0.0`.
3. **AdMob (1.0.0부터)** — `play-services-ads` 의존성, 배너 슬롯, 테스트 광고 단위 ID → 실제 ID로 교체. 광고 추가 시 **데이터 안전(Data safety)** 에 광고 ID·수집 항목을 다시 신고해야 합니다.
4. **개인정보처리방침 URL** — 앱은 서버를 두지 않고 Supabase를 읽기만 하지만, 알림·위젯·(향후 광고) 때문에 정책 페이지가 필요합니다.
5. **스토어 등록정보** — 앱 이름, 짧은 설명, 스크린샷(폰 필수), 아이콘 512×512, 그래픽 이미지 1024×500.
6. **R8/난독화** — 지금은 `isMinifyEnabled = false`(안전 우선). 켤 때 `proguard-rules.pro`의 Retrofit/kotlinx.serialization 규칙을 그대로 두고 실기기 스모크 테스트.
7. **콘텐츠 등급 설문** — 금융 정보 앱이지만 투자 권유가 아님을 설명에 명시.
8. **targetSdk 정책** — Play는 매년 targetSdk 하한을 올립니다. 2027년 정책 발표 시 `compileSdk`/`targetSdk` 상향 + AGP 업데이트 필요.

---

## 9. 알려진 제약 / 다음 후보

- 실기기에서 **푸시 렌더링·알림·위젯 동작은 아직 확인하지 못했습니다** (에뮬레이터 없음). Android Studio에서 한 번 실행해 확인 필요.
- `details`에 새 키가 생기면 도메인 모델에 추가해야 화면에 나옵니다(모르는 키는 조용히 무시).
- 브리핑이 5일치뿐이라 차트는 짧습니다. 날짜가 쌓이면 자동으로 길어집니다(90일까지 조회).
- 목표가 알림은 15분 주기라 급등락을 즉시 잡지는 못합니다. 실시간이 필요하면 Supabase Realtime 구독이 다음 후보.
- 다크 테마 기본이지만 라이트 모드 색 대비는 실기기에서 한 번 점검 권장.
- 문자열이 코드에 한국어로 들어 있습니다. 다국어 지원 시 `strings.xml`로 이동.

---

## 10. 다음 단계

```bash
cd work/daily-briefing-android
git init && git add . && git commit -m "feat: 데일리 브리핑 안드로이드 0.5.0"
gh repo create clghks86/daily-briefing-android --public --source=. --push
```

Android Studio에서 열어 실기기(또는 에뮬레이터)로 실행 → 알림·위젯 확인 → 1.0.0에서 AdMob·서명·스토어 등록.
