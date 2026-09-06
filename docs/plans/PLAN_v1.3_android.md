# PLAN_v1.3_android — 수집 통계·인사이트 대시보드

> 플랫폼: Android 네이티브(Kotlin) · 패키지: `com.borasarang.planjupjup` · 웹 포털: `assets/web`
> 작성일: 2026-09-06 · 상태: 승인됨(사용자 "권장대로 진행" 확정)

## 1. 한 줄 설명

수집된 요금제·수집 이력을 바탕으로 **KPI 대시보드 + 시계열 수집 추이 + 브랜드/통신망/가격/데이터 분포 + 가성비 랭킹 + 자동 인사이트**를 웹 포털에서 제공한다. 기존 6개 테이블(스키마 확장 없음)로 구현하고, 부족분은 Kotlin 파싱(DataAmount/Voice/Sms)으로 보완한다.

## 2. 확정 사항 (사용자 결정, 2026-09-06)

| # | 항목 | 결정 | 비고 |
|---|------|------|------|
| 1 | 차트 구현 | **바닐라 JS + 인라인 SVG/CSS** (Chart.js 미도입) | 포털 "프레임워크 없음" 원칙 유지, 오프라인·용량 안전 |
| 2 | 가성비 스코어 | **가중치 공식** (투명, 설명 가능) | `(dataGb + voiceMin×0.3 + smsCount×0.1) ÷ (price/1000)` |
| 3 | 캐시 | **StatsRepository 5분 TTL 메모리 캐시** | 변경 시 무효화(saveCrawlResults/cleanup 연계) |
| 4 | 네이티브 범위 | v1.3 **미포함** (홈 3열 통계 유지) | 네이티브 최소 원칙, 의존성 추가 없음 |
| 5 | 브랜드 상세 | 포털 내 인라인 확장(새 화면 아님) | 필터·드릴다운은 JS 상태로 처리 |
| 6 | 데이터 신뢰성 | 파싱 실패 시 기본값 + 원문 보관 + STALE 표시 | logs 경고, 통계 응답에 `parsedCount/rawCount` |

## 3. 리서치 요약 (웹 리서치 반영)

- **의사결정 우선 설계**: 각 차트에 1줄 "이 데이터로 알 수 있는 것" 요약 제공
- **차트 유형 선택**: 비교=막대, 시간 추이=꺾은선/영역, 구성 비율=도넛(3~6 세그먼트), 분포=히스토그램(가격대)
- **Y축 0 시작 의무**: 가격/건수 차트 왜곡 방지
- **알뜰폰 시장 니즈**: 데이터 단가(GB당 원), 5G/LTE 비중, 무제한/QoS(Mbps), 통화·문자 기본제공 여부, 프로모션(할인 후 실질가)
- **접근성**: 색 3색 한정 + 라벨 병행, `prefers-reduced-motion` 존중, 키보드 포커스

## 4. 아키텍처

```
┌────────── 웹 포털 (assets/web) ──────────────────────────┐
│ header + [📊 통계] 버튼 → 통계 패널(섹션) 렌더              │
│   ├─ KPI 카드 그리드 (overview)                           │
│   ├─ 저장 커버리지: 브랜드별 요금제 수·평균가 (막대)         │
│   ├─ 통신망 비교: KT/LGU+/SKT 요금제 수·평균가·GB당 단가    │
│   ├─ 수집 추이: 일별 발견/신규/갱신/실패 (영역/라인)         │
│   ├─ 신규 요금제 추이: 일별 new (firstCollectedAt 기준)     │
│   ├─ 가격대 분포 (히스토그램) + 데이터용량 구간 분포          │
│   ├─ 가성비 TOP 랭킹 (표 + 스코어)                          │
│   └─ 자동 인사이트 카드 (긍정/주의/정보)                    │
└───────────────────────────────────────────────────────────┘
┌────────── 백엔드 (Ktor) ──────────────────────────────────┐
│ GET /api/stats/overview                                   │
│ GET /api/stats/brands                                 [5분]│
│ GET /api/stats/networks   ── StatsRepository ── 캐시      │
│ GET /api/stats/distribution?type=price|data               │
│ GET /api/stats/trends?type=crawl|new&gran=daily|hourly    │
│ GET /api/stats/value-ranking?network=&limit=              │
│ GET /api/stats/collection-health                          │
│ GET /api/stats/insights                                   │
└───────────────────────────────────────────────────────────┘
┌────────── Room ───────────────────────────────────────────┐
│ plans(기존) + crawl_logs(기존) — 스키마 변경 없음          │
│ SQL GROUP BY 집계 + Kotlin 파싱(GB/QoS/분/건)              │
└───────────────────────────────────────────────────────────┘
```

## 5. 데이터 파싱 규칙 (PlanMetrics)

```kotlin
// dataAmount "15GB" → 15, "무제한+5Mbps" → 100(무제한 가정치)+qos 5
data class DataAmount(val dataGb: Int, val isUnlimited: Boolean, val qosMbps: Int?)
// "15GB", "무제한", "무제한+5Mbps", "100GB+3Mbps" 파싱. 파싱 불가 → dataGb=0, 유효성 false
fun parseDataAmount(text: String): DataAmount

// voice "무제한"/"기본제공" → 2000분(가정 가중값), "300분" → 300. 숫자 없으면 0
fun parseVoiceMinutes(text: String): Int
// sms "무제한"/"기본제공" → 2000건, "300건" → 300
fun parseSmsCount(text: String): Int
```

- 무제한 판정: `dataAmount`가 "무제한"을 포함하거나 GB값이 0이 아닌데 "→∞". 실제 단가 비교를 위해 **무제한=100GB 가정치** 사용 (UI에 "가정치" 라벨)
- QoS: `(\d+)Mbps` 정규식. 없으면 null
- 파싱 실패(0GB) 행은 분포·평균에서 제외, `unparsed` 카운트 별도 집계

## 6. 집계 쿼리 (Room)

```
-- 브랜드별: 요금제수, 평균/최소/최대가, 망, 네트워크 타입, 신규 수
SELECT carrierName AS brand, mvnoNetwork AS mvno, networkType AS network,
       COUNT(*) AS planCount, AVG(price) AS avgPrice, MIN(price) AS minPrice,
       MAX(price) AS maxPrice, SUM(CASE WHEN isNew=1 THEN 1 ELSE 0 END) AS newCount
FROM plans GROUP BY carrierName, mvnoNetwork

-- 통신망 비교
SELECT mvnoNetwork AS mvno, COUNT(*) planCount, AVG(price) avgPrice, MIN/MAX...
       SUM(CASE WHEN networkType='5G' THEN 1 ELSE 0 END) AS g5Count,
       SUM(CASE WHEN isNew=1 THEN 1 ELSE 0 END) AS newCount
FROM plans GROUP BY mvnoNetwork

-- 일별 수집 추이 (crawl_logs)
SELECT substr(startedAt/86400000,1,10) AS day,  -- kotlin 그룹핑 예정
       SUM(plansFound), SUM(plansNew), SUM(plansUpdated),
       SUM(CASE WHEN status='FAILED' THEN 1 ELSE 0 END) AS failCount
FROM crawl_logs GROUP BY day

-- 일별 신규 요금제 추이 (plans)
SELECT ... GROUP BY firstCollectedAt 일자
```
- 시간별(hourly)은 Kotlin 그룹핑 (가독성·로컬 타임존)
- 전체 데이터 필요 시 `getAll()`(id/가격/망/데이터/음성/문자/네트워크/태그/플래그만) fetch — 3천건 규모는 수 ms

## 7. 인사이트 규칙 (자동 생성)

| 유형 | 규칙 예시 |
|------|-----------|
| 긍정(positive) | "가장 저렴한 브랜드는 X (평균 N원)", "이번 주 신규 Y개 — Z 브랜드가 활발" |
| 주의(warning) | "수집 실패 5건 연속 감지", "무제한 요금제 평균 단가가 업계 평균 대비 ▲" |
| 정보(info) | "5G 비중 60%", "GB당 평균 단가 KT vs LGU+ 비교" |
- 최대 5개, 우선순위 점수(영향도×신선도)로 정렬
- 같은 문구 반복 방지: 문장 템플릿에서 태그 교체, 최대 100자

## 8. API 설계 (상세: docs/api/ENDPOINTS.md 갱신)

| Method | Path | 설명 |
|---|---|---|
| GET | `/api/stats/overview` | KPI: 전체/브랜드수/망수/이번주신규/평균·최저·최고가/무제한비중/5G비중/24h수집 성공·실패 |
| GET | `/api/stats/brands` | 브랜드별 집계 + brand 옵션 시 상세(신규 추이·요금제 미리보기) |
| GET | `/api/stats/networks` | 3사 망 비교 (요금제수/평균·최저가/GB당 단가/5G·무제한 비중) |
| GET | `/api/stats/distribution?type=price\|data` | 가격대 히스토그램 / 데이터 용량 구간 분포 |
| GET | `/api/stats/trends?type=crawl\|new&gran=daily\|hourly&days=30` | 수집 추이 / 신규 요금제 추이 |
| GET | `/api/stats/value-ranking?network=&limit=10` | 가성비 TOP (스코어+해체) |
| GET | `/api/stats/collection-health` | 소스별 성공률·평균 소요시간·최근 실패, 신선도 |
| GET | `/api/stats/insights` | 자동 인사이트 문장 (최대 5개) |
| GET | `/api/stats` (기존) | 홈 3열과 호환 유지 (overview 축소판) |

- 응답 공통: `{ "generatedAt": ts, "cache": "memory", ...데이터 }`
- 통계 응답 크기: ≤ 50KB (브랜드 스코어 분포 포함 시 개수 상한)

## 9. 웹 포털 UI 구성

```
헤더: [💡 통계] 버튼 추가 (토글) → main 위에 stats 섹션 표시
통계 섹션:
├─ KPI 카드 그리드 (8장 sensors): 전체 요금제/브랜드/통신망/이번주 신규/
│   평균가/최저가/무제한 비중/5G 비중 (각 카드: 수치+라벨+변화)
├─ 저장 커버리지(브랜드별 막대) + 통신망 비교(3열 카드)
├─ 수집 추이 (영역/라인: 발견·신규·실패) + 신규 요금제 추이(라인) — gran 토글
├─ 가격대 분포(바) + 데이터 용량 구간(도넛)
├─ 가성비 TOP10 (리스트: 순위·브랜드·요금제·데이터·가격·스코어 막대)
└─ 인사이트 카드 (⚠️/💡/✅ 아이콘 + 문장)
접힘: "통계" 재클릭 시 닫힘. 요금제 검색과 상태 독립
차트: 인라인 SVG 함수 renderBars/renderLines/renderDonut (바닐라)
컬러팔레트: primary #0066FF / warning #E65100 / success #34A853 / 3사(red/violet/blue)
반응형: 모바일 1열, 카드 min-width 제거, 터치 44px
```

## 10. 네이티브

- v1.3 변경 없음 (홈 3열 통계 유지). 향후 v1.4에서 홈 카드 확장 검토
- 통계 API가 내려주는 JSON은 홈에서 사용 가능하게 `PlanStats` 확장은 하지 않음

## 11. 단위 테스트

| 테스트 | 검증 |
|--------|------|
| PlanMetricsTest | parseDataAmount(GB/무제한/QoS), parseVoiceMinutes, parseSmsCount 파싱/경계/무제한 |
| StatsModelTest | 분포 버킷 계산, 가성비 스코어 공식(0원·무제한·경계), 추이 그룹핑(일/시간) |

## 12. 비기능

- **성능**: aggregation 쿼리는 인덱스 사용(plans.mvnoNetwork/price/collectedAt/crawl_logs.startedAt), 전체 fetch 3천건 < 20ms, 응답 < 300ms, 캐시 5분 TTL
- **메모리**: 집계 중간 데이터만 유지 (원본 전체 방출), 표 차트는 상위 10개만 렌더
- **로깅**: 포털 진입 `[INFO] [FEATURE] 통계대시보드`, API 오류 `[ERROR] E-AND-SRV-0105` + 신규 error_code 3개

## 13. 에러코드 (error_message_ko.json 추가)

- `E-AND-SRV-0105`: "통계 데이터를 불러오는데 실패했습니다"
- `E-AND-SRV-0106`: "가성비 랭킹 산출에 실패했습니다"
- `E-AND-SRV-0107`: "인사이트 생성에 실패했습니다"

## 14. 마일스톤 (약 1~1.5일)

1. M1 PlanMetrics 파싱 유틸 + 테스트 (kotlin, ui 아님)
2. M2 DAO 집계 쿼리 + StatsRepository + 관측/캐시 + 테스트
3. M3 Ktor 라우트 8종 + JSON 직렬화
4. M4 포털: 통계 섹션 HTML/CSS/JS(SVG 차트) + 접근성/반응형
5. M5 빌드·단위·lint·S22 E2E + 문서(ENDPOINTS/DESIGN/TODO/CHANGELOG/error_code) 마감

## 15. DoD

- `./gradlew assembleDebug` 성공, lint Error 0, 단위 테스트 통과 (기존 23 + 신규)
- S22 실기: 포털 통계 버튼 → KPI/차트/랭킹/인사이트 렌더, API 응답 스키마 검증
- 실데이터(430건+) 기준 분포·평균·단가 정합성(알뜰폰허브 공식 수치와 오차 범위)
- 문서: ENDPOINTS·DESIGN·TODO·CHANGELOG·error_message_ko.json 갱신, 세션 로그 작성