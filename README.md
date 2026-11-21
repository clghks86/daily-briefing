# 데일리 브리핑 대시보드

매일 쌓이는 경제·암호화폐·금 브리핑(Supabase `daily_briefings`)을 한 화면에서 보는 정적 대시보드입니다.
빌드 도구 없이 `index.html` 한 파일로 동작하고, GitHub Pages에서 바로 서빙됩니다.

- 라이브: https://clghks86.github.io/daily-briefing/
- 저장소: https://github.com/clghks86/daily-briefing

## 화면 구성

| 영역 | 내용 |
| --- | --- |
| 날짜 선택 | DB에 실제로 있는 `briefing_date`만 버튼으로 나열. 오늘(한국시간) 브리핑이 있으면 오늘, 없으면 최신 날짜를 기본 선택 |
| 오늘의 요약 | `title` · `summary` · `details.as_of_kst` · `details.key_events` · `details.kr_market_note` |
| 핵심 지표 | 미국 증시(나스닥·S&P500·다우), 국내(코스피·코스닥), 금(국제 $/oz · KRX 원/g), 코인(BTC·ETH·XRP·SOL), 환율·금리(원/달러·미 10년물·김치프리미엄·USDT), 공포·탐욕 지수 |
| 추이 | Chart.js. `지수` / `금` / `코인` / `환율·금리` 탭. 가격대가 다른 자산은 첫 표시일=100으로 환산해 비교 |
| 주요 뉴스 | `details.news` 배열(`category`, `title`, `summary`, `source`, `url`)을 카드로 표시. 없는 날짜는 안내 문구 |
| 브리핑 전문 | `raw_markdown`을 marked + DOMPurify로 렌더. 표는 모바일에서 가로 스크롤 |
| 첨부 이미지 | `briefing_images`에 그 날짜 행이 있으면 자동으로 표시(현재 0건) |

등락률 색은 국내 관례를 따릅니다: **상승=빨강, 하락=파랑**.

## 데이터

Supabase REST API를 publishable 키로 직접 조회합니다.

```
GET {SUPABASE_URL}/rest/v1/daily_briefings?select=*&order=briefing_date.desc
GET {SUPABASE_URL}/rest/v1/briefing_images?briefing_date=eq.{date}&order=sort_order.asc
```

`daily_briefings` 컬럼: `briefing_date, title, summary, us_dow, us_dow_chg, us_sp500, us_sp500_chg, us_nasdaq, us_nasdaq_chg, kr_kospi, kr_kospi_chg, kr_kosdaq, kr_kosdaq_chg, usd_krw, us_10y_yield, gold_intl_usd, gold_intl_chg, gold_krx_krw, gold_krx_chg, btc_usd, btc_krw, btc_chg, eth_usd, eth_krw, eth_chg, xrp_usd, xrp_krw, xrp_chg, usdt_usd, usdt_krw, sol_usd, sol_krw, sol_chg, kimchi_premium, fear_greed_index, details, raw_markdown`

`details`(jsonb)에서 화면이 읽는 키: `as_of_kst`, `key_events[]`, `kr_market_note`, `dxy`, `wti_usd`, `vix`, `gold_etf{ace_krx,ace_chg,tiger_krx,tiger_chg}`, `usd_krw_offshore_weekend`, `news[]`.

`briefing_images` 컬럼: `id, briefing_date, image_url, caption, sort_order`.

## 보안

- 프론트엔드에는 **publishable 키만** 들어 있습니다 (`sb_publishable_...`). 이 키는 공개되어도 되는 키이고, 실제 접근 제어는 Supabase의 RLS가 담당합니다.
- **secret key / service_role 키는 절대 이 저장소나 `index.html`에 넣지 않습니다.** 노출되면 모든 데이터에 접근할 수 있습니다.
- 저장소가 Public이므로 커밋 전에 키가 섞이지 않았는지 확인하세요: `grep -r "sb_secret" .`

## 배포

```bash
bash deploy.sh
```

`deploy.sh`가 하는 일: 계정 확인 → 저장소 존재 확인(없으면 안내 후 종료) → 커밋 → push → Pages 활성화(main / root) → 라이브 URL이 HTTP 200이 될 때까지 대기 → 로컬 파일과 서빙된 파일의 해시가 같은지 확인.

## 로컬 수정

`index.html` 하나만 고치면 됩니다. CSS 변수는 파일 상단 `:root`에 모여 있습니다.
