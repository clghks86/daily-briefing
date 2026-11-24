# 데일리 브리핑 — Google Play 등록 자료

이 문서는 `com.clghks86.dailybriefing` (v0.5.0) 앱을 Google Play Console에 등록할 때 그대로 복사해 쓸 수 있는 문구와, 데이터 보안 설문·서명·업로드 절차를 정리한 것입니다.
에셋은 같은 폴더(`android/store-assets/`)에 있습니다.

- 작성 기준: 2026-11-24, 앱 버전 0.5.0 (versionCode 1), 광고 없음·로그인 없음
- 개인정보처리방침: <https://clghks86.github.io/daily-briefing/privacy.html>
- 문의 메일: `clghks@gmail.com`

---

## 1. 에셋 파일

| 파일 | 용도 | 규격 | 실제 확인값 |
|---|---|---|---|
| `icon-512x512.png` | 앱 아이콘 | 512×512, 32-bit PNG, 1MB 이하 | 512×512, RGB(투명도 없음), 229KB |
| `feature-graphic-1024x500.png` | 피처 그래픽 | 1024×500, PNG/JPG | 1024×500, RGB, 458KB |
| `screenshot-01.png` | 휴대전화 스크린샷 1 | 1080×1920 (9:16) | 1080×1920, PNG |
| `screenshot-02.png` | 휴대전화 스크린샷 2 | 1080×1920 | 1080×1920, PNG |
| `screenshot-03.png` | 휴대전화 스크린샷 3 | 1080×1920 | 1080×1920, PNG |
| `screenshot-04.png` | 휴대전화 스크린샷 4 | 1080×1920 | 1080×1920, PNG |
| `screenshot-05.png` | 휴대전화 스크린샷 5 | 1080×1920 | 1080×1920, PNG |

스크린샷 순서(Play Console 업로드 순서와 같게):

1. **매일 아침 9:30, 시장 핵심만 한눈에** — 홈 요약·관심 지표 핀·코스피/BTC/KRX 금
2. **미국·국내 증시부터 코인·금까지** — 5개 섹션 지표 카드 + 공포·탐욕 지수
3. **자산별 흐름을 한 그래프에** — 추이 차트(첫날=100 환산)와 11/24 값
4. **원문으로 이어지는 주요 뉴스와 전문** — 뉴스 카드·원문 링크·전문 카드
5. **목표가 알림과 홈 화면 위젯까지** — 09:35 알림·목표가 알림·Glance 위젯

> 스크린샷은 2026-11-24 실제 브리핑 데이터(Supabase `daily_briefings`)로 앱 UI를 그대로 재현해 만든 **홍보용 이미지**입니다. 이 환경에 안드로이드 기기/에뮬레이터가 없어 실기기 캡처가 아니므로, Play 정책상 문제는 없지만 실기기에서 같은 화면을 캡처해 두면 더 정확합니다(교체 방법은 8절).

---

## 2. 스토어 등록정보 문구

### 앱 이름 (30자 이내)

```
데일리 브리핑 - 경제·코인·금
```

*(17자. 앱 안에서 쓰는 이름은 `데일리 브리핑`이고, 스토어 검색을 위해 뒤에 주제어를 붙였습니다. 짧게 가려면 `데일리 브리핑`만 써도 됩니다.)*

### 간단한 설명 (80자 이내)

```
매일 아침 9:30, 미국·국내 증시와 코인·금 시세, 주요 뉴스를 한 화면에서. 투자 권유가 아닙니다.
```

*(58자)*

### 자세한 설명 (4,000자 이내)

```
매일 아침 9시 30분, 시장을 보는 데 필요한 숫자만 정리해 주는 개인용 브리핑 앱입니다.

미국 증시(다우·S&P500·나스닥)와 국내 증시(코스피·코스닥), 주요 5대 코인(비트코인·이더리움·리플·테더·솔라나), 국제 금과 국내 KRX 금현물, 원/달러 환율·미 10년물 금리·김치 프리미엄까지 한 화면에서 확인할 수 있습니다.

■ 이런 점이 편합니다
· 홈 한 화면 요약 — 그날의 핵심 이벤트와 지표를 카드로 먼저 보여 줍니다
· 관심 지표 핀 — 코스피·BTC·KRX 금처럼 자주 보는 지표를 상단에 고정
· 추이 차트 — 지수·금·코인·환율을 첫날=100으로 환산해 흐름을 비교하고, 좌우로 밀어 그날 값을 확인
· 주요 뉴스와 원문 — 출처가 표시된 뉴스 카드에서 원문으로 바로 이동
· 브리핑 전문 — 요약이 아니라 그날 브리핑 전체를 마크다운으로 읽을 수 있습니다
· 날짜별 다시 보기 — 지난 브리핑을 날짜 칩으로 넘겨 봅니다
· 북마크 — 다시 보고 싶은 날짜를 저장

■ 알림과 위젯
· 브리핑 알림 — 매일 09:35 무렵 새 브리핑이 올라오면 오늘의 헤드라인을 알려 줍니다
· 목표가 알림 — 코스피, KRX 금, 비트코인(달러·원), 원/달러에 목표가를 등록하면 값이 닿을 때 알려 줍니다
· 홈 화면 위젯 — 코스피·BTC·KRX 금과 헤드라인 2줄을 위젯에서 바로 확인

■ 데이터와 개인정보
· 회원가입과 로그인이 없습니다
· 관심 지표·목표가·북마크·테마 같은 설정은 기기 안에만 저장되고 서버로 보내지 않습니다
· 앱은 브리핑 조회를 위해 데이터베이스에 읽기 전용으로만 접속합니다
· 현재 버전에는 광고가 없습니다 (광고는 1.0.0에서 도입 예정이며, 도입 시 개인정보처리방침을 갱신해 알립니다)

■ 알아 두세요
· 이 앱은 시세와 뉴스를 정리해 보여 주는 정보 제공 앱이며, 투자 권유나 매매 자문이 아닙니다
· 모든 수치는 브리핑 기준 시각의 값이고, 국내 지표는 장중 값이라 이후 달라질 수 있습니다
· 일부 지표는 휴장일에는 직전 종가를 그대로 표시합니다

개인정보처리방침: https://clghks86.github.io/daily-briefing/privacy.html
문의: clghks@gmail.com
```

### 카테고리·태그

- **카테고리**: 금융 (Finance)
- **태그**: 주식, 암호화폐, 금, 경제 뉴스, 환율
- **연락처 이메일**: `clghks@gmail.com`
- **개인정보처리방침 URL**: `https://clghks86.github.io/daily-briefing/privacy.html`

---

## 3. 데이터 보안(Data safety) 설문 — 답변 가이드

현재 버전(0.5.0) 기준입니다. **광고를 붙인 1.0.0에서는 반드시 다시 신고해야 합니다.**

| 질문 | 답 |
|---|---|
| 앱이 필수 사용자 데이터 유형을 수집하거나 공유하나요? | **아니요** |
| 데이터 유형 선택 (위치, 개인 정보, 금융 정보, 메시지, 사진, 파일, 건강, 연락처, 앱 활동, 웹 검색, 앱 정보·성능, 기기 ID 등) | **아무것도 선택하지 않음** |
| 전송 중 데이터가 암호화되나요? | 예 (HTTPS만 사용) |
| 사용자가 데이터 삭제를 요청할 수 있나요? | 해당 없음 — 수집하는 데이터가 없어 이 항목은 나타나지 않습니다 |
| 앱에 광고가 포함되어 있나요? | **아니요** (1.0.0에서 AdMob 도입 시 "예"로 바꾸고 광고 ID 수집을 신고) |
| 앱이 Google Play 가족 정책 대상인가요? | 아니요 |

설문에서 "계정 생성이 필요하지 않음"으로 답하세요. 앱은 로그인·회원가입이 없습니다.

> 기기에 저장되는 설정값(핀·목표가·북마크)은 **기기 밖으로 나가지 않으므로** 데이터 보안 설문의 "수집"에 해당하지 않습니다. 다만 이 내용은 개인정보처리방침 2항에 명시해 두었습니다.

---

## 4. 앱 콘텐츠 / 앱 액세스

| 항목 | 답 |
|---|---|
| 앱 액세스 권한 | **모든 기능이 제한 없이 제공됩니다** (로그인 없음) |
| 콘텐츠 등급 설문 | 폭력·성적 콘텐츠·욕설·도박·약물·공포 요소 **모두 없음**. 사용자 생성 콘텐츠·채팅·위치 공유·디지털 구매 없음. 뉴스/정보 링크만 제공 → 일반적으로 **전체이용가** 로 산정됩니다 |
| 타겟층 | **만 18세 이상** (금융 정보 앱, 아동 대상 아님) |
| 뉴스 앱 여부 | 앱은 자체 취재·보도를 하지 않고, 외부 기사의 제목·요약과 원문 링크를 보여 주는 개인 브리핑 뷰어입니다. Play Console이 뉴스 앱 신고를 요구하면 이 사실대로 답하세요 |
| 정부 승인/금융 라이선스 | 해당 없음 (투자 자문·중개·결제 기능 없음) |

---

## 5. 서명 키 만들기 (최초 1회)

릴리스 키는 **잃어버리면 같은 앱을 다시 올릴 수 없습니다.** 아래 파일은 git에 커밋하지 마세요(`android/.gitignore`에 이미 `*.jks`, `keystore.properties` 제외 규칙이 있는지 확인).

```bash
cd work/daily-briefing/android

keytool -genkey -v \
  -keystore daily-briefing-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias daily-briefing
# 이름/조직/도시/국가(KR)를 묻습니다. 비밀번호는 따로 안전하게 보관하세요.
```

`android/keystore.properties` (git 제외):

```properties
storeFile=daily-briefing-release.jks
storePassword=********
keyAlias=daily-briefing
keyPassword=********
```

`app/build.gradle.kts`의 `android { }` 블록에 추가:

```kotlin
val keystoreProps = java.util.Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    signingConfigs {
        create("release") {
            if (keystoreProps.isNotEmpty()) {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")  // 키가 없으면 서명 없이 빌드
        }
    }
}
```

> Play 앱 서명(Play App Signing)을 쓰면 업로드 키만 있으면 되고, 키를 잃어도 Google에 재설정을 요청할 수 있습니다. 첫 업로드 때 **"Google에서 앱 서명 키 관리"** 를 선택하는 것을 권장합니다.

## 6. 릴리스 빌드 & 업로드

```bash
cd work/daily-briefing/android

./gradlew clean testDebugUnitTest      # 단위 테스트 48개
./gradlew bundleRelease                # app/build/outputs/bundle/release/app-release.aab
./gradlew assembleRelease              # 실기기 설치용 APK (선택)

# 업로드 전 확인
bundletool build-apks --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=/tmp/app.apks --mode=universal   # (bundletool이 있을 때)
```

1. Play Console → 앱 만들기 → 기본 언어 **한국어**, 앱 이름·게임 여부(앱), 무료
2. **앱 설정**: 개인정보처리방침 URL, 앱 액세스 권한, 광고 여부, 콘텐츠 등급, 타겟층, 데이터 보안
3. **스토어 등록정보**: 2절 문구 + 1절 에셋 업로드
4. **테스트 및 출시 → 내부 테스트**: 위 AAB 업로드 → 본인 계정으로 설치해 알림·위젯 확인 (가장 빠른 스모크 테스트)
5. **비공개 테스트**: 개인 개발자 계정이 2023-11-13 이후 만들어졌다면 **테스터 12명 이상이 14일 연속 옵트인** 상태여야 프로덕션 액세스를 신청할 수 있습니다. (해당 여부는 Play Console 대시보드에 "프로덕션 액세스 신청" 단계가 보이는지로 확인)
6. **프로덕션**: 액세스 승인 후 출시. `versionCode`는 업로드마다 1씩 올립니다

---

## 7. 출시 전 사용자가 직접 해야 하는 일

1. **판매자(트레이더) 상태로 전환** — 현재 Play Console 계정은 비판매자로 선언되어 있습니다. 광고를 붙인 앱을 배포하려면 판매자 상태 전환이 필요하고, 한국 개발자 추가 정보의 임시값(`111111`)을 실제 값으로 바꿔야 합니다.
2. **서명 키 생성·보관** (5절) — 이 작업 환경에는 JDK가 있으나 키는 사용자 PC/안전한 곳에 두는 것이 좋습니다.
3. **실기기 확인** — 이 환경에는 에뮬레이터가 없어 앱 화면·알림·위젯이 실제 기기에서 확인되지 않았습니다. Play 업로드 전 내부 테스트로 한 번 설치해 확인하세요.
4. **실기기 스크린샷 교체(선택)** — 8절.
5. **광고 도입 시** (1.0.0) 개인정보처리방침 7항 갱신 + 데이터 보안 재신고 + 앱 내 동의 절차.

## 8. 실기기 스크린샷으로 교체하려면

1. 기기에서 앱 실행 → 각 화면(홈 / 핵심 지표 / 추이 / 주요 뉴스 / 설정·알림)에서 전원+볼륨다운으로 캡처
2. 1080×1920(9:16)로 자르고, 상단에 홍보 문구를 넣고 싶으면 기존 이미지의 문구 영역만 참고해 같은 배치로 합성
3. 파일명을 `screenshot-01.png` ~ `screenshot-05.png` 로 바꿔 Play Console에서 교체 업로드
   (이 폴더의 파일을 덮어쓰고 `git commit` 해 두면 나중에 비교하기 쉽습니다)

---

## 9. 참고 링크

- 개인정보처리방침: <https://clghks86.github.io/daily-briefing/privacy.html>
- 웹 대시보드: <https://clghks86.github.io/daily-briefing/>
- 앱 소스: `work/daily-briefing/android/` (저장소 `clghks86/daily-briefing`)
- Play Console 데이터 보안 도움말: <https://support.google.com/googleplay/android-developer/answer/10787469>
