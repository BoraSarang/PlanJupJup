# PLAN_v1.0_android — 알뜰요금줍줍(PlanJupJup) 개발 계획서

> 플랫폼: Android 네이티브(Kotlin) 단일 앱 · 타겟: Galaxy S22(One UI 8.0 / Android 16)
> 패키지: `com.borasarang.planjupjup` · 런처명: 요금줍줍 · 정식명: 알뜰요금줍줍
> 작성일: 2026-09-06 · 상태: 승인됨(사용자 확정 반영)

## 1. 한 줄 설명

갤럭시 S22를 서버로 삼아 알뜰폰(MVNO) 신규 요금제 정보를 주기 수집하고,
같은 네트워크의 브라우저(맥북·아이패드 등)에서 웹 포털로 열람하는 포털 앱.
앱이 크롤러 + DB + 웹서버 역할을 모두 수행한다.

## 2. 확정 사항 (사용자 결정, 2026-09-06)

| # | 항목 | 결정 |
|---|------|------|
| 1 | UI 프레임워크 | 기존 View + ViewBinding 유지 (네이티브 화면은 상태·설정·디버그 로그 최소 구성) |
| 2 | 패키지명 | `com.borasarang.planjupjup` 확정 |
| 3 | 초기 크롤링 대상 | 알뜰폰허브 + 모요 2곳으로 시작, MVP에 통신사 공식 1~2곳(KT엠모바일·LG헬로모바일) 포함 |
| 4 | 통신사 DB 확장 | `https://www.mvnohub.kr/brand.do` 크롤링으로 통신사 DB 구성 후 점진 추가 |
| 5 | 데이터 보관 기간 | 설정에서 30일/90일 선택, 기본값 30일 |
| 6 | 포트 | 기본 3000, 설정에서 변경 가능 |
| 7 | 테스트 | 단위(JUnit + MockK) + UI(Espresso) 둘 다 |
| 8 | 생존성 | 배터리 최적화 예외 요청 + START_STICKY + BootReceiver + Watchdog (DroidRelay 패턴 적용) |

## 3. 아키텍처

```
┌────────────── Android App (Kotlin, ViewBinding) ──────────────┐
│ [Foreground Service: HttpServerService, type=dataSync]         │
│   ├─ Ktor CIO 임베디드 서버 (0.0.0.0:3000)                     │
│   │    ├─ GET  /                  → 포털 HTML (assets/web)     │
│   │    ├─ GET  /api/plans         → 목록 (필터/정렬/페이징)    │
│   │    ├─ GET  /api/plans/:id     → 상세 + 출처 목록           │
│   │    ├─ GET  /api/sources       → 소스 상태                  │
│   │    ├─ POST /api/sources/:id/toggle → on/off                │
│   │    ├─ POST /api/sync          → 즉시 수집                  │
│   │    ├─ GET  /api/stats         → 홈 통계                    │
│   │    └─ GET/POST /api/settings  → 설정 조회/저장             │
│   ├─ Watchdog 코루틴 (헬스체크+자동 재시작)                    │
│   └─ WorkManager (주기 크롤링, 소스별 개별 스케줄)              │
│ [Room DB] plans / plan_sources / sources / carrier_brands /     │
│           crawl_logs                                           │
│ [UI] Home(상태·주소·통계·수동수집) / SourceManage / Settings     │
│      (배터리예외·포트·보관기간·자동시작·Watchdog주기)           │
│ [Receiver] BootReceiver (부팅 시 autoStart면 서버 자동 시작)    │
└──────────────────────────────────────────────────────────────┘
```

## 4. 기술 스택 (고정)

| 영역 | 선택 | 비고 |
|---|---|---|
| 언어 | Kotlin 2.0 | |
| UI | View + ViewBinding + Material3 Components | Compose 미사용 |
| 웹서버 | Ktor 3.x CIO 엔진 | Netty 대비 경량·Android 친화 |
| DB | Room 2.6.x + Paging | 인덱스 전략 포함 |
| 백그라운드 | WorkManager 2.9.x (CoroutineWorker) | 제약: 네트워크 연결 + 배터리부족 제외 |
| 파싱 | Jsoup 1.18.x | |
| 포털 프론트 | 순수 HTML/CSS/바닐라 JS (assets/web) | 프레임워크 없음 |
| 설정 저장 | DataStore Preferences | 포트·보관기간·자동시작·Watchdog주기 |
| 로그 | Timber + DebugLogger 경유 + crawl_logs 테이블 | [INFO]/[ERROR] + 에러코드 |
| 테스트 | JUnit4 + MockK(단위), Espresso(UI) | |

## 5. 데이터 모델 (Room)

- `plans`: id(정규화키, PK), carrierName, mvnoNetwork(SKT/KT/LGU+),
  planName, price, priceAfterDiscount?, discountMonths?,
  dataAmount(원문), voice, sms, networkType(5G/LTE),
  eventBadge?, collectedAt, isNew, sourceId?, tags?(CSV)
- 인덱스: (carrierName,planName) unique, mvnoNetwork, networkType,
  price, collectedAt, isNew, tags
- `plan_sources`: (planId, sourceName) 복합PK, sourceUrl, fetchedAt, rawDataJson?
- `sources`: id PK, name, type(COMPARE_SITE/CARRIER_SITE/BRAND_LIST),
  baseUrl, enabled, intervalHours, lastRunAt?, lastStatus, errorMessage?,
  selectorConfigJson?(셀렉터 외부화)
- `carrier_brands`: id PK, name, networkType, logoUrl?, homepageUrl?,
  planListUrl?, isActive, sortOrder, collectedAt
- `crawl_logs`: id 자동증가, sourceId, sourceName, startedAt, finishedAt?,
  status, plansFound, plansNew, plansUpdated, errorMessage?

### 중복 병합 규칙

- 키: `carrierName + planName` 정규화(공백/특수문자 제거, 소문자화, 64자 절단)
- 동일 키 다출처 수집 시 1개 Plan으로 병합, 출처는 `plan_sources`에 전수 기록
- 충돌 값은 최신 수집값이 대표값, 원문은 출처 URL로 확인

## 6. 크롤러 설계

- 공통 추상 `BaseCrawler`: 설정 파싱 → 목록 수집 → 항목 파싱 → 트랜잭션 저장,
  `inferNetwork`(통신사명→망 유추), `generateTags`(큐레이션 태그 자동 부여)
- `CrawlerFactory`: 소스 type+name으로 구현체 분기
- 셀렉터 외부화: `selectorConfigJson`을 DB에 저장 → 사이트 구조 변경 시 설정만 수정
- 예의: 요청 간격 1초 이상, User-Agent 명시, 상세페이지는 선택적
- 실패 격리: 크롤러별 try-catch, `crawl_logs` 기록 후 `Result.retry()`
- 초기 소스 5개 시드: mvnohub(6h), moyo(6h), ktmmobile(12h), liivm(12h),
  brand_list(168h, 주1회)

## 7. 생존성 설계 (DroidRelay 패턴 적용)

- Manifest: `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_DATA_SYNC`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`,
  `POST_NOTIFICATIONS`, `INTERNET`, `ACCESS_NETWORK_STATE`
- `HttpServerService`: `START_STICKY`, `onCreate` 최상단 `startForeground()`,
  Android 12+ `ForegroundServiceStartNotAllowedException` catch 후 백그라운드 유지,
  `onTaskRemoved`에서 상태 저장, `stop()` 시 서버 정리
- `BootReceiver`: `BOOT_COMPLETED` → `autoStart=true`면 `HttpServerService.start()`
- Watchdog: `watchdogIntervalSec`(기본 60초)마다 서버 null/무응답 체크 → 자동 재시작
- 설정 화면: `PowerManager.isIgnoringBatteryOptimizations()` 상태 표시,
  미허용 시 `ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` 인텐트 버튼,
  ON_RESUME마다 상태 갱신
- 참고: Android 16 `dataSync` 장시간 제한 대비 — Watchdog 재시작으로 대응

## 8. API 명세 요약 (상세: docs/api/ENDPOINTS.md)

| Method | Path | 설명 |
|---|---|---|
| GET | `/` | 포털 HTML |
| GET | `/api/health` | 헬스체크 |
| GET | `/api/plans` | 목록 (network, carrier, minData, maxPrice, sort, tag, page, pageSize) |
| GET | `/api/plans/:id` | 상세 + 출처 |
| GET | `/api/sources` | 소스 상태 |
| POST | `/api/sources/:id/toggle` | on/off |
| POST | `/api/sync` | 즉시 수집 (`{sourceId?}`) |
| GET | `/api/stats` | 통계 |
| GET/POST | `/api/settings` | 설정 조회/저장 |

## 9. 웹 포털 UI (브라우저)

- 상단: 큐레이션 태그 칩(가성비청년/해비유저/효도폰/영상시청/신규출시)
- 좌측: 통신망·통신사 체크박스, 데이터/가격 슬라이더, 초기화 버튼
- 메인: 카드 그리드(이름·통신사·망·가격·데이터/통화/문자·특전·출처뱃지),
  정렬 드롭다운 + 건수 + 페이지네이션(20개씩)
- 출처 뱃지: `target="_blank"` 새 탭 (포털 유지)
- 전체 로드(최대 500건) 후 클라이언트 사이드 필터/정렬

## 10. 네이티브 UI (ViewBinding, 최소)

- 홈: 서버 상태 카드(실행중/중지), 주소 표시+복사+QR(선택), 통계 3개(요금제수/활성소스/마지막수집), "지금 수집하기"
- 소스관리: RecyclerView 목록(이름·타입·주기·상태색상·토글·즉시실행)
- 설정: 포트 입력+적용, 보관기간 라디오(30/90), 자동시작 스위치,
  배터리예외 상태+요청 버튼, Watchdog 주기, 오래된 데이터 정리 버튼
- 디버그 로그 화면(홈 탭 또는 설정 내): 최근 crawl_logs 표시 — DoD의 DebugLogger 요건 충족

## 11. 비기능 요구사항

- 배터리: FGS + dataSync 타입 + 배터리예외 권고 + WorkManager 제약
- 메모리: Ktor CIO + Room Paging → 100MB 이하 목표
- 안정성: 크롤러별 타임아웃 30초, SupervisorJob, 트랜잭션 저장
- 보안: 로컬망 바인딩(0.0.0.0:3000), 인증 없음(동일망 신뢰), usesCleartextTraffic 허용
- 예의: robots.txt 확인, 6시간 이상 간격(비교사이트), 12시간(공식), 과요청 금지
- 로그: `[INFO] [FEATURE] <기능명>` 진입 로그, 실패 경로 `[ERROR] E-AND-...` + 메시지 매핑

## 12. 에러코드 (상세: error_message_ko.json)

- 형식 `E-AND-{CATEGORY}-{NUM4}`, CATEGORY: NET/DB/CRAWL/SRV/UI/VALID/STOR/PERM
- 예: E-AND-SRV-0101(FGS 시작 거부), E-AND-CRAWL-0201(파싱 실패),
  E-AND-NET-0301(서버 기동 실패), E-AND-DB-0401(마이그레이션 실패)

## 13. 마일스톤 (예상 5~7일, 1인)

1. M0 기반: Gradle·Manifest·패키지 구조·시드 (0.5일)
2. M1 DB+서버: Room + Ktor 정적서빙 + FGS + BootReceiver + Watchdog (1일)
3. M2 크롤러1: 알뜰폰허브 → /api/plans 노출 (1일)
4. M3 포털: 필터+카드그리드+뱃지 (1일)
5. M4 네이티브: 홈·소스·설정(배터리예외 포함) (1일)
6. M5 크롤러2~4: 모요+중복병합, KT엠모바일, LG헬로, brand.do (1.5일)
7. M6 스케줄+테스트+문서: WorkManager, 단위/UI 테스트, 빌드 검증 (1일)

## 14. 리스크

| 리스크 | 대응 |
|---|---|
| 사이트 구조 변경 | selectorConfigJson 외부화 + 크롤러 격리 |
| Android 16 dataSync 시간제한 | Watchdog 재시작 |
| 크롤링 차단 | 간격 1초+, UA 명시, ETag 활용 |
| FGS 백그라운드 시작 제한 | startService 우선 + FGS 폴백, 예외 catch |

## 15. DoD

- `./gradlew assembleDebug` 성공, lint Error 0
- 단위 테스트 통과(리포지토리·병합), Espresso 3화면 통과
- 실기: 앱 실행→알림→`http://S22_IP:3000/` 포털 로드, 4개 소스 수집+뱃지 확인
- 설정: 포트변경→재시작, 보관기간 반영, 배터리예외 상태 표시
- 문서: README·CHANGELOG·ENDPOINTS·PERMISSIONS·error_message_ko.json 갱신
- TODO+세션 로그 마감
