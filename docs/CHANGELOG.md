# CHANGELOG — PlanJupJup

> 형식: `[버전] 날짜 (platform: android)` + error_code + perf/cache 영향 기록

## [v1.3.1] 2026-09-06 (platform: android)

- 포털 모바일 최적화: 통계 대시보드 가로 초과(S22 360px) 수정 — `.main`/`.stats-section`/
  `.chart-card`에 `min-width:0`, 트렌드/분포 그리드를 `auto-fit minmax(0,1fr)`로 전환
- 상단 고정 탭 내비 신설: **요금제 목록 | 📊 통계** 화면 전환식 (role=tab/aria-selected,
  액티브 하이라이트, 전환 시 scrollTop 리셋) — 기존 📊 통계 토글 버튼 대체
- 통계 목차 내비게이션: 요약/인사이트/브랜드/통신망/추이/분포/가성비/건강도 앵커 smooth scroll
  (renderStats 재렌더 시 목차도 갱신)
- 브랜드 드릴다운: 저장 커버리지 막대 클릭 → 해당 브랜드 검색 적용 후 요금제 목록 탭으로 이동
- 검색 기능: 키워드 실시간 필터(디바운스 200ms, planName·carrierName·dataAmount·voice·sms·
  eventBadge 부분일치) + 브랜드 드롭다운(전체 브랜드 + 26개, allPlans에서 추출·가나다 정렬)
  — 서버 변경 없이 클라이언트 필터 재사용
- 필터 초기화에 검색어·브랜드 포함
- 검증: assembleDebug 성공, S22 실기(360px: 가로 overflow 0, 탭 전환, 목차 점프,
  키워드 "100분"→24건, 브랜드 "KCT (티플러스)"→2건, 통계 KPI 8/SVG 5), 데스크톱 1280px 회귀 정상
- perf/cache: 영향 없음 (클라이언트 필터만 추가)
- [후속 피드백 반영] 상단 메뉴 재구성: "요금제 전체" 탭 제거 → 로고(📱 알뜰요금줍줍) 클릭 시
  전체 요금제로 이동(필터 초기화 포함), 큐레이션 태그는 왼쪽·📊 통계 탭은 오른쪽 끝 배치
- [후속 피드백 반영] 통계 보는 중 큐레이션 태그 클릭 시 요금제 목록 탭으로 자동 전환 후 필터 적용
- [후속 피드백 반영] "지금 수집하기" 버튼 제거 → "전체 N개" 배지에 수집 요청 연결
  (confirm 컨펌 → POST /api/sync → "예약됨 (새로고침 해주세요)")
- [후속 피드백 반영] 브랜드/키워드 검색 셀렉트 스타일을 가격순 정렬 셀렉트와 동일하게 통일
- [후속 피드백 반영] 데이터 용량 도넛 범례를 그래프 오른쪽 고정(모바일에서 아래로 내려가던 문제 수정)
- [후속 피드백 반영] 통계 목차 점프 시 sticky 헤더 높이만큼 오프셋 적용(지정 위치보다 내려가던 문제 수정)
- [후속 피드백 반영] 마지막 업데이트를 상대시간 표시(방금 전/N분 전/N시간 전/N일 전, 정확 시각은 툴팁)
- [후속 피드백 반영] 키워드 검색 고도화: **초성 검색**(자모 ㄱ-ㅎ 입력 시 요금제명·브랜드 초성 매칭,
  영문 브랜드 KT/SK/LG는 자모 변환으로 "ㅋㅌ/ㅅㅋ/ㅇㅈㄱ" 지원) + **자연어 검색**
  (예: "5만원 이하"/"10기가"/"300분"/"200건" — 숫자·단위·방향어 파싱으로 가격·데이터·통화·문자
  상하한 필터 적용, 무제한 처리). 검색 placeholder에 사용 예시 명시
- [후속 피드백 반영] 필터 오픈 시 모바일 그리드 깨짐 수정 — 파일 하단에 중복된 고아 규칙
  `body.filter-open .main{280px 1fr}`(media 외부)가 모바일 1열 오버라이드를 덮어쓰던 문제 제거
- [후속 피드백 반영] 통계 그래프 높이 통일 — 발견·신규·실패 범례를 h4 한 줄로 이동,
  `.chart-card`를 flex column으로, `.chart-scroll`을 `flex:1` 스테이지(차트가 카드 높이를 채움),
  도넛 카드도 같은 높이로 정렬 (desktop 1280에서 4개 카드 모두 250px 동일)

## [v1.3.0] 2026-09-06 (platform: android)

- 통계·인사이트 대시보드 신설 (DB 스키마 변경 없음 — 기존 plans/crawl_logs 집계)
- `StatsRepository`: overview/브랜드/통신망/가격·데이터 분포/수집·신규 추이(daily·hourly)/
  가성비 랭킹/수집 건강도/자동 인사이트 + 5분 TTL 메모리 캐시 (CACHE_TTL_MS = 5*60*1000L)
- `/api/stats/*` 8종 라우트 추가 (기존 `/api/stats` 홈 3열은 호환 유지)
- 가성비 스코어: (dataGb + voiceMin×0.3 + smsCount×0.1) ÷ (price/1000)
  (price≤0·benefit≤0·파싱 실패 제외, 무제한=100GB·2000분·2000건 가정치)
- 데이터 파싱 보강: `PlanMetrics` (GB/무제한/QoS, voiceMin, smsCount) —
  "100GB+3Mbps"는 무제한 아님(100GB 사용), "무제한"만 무제한 판정
- 인사이트: 브랜드 1위(보유/KT망) / 네트워크 구성(5G 비중) / 평균 가격대 /
  수집 실패 감지(warning) — 한글 조사(josa "이/가") 자동 처리
- 수집 캐시 연동: saveCrawlResults/cleanup 시 statsRepository.invalidate()
- 포털 통계 섹션: 📊 통계 버튼 + KPI 카드/인사이트/브랜드-통신망/분포(SVG 바)·데이터(도넛)/
  가성비 TOP 10(망 필터)/수집·신규 추이(일별·시간별 토글) — 바닐라 JS+인라인 SVG (
  Chart.js 미도입)
- 에러코드: E-AND-SRV-0105(통계 로드), E-AND-SRV-0106(가성비 랭킹), E-AND-SRV-0107(인사이트)
- 검증: 단위 49/49 (PlanMetricsTest 17) + lint Error 0 + assembleDebug, S22 실기 curl
  8종 + 포털 통계 대시보드 agent-browser 확인(시간별 gran 토글·망 필터 동작)
- perf/cache: 스탯 캐시 TTL 5분 메모리+invalidate, 멀티소싱 없음, 오버헤드 미미

## [v1.2.0] 2026-09-06 (platform: android)

- 알림 센터 신설 (DB v4): notification_logs 테이블 + MIGRATION_3_4
  (id `NOT NULL PRIMARY KEY AUTOINCREMENT` 명시 — Kotlin 포팅 Room 검증 이슈 수정)
- 수집 완료·신규 요금제·5연속 실패·일일 요약 알림을 DB에 기록, 웹 포털 + 앱 알림 탭에서 확인
- 포털 알림 UI: 벨+뱃지, 패널(전체 읽음/보관일 삭제), 탭(전체/신규/수집완료/실패),
  목록·페이징, 상세 모달(통계+출처별/브랜드별/통신망별 차트, 신규/실패 소스)
- 앱 알림 탭(하단 4번째): 목록 + 전체 읽음 + 상세 다이얼로그 + 삭제
- 알림 API: 목록/상세/읽음/전체읽음/삭제/보관정리/미읽기 수 (보관 기간 = 데이터 보관일)
- 설정 알림 토글 3종: 수집 완료 / 신규 요금제 / 실패 각각 ON·OFF (API `/api/settings` 확장)
- 일일 요약 워커: 매일 09:00 24시간 주기 PeriodicWork + 다음 9시까지 첫 실행 지연
  (E-AND-NOTIF-0701)
- 버그 수정: 코틀린 직렬 컴파일러 미적용으로 `Serializer not found` 크래시 →
  detailJson을 buildJsonObject 수동 직렬화로 교체
- 버그 수정: 크롤러 드래프트 `isNew=false` 고정으로 신규 요금제 알림 미발생 →
  `SaveResult.createdIds` 반환 후 실제 생성 요금제 조회, 수집완료 상세 통계를
  sourceResults 기준으로 보정
- 검증: 단위 23/23(201P: 일일요약 예약/startOfToday 추가), lint Error 0,
  S22 실기 E2E: 수동 수집→알림 생성, 토글 OFF 시 미생성·ON 재개, 포털 패널/상세
  모달, 앱 알림 탭 상세 다이얼로그 확인
- perf/cache: 알림 저장 추가(저장량 미미)·캐시 영향 없음

## [v1.1.0] 2026-09-06 (platform: android)

- 포털 상세검색 접기/펼치기: 기본 접힘 + ☰ 필터 버튼 + ✕/배경/Esc 닫기,
  모바일·패드에서는 왼쪽 드로어
- 포털 반응형: 폰 1열·패드 2열, 터치 영역 44px
- 소스 화면: 마지막 수집일 + 발견/신규/갱신 통계 표시 (최근 로그 기준)
- 수집 주기 설정: 소스별 30분/1시간/6시간/12시간/24시간/주1회 (분 단위, Room v3
  마이그레이션), 소스별 독립 PeriodicWork 병렬 수집
- 5연속 실패 시 알림 + E-AND-CRAWL-0204 (알림 권한 요청 포함)
- 검증: 단위 21/21, Espresso 2/2, lint Error 0, S22 실기(intervalMinutes 노출·수집 동작 확인)

## [v1.0.2] 2026-09-06 (platform: android)

- 홈 "서버 중지됨" 오표시 수정: 상태 판단 소켓 접속을 IO 스레드로 이동
  (메인 스레드 NetworkOnMainThreadException이 원인)
- IP 확인 불가 수정: Wi-Fi 정보 + 네트워크 인터페이스 열거 이중 탐색
  (모바일 데이터에서도 10.x 사설 IP 표시 확인)
- 홈에 "포털 보기" 버튼 추가 (주소 탭도 브라우저 열기 유지)
- 하단 탭 아이콘 3종 추가 (홈/RSS/설정 벡터)
- 홈 onResume마다 상태 새로고침
- 검증: 단위 18/18, Espresso 2/2(포털 버튼 노출 포함), lint Error 0,
  S22 실기에서 브라우저 인텐트 `http://10.205.96.43:3000` 발사 확인

## [Unreleased]

- 문서 우선: PLAN_v1.0_android, TODO, DESIGN, api/ENDPOINTS, PERMISSIONS, error_message_ko.json 작성

## [v1.0.1] 2026-09-06 (platform: android)

- 상태바 가림 수정: 4개 화면 루트 `fitsSystemWindows`, 홈 로고 행 추가
- 홈 접속 주소 탭 → 브라우저 열기 (복사 버튼 유지), E-AND-UI-0701 추가
- 요금제 최초/최근 수집 시각: `firstCollectedAt` + Room v1→v2 마이그레이션(백필),
  갱신 시 최초값 유지, 포털 카드에 "첫 수집·최근" 표시, DB 열기 실패 폴백(E-AND-DB-0403)
- 포털 "총 100개" 수정: API 상한 100→1000, 뱃지는 `/api/stats` 기준 "전체 N개"
- 검증: 단위 18/18, Espresso 2/2, lint Error 0, S22 실기(v1→v2 업그레이드 432건 보존,
  재수집 후 first 유지·recent 전진 확인)

## [v1.0.0] 2026-09-06 (platform: android)

- 최초 릴리스: Room 5테이블 + Ktor CIO(3000) + 포털(필터/정렬/태그/뱃지) +
  크롤러(허브·모요·KT엠공식·brand.do) + WorkManager + 3화면 + 배터리예외 +
  BootReceiver + Watchdog + START_STICKY
- 검증: 단위 18/18, Espresso 2/2(SM-S901N), lint Error 0, S22 실기 E2E 432건 수집
- 제외: LG헬로모바일 공식(liivm, 셀렉터 미검증으로 DISABLED 시드)
- perf: 수집 3소스 병렬 약 74초, 메모리 목표 100MB 이하(미측정)
