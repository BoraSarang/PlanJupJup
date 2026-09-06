# API 명세 — PlanJupJup 내장 서버 (Ktor CIO, 기본 포트 3000)

> 베이스: `http://<S22 로컬 IP>:3000` · 응답: JSON · CORS: 같은 망 브라우저 허용

## GET /

- 웹 포털 정적 HTML (`assets/web/index.html`)

## GET /api/health

- 응답: `{"status":"ok","timestamp":1699999999999}`

## GET /api/plans

- 쿼리: `network`(5G/LTE), `carrier`(SKT/KT/LGU+), `minData`(GB 정수),
  `maxPrice`(원), `sort`(price_asc/price_desc/data_desc/newest, 기본 price_asc),
  `tag`(큐레이션 태그), `page`(기본 1), `pageSize`(기본 50, 최대 100)
- 응답:
```json
{
  "plans": [
    {
      "id": "tplus-5g-15gb",
      "carrierName": "티플러스",
      "mvnoNetwork": "LGU+",
      "planName": "5G 티플 가성비(300분/15GB)",
      "price": 10000,
      "priceAfterDiscount": 24200,
      "discountMonths": 6,
      "dataAmount": "15GB",
      "voice": "300분",
      "sms": "300건",
      "networkType": "5G",
      "eventBadge": "CU 20% 할인",
      "collectedAt": 1699999999999,
      "firstCollectedAt": 1699900000000,
      "isNew": false,
      "tags": "가성비청년",
      "sources": [
        {"sourceName": "알뜰폰허브", "sourceUrl": "https://www.mvnohub.kr/product/products/7177.do"}
      ]
    }
  ],
  "total": 128,
  "page": 1,
  "pageSize": 50
}
```

## GET /api/plans/{id}

- 성공 200: Plan + sources 전체. 없음 404: `{"error":"Not found"}`

## GET /api/sources

- 응답: `[{"id":"mvnohub","name":"알뜰폰허브","type":"COMPARE_SITE","enabled":true,"intervalHours":6,"intervalMinutes":360,"lastRunAt":1699999999999,"lastStatus":"SUCCESS","errorMessage":null}]`
- `intervalMinutes`: 수집 주기(분). 기준값.

## POST /api/sources/{id}/toggle

- 해당 소스 enabled 반전. 성공 200. 없음 404.

## POST /api/sync

- 바디: `{"sourceId": null}` (null이면 전체 활성 소스)
- 응답 202 Accepted (WorkManager에 위임, 비동기 실행)

## GET /api/stats

- 응답: `{"totalPlans":128,"activeSources":4,"lastCollectedAt":1699999999999}`
- 홈 3열과 호환 유지 (통계 대시보드와 별개)

## GET /api/stats/overview

- KPI 대시보드 전용. 응답:
```json
{
  "generatedAt": 1699999999999, "cache": "memory",
  "totalPlans": 432, "brandCount": 26, "networkCount": 3, "newThisWeek": 5,
  "avgPrice": 16915, "minPrice": 10, "maxPrice": 69000,
  "unlimitedRatio": 0.31, "g5Ratio": 0.27, "avgDataGb": 30.7,
  "crawlCountToday": 52, "crawlFail24h": 14, "lastCollectedAt": 1699999999999
}
```

## GET /api/stats/brands

- 쿼리: `brand`(선택, 특정 브랜드만 필터)
- 응답: `{"generatedAt":ts,"cache":"memory","brands":[{"brand":"KT엠모바일","mvnoNetwork":"KT","planCount":196,"newCount":196,"avgPrice":23106,"minPrice":2900,"maxPrice":69000,"avgDataGb":23.9,"g5Count":57,"pricePerGb":965}]}`
- `pricePerGb`: 파싱 가능 행 기준 (평균가 ÷ 평균데이터), 없으면 null
- 브랜드 드릴다운은 `GET /api/plans?carrier={brand}` 재사용

## GET /api/stats/networks

- 3사 망 비교. 응답:
```json
{"generatedAt":ts,"cache":"memory","networks":[
  {"network":"KT","planCount":284,"avgPrice":20106,"minPrice":10,"maxPrice":69000,"avgDataGb":29.4,"g5Ratio":0.28,"unlimitedRatio":0.0,"pricePerGb":683}]}
```

## GET /api/stats/distribution

- 쿼리: `type`(`price`|`data`, 기본 price)
- 응답: `{"generatedAt":ts,"cache":"memory","type":"price","buckets":[{"label":"1만원 미만","count":163,"min":0,"max":10000},...]}`
- 가격대 버킷: 1만 미만 / 1~2 / 2~3 / 3~5 / 5~10 / 10만 이상 (개방)
- 데이터 버킷: 1GB 이하 / 2~5 / 6~15 / 16~50 / 51~100 / 100GB 이상 + 파싱 불가 (min/max null)

## GET /api/stats/trends

- 쿼리: `type`(`crawl`|`new`, 기본 crawl), `gran`(`daily`|`hourly`, 기본 daily), `days`(기본 30, hourly는 1~2만 유효)
- `crawl`: crawl_logs 기준 발견/신규/갱신/실패
- `new`: plans.firstCollectedAt 기준 신규 수
- 응답: `{"generatedAt":ts,"cache":"memory","type":"crawl","gran":"daily","points":[{"label":"08-30","ts":1788015600000,"plansFound":0,"plansNew":0,"plansUpdated":0,"failCount":0},...]}`
- 캐시는 TTL 5분이며 saveCrawlResults/cleanup 시 invalidate

## GET /api/stats/value-ranking

- 쿼리: `network`(`5G`|`LTE`, 선택), `limit`(기본 10, 최대 50)
- 스코어 = (dataGb + voiceMin×0.3 + smsCount×0.1) ÷ (price/1000).
  무제한=100GB·2000분·2000건 가정치. price≤0·파싱 실패·benefit≤0 제외.
- 응답: `{"generatedAt":ts,"cache":"memory","items":[{"rank":1,"id":"...","carrierName":"...","planName":"...","price":10,"dataAmount":"10GB","voice":"무제한","sms":"무제한","networkType":"LTE","dataGb":10,"score":81000.0,"scoreLabel":"81000.0"},...]}`

## GET /api/stats/collection-health

- 응답:
```json
{"generatedAt":ts,"cache":"memory","success24h":38,"fail24h":14,"avgDurationSec":79.7,"lastCollectedAt":ts,
 "sources":[{"sourceId":"brand_list","sourceName":"알뜰폰허브 브랜드목록","lastStatus":"SUCCESS","lastRunAt":ts,"errorMessage":null,"successRate":0.8},...]}
```

## GET /api/stats/insights

- 자동 인사이트 (최대 5개, 중복 키 방지). 응답:
```json
{"generatedAt":ts,"cache":"memory","insights":[
  {"type":"positive","title":"브랜드 보유 1위","text":"KT엠모바일이 196개로 가장 많은 요금제를 보유했습니다 (평균 23106원)."},
  {"type":"info","title":"네트워크 구성","text":"수집된 요금제의 5G 비중은 약 27%입니다."},
  {"type":"warning","title":"수집 실패 감지","text":"최근 24시간 수집 실패가 14건 발생했습니다."}]}
```

## GET /api/settings

- 응답: `{"port":3000,"retentionDays":30,"autoStart":true,"watchdogIntervalSec":60,"notifCrawlComplete":true,"notifNewPlan":true,"notifFailure":true}`

## POST /api/settings

- 바디(부분 허용): `{"port":3000,"retentionDays":30,"autoStart":true,"watchdogIntervalSec":60,"notifCrawlComplete":true,"notifNewPlan":true,"notifFailure":true}`
- 포트 변경 시 서버 자동 재시작. 성공 200.

## GET /api/notifications

- 쿼리: `type`(CRAWL_COMPLETE/NEW_PLANS_FOUND/CRAWL_FAILED_STREAK/CRAWL_SUMMARY),
  `isRead`(true/false), `page`(기본 1), `pageSize`(기본 20, 최대 100)
- 응답:
```json
{
  "notifications": [
    {"id": 1, "type": "CRAWL_COMPLETE", "summary": "수집 완료: ...", "createdAt": 1699999999999, "isRead": false}
  ],
  "total": 1, "page": 1, "pageSize": 20, "unreadCount": 1
}
```

## GET /api/notifications/{id}

- 알림 + 상세(detailJson 파싱): `{"notification":{...},"detail":{type,summary,totalFound,newPlans,updatedPlans,failedCount,bySource[],byBrand[],byNetwork[],newPlansDetail[],failedSources[],startedAt,finishedAt}}`
- 없음 404.

## POST /api/notifications/{id}/read

- 해당 알림 읽음 처리. 응답 `{"updated":1}`.

## POST /api/notifications/read-all

- 전체 읽음 처리. 응답 `{"updated":N}`.

## DELETE /api/notifications/{id}

- 알림 삭제. 응답 `{"deleted":1}`.

## POST /api/notifications/cleanup

- 보관 기간(데이터 보관일) 초과 알림 정리. 응답 `{"deleted":N}`.

## GET /api/notifications/unread-count

- 응답: `{"unreadCount":3}`

## 에러 응답 공통

- 형식: `{"error":"메시지"}` + HTTP 상태코드(400/404/500)
- 서버 내부 예외는 StatusPages에서 500으로 래핑, 로그에 `[ERROR] E-AND-SRV-...` 기록
