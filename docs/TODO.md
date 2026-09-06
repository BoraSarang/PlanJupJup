# TODO — PlanJupJup v1.0 (Android)

> 규칙: 1커밋 1관심사, 완료 시 체크 + 세션 로그 갱신. bd 미사용(저장소 없음) — 본 파일이 단일 진실.

## v1.4 GitHub 공개 (2026-09-06, PLAN_v1.4_web_github)

- [x] G-101 README 개편(배지·기능·구조·링크)
- [x] G-102 랜딩 페이지 landing/ 제작 + 실기 스크린샷 캡처
- [x] G-103 CI 워크플로우(빌드·테스트·lint) — CI v4 플래키 테스트 수정 후 green
- [x] G-104 Release 워크플로우(tag v* → APK Release) + 조건부 사이닝 — v1.0.0 릴리즈 생성
- [x] G-105 Pages 워크플로우 + Pages Source 설정(gh api) + 배포 확인(HTTP 200)
- [x] G-106 원격 push(main) + CHANGELOG·세션 로그 갱신

## v1.3.1 포털 개선 (2026-09-06, PLAN_v1.3.1_android)

- [x] P-101 모바일 깨짐 수정: 그리드 min-width:0 체인 + 차트/그리드 auto-fit(minmax(0,1fr)) + 모바일 규칙
- [x] P-102 상단 고정 탭 내비(요금제 목록|통계) 화면 전환식 + 통계 목차 앵커 스크롤
- [x] P-103 키워드 실시간(디바운스)·브랜드 드롭다운 검색 + applyFilters 연계 + reset 초기화
- [x] P-104 재빌드/설치 + S22 모바일·데스크톱 E2E + CHANGELOG/DESIGN/TODO/세션 로그 갱신

## v1.3.0 통계·인사이트 대시보드 (2026-09-06, PLAN_v1.3_android)

- [x] S-001 PlanMetrics 파싱 유틸 (DataAmount: GB/무제한/QoS, Voice분, Sms건) + 단위 테스트
- [x] S-002 DAO 집계 쿼리 (브랜드/통신망/추이) + StatsRepository(집계·가성비·인사이트·5분캐시)
- [x] S-003 Ktor /api/stats/* 8종 라우트 + JSON 직렬화 + error_code 3종
- [x] S-004 포털 통계 섹션 (버튼/패널, SVG 차트 5종, 가성비 TOP, 인사이트 카드)
- [x] S-005 빌드·단위·lint·S22 E2E + ENDPOINTS/DESIGN/CHANGELOG/error_message_ko.json 갱신

## v1.2.0 알림 센터 (2026-09-06)

- [x] N-001 알림 DB (v4 MIGRATION_3_4) + NotificationLog 엔티티/DAO/Repository
- [x] N-002 알림 생성 서비스: 수집완료/신규/5연속실패/일일요약(detailJson buildJsonObject 수동 직렬화)
- [x] N-003 알림 API 7종 (목록/상세/읽음/전체읽음/삭제/보관정리/미읽기)
- [x] N-004 포털 알림 UI (벨+뱃지/패널/탭/페이징/상세 모달)
- [x] N-005 앱 알림 탭 (하단 4번째, 목록/전체읽음/상세 다이얼로그/삭제)
- [x] N-006 설정 알림 토글 3종 + /api/settings 확장 + 생성 게이팅
- [x] N-007 일일 요약 워커 (09:00 PeriodicWork, E-AND-NOTIF-0701)
- [x] N-008 신규 요금제 알림 버그 수정 (SaveResult.createdIds 조회) + 수집완료 통계 보정
- [x] N-009 빌드/단위 23/23/lint/S22 E2E + CHANGELOG/ENDPOINTS/error_message_ko.json 갱신

## M0 기반

- [x] T-001 프로젝트 스캐폴딩: settings.gradle.kts, root/app build.gradle.kts, gradle.properties, 패키지 디렉토리
- [x] T-002 AndroidManifest.xml: 권한 9종 + HttpServerService(dataSync) + BootReceiver + MainActivity
- [x] T-003 리소스: strings(app_name_launcher=요금줍줍, app_name_full=알뜰요금줍줍), themes(Material3), colors, item 레이아웃
- [x] T-004 Application 클래스 + Timber + DataStore PreferencesManager + InitialDataSeeder(소스 5개)

## M1 DB + 서버 + 생존성

- [x] T-010 Room Entity 5종(Plan/PlanSourceMapping/CrawlSource/CarrierBrand/CrawlLog) + Converters
- [x] T-011 DAO 5종 + PlanDatabase + Repository(Plan/Source/Settings)
- [x] T-012 Ktor CIO 서버: 라우팅 9개 + 정적 리소스 + CORS + StatusPages + CallLogging
- [x] T-013 HttpServerService: START_STICKY + startForeground + onTaskRemoved + start()/stop() 헬퍼
- [x] T-014 BootReceiver + Watchdog 코루틴(헬스체크·자동재시작)

## M2 크롤러

- [x] T-020 BaseCrawler + CrawlerSelectorConfig + CrawlerFactory + PlanCrawler 인터페이스
- [x] T-021 MvnohubCrawler(목록+페이지네이션+상세 선택적) + 픽스처
- [x] T-022 MoyoCrawler + 중복 병합 검증(뱃지 2개)
- [x] T-023 KtmMobileCrawler(KT엠모바일 공식)
- [x] T-024 LiivMCrawler(LG헬로모바일 공식)
- [x] T-025 BrandListCrawler(brand.do → carrier_brands)

## M3 웹 포털

- [x] T-030 assets/web: index.html(시맨틱+접근성) + style.css(모바일퍼스트)
- [x] T-031 app.js + PlanPortal(로드·필터·정렬·페이지네이션·큐레이션태그·출처뱃지 새탭)

## M4 네이티브 UI

- [x] T-040 MainActivity + BottomNavigation + NavGraph(홈/소스/설정)
- [x] T-041 HomeFragment + HomeViewModel(상태·주소복사·통계·수동수집)
- [x] T-042 SourceManageFragment + RecyclerView 어댑터(토글·즉시실행)
- [x] T-043 SettingsFragment(포트·보관기간30/90·자동시작·Watchdog주기·정리버튼)
- [x] T-044 배터리예외 UI(상태표시+요청인텐트+ON_RESUME 갱신) + DebugLog 화면(crawl_logs)

## M5 스케줄 + 테스트 + 마감

- [x] T-050 CrawlWorker(CoroutineWorker+setForegroundAsync+트랜잭션) + CrawlScheduler(개별주기·즉시·취소)
- [x] T-051 단위 테스트: Repository(병합·페이징·정리), Crawler(픽스처 파싱)
- [x] T-052 UI 테스트: 홈·소스·설정 Espresso
- [x] T-053 빌드 검증(assembleDebug·lint) + README·CHANGELOG·ENDPOINTS·PERMISSIONS 마감

## 완료 기준 (DoD 요약)

- [ ] lint Error 0, 단위/UI 테스트 통과
- [ ] 실기: 알림→포털 로드→4개 소스 수집→뱃지 2개→필터/정렬 동작
- [ ] 설정 반영(포트·보관기간·배터리예외) 확인 후 세션 로그 작성
