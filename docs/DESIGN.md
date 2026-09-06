# DESIGN.md — PlanJupJup UI/UX 설계

> 네이티브: View + ViewBinding + Material3 Components (Compose 미사용).
> 원칙: 네이티브 화면은 최소(상태·설정·로그), 주 사용 화면은 웹 포털.

## 1. 네이티브 앱 정보 구조

```
MainActivity (BottomNavigation)
├─ 홈(HomeFragment)
│   ├─ 서버 상태 카드: [아이콘] 실행중/중지됨 + 주소 + [복사] [QR]
│   ├─ 통계 3열: 수집 요금제 수 / 활성 소스 수 / 마지막 수집(상대시간)
│   └─ [지금 수집하기] 버튼 (실행 중 비활성화 + "수집 중..." 표시)
├─ 소스(SourceManageFragment)
│   └─ RecyclerView 행: 이름 / 타입·주기 / 최근로그(마지막 수집·발견/신규/갱신) / 상태(색상) /
│       토글 / 주기 스피너(30분~주1회) / 즉시실행
│       상태색: SUCCESS=초록, FAILED=빨강, RUNNING=주황, NEVER_RUN=회색
└─ 설정(SettingsFragment)
    ├─ 서버: 포트 입력 + [적용] + 상태행 + 자동시작 스위치
    ├─ 배터리 최적화 예외: 상태행(허용됨=primary/미허용=error)
    │   + 미허용 시 설명문 + [배터리 무제한 허용 요청] 버튼
    │   + 허용 시 [배터리 무제한 해제] 버튼 + 해제 안내문
    ├─ 데이터: 보관기간 라디오(30일/90일) + [오래된 데이터 정리] 버튼
    ├─ 알림: 수집 완료 / 신규 요금제 발견 / 수집 실패 3종 스위치 (기본 ON)
    ├─ Watchdog: 헬스체크 주기 입력(15~3600초) + [적용]
    └─ 디버그 로그: 최근 crawl_logs 50건 리스트 + 앱 정보(버전·제작자·문의)
```

## 1-2. 알림 센터

```
─ 알림(NotificationFragment) — 하단 4번째 탭
│   헤더: "알림 센터" + 미읽기 수(모두 읽음) + [전체 읽음]
│   목록: 요약(2줄 이내) + 상대시간("방금 전") + 삭제(✕), 미읽음 볼드·읽음 일반
│   항목 탭 → 상세 다이얼로그: 제목(타입) + 요약 + 통계
│     (전체 발견/신규/갱신/실패) + 출처별 목록
└─ 웹 포털 알림 센터 (aside 패널)
    헤더: "알림 센터" + [전체 읽음] [보관일 삭제] [닫기]
    탭: 전체 / 신규 요금제 / 수집 완료 / 수집 실패
    목록: 요약 + 시각 + 삭제(🗑), 미읽음 파란 점
    항목 클릭 → 모달: 통계 카드 4개 + 출처별/브랜드별/통신망별 바차트
    + 신규 요금제 목록(링크) + 실패 소스 목록
```

## 2. 네이티브 시각 규칙 (Material3)

- 테마: `Theme.Material3.DayNight.NoActionBar` 기반, 카드 radius 12dp, 패딩 16dp
- 상태 카드 배경: 실행중=`colorPrimaryContainer`, 중지=`colorErrorContainer`
- boolean 네이밍: isServerRunning, isCrawling, batteryUnrestricted
- 함수 동사 prefix: loadStats(), triggerManualCrawl(), copyAddress(), requestBatteryExemption()
- 진입 로그: `[INFO] [FEATURE] 홈`, `[INFO] [FEATURE] 수동수집` 등 화면·액션마다 1개 이상

## 3. 웹 포털 레이아웃 (assets/web)

```
header (sticky)
├─ 로고 "📱 알뜰요금줍줍"(클릭 시 전체 요금제로 이동 = 필터 초기화) + 마지막업데이트 + [전체 N개 뱃지=수집 요청(confirm)] + [🔔 알림]
└─ 탭 내비(전환식): [큐레이션 태그 칩(왼쪽, 가로 스크롤)] [📊 통계(오른쪽 끝)] — 하나만 표시, 전환 시 scrollTop 0
main (기본 1열, 필터 열림 시 데스크탑만 280px 1열)
├─ aside 필터 (기본 접힘): ☰ 필터 버튼으로 펼치기, ✕·배경·Esc로 닫기
│   ※ 폰·패드(≤900px): 왼쪽 드로어 + 배경 딤
├─ aside 필터: 통신망 체크 / 통신사 체크 / 데이터 슬라이더 / 가격 슬라이더 / 초기화
├─ section#statsSection (통계 탭에서만 표시)
│   ├─ 통계 목차 칩 (sticky 아님, 최상단): 요약/인사이트/브랜드/통신망/추이/분포/가성비/건강도
│   │   → 클릭 시 해당 섹션으로 smooth scroll (sticky 헤더 높이만큼 오프셋)
│   ├─ KPI 카드 grid #sec-kpi (from 2026-09-06: 3~4열 시맨틱, 360px에서 2열)
│   ├─ 인사이트 카드 5종 #sec-insights (type별 accent: positive=초록/info=파랑/warning=주황)
│   ├─ 저장·브랜드별 커버리지 #sec-brands → SVG 바 차트, 막대 클릭=해당 브랜드 검색 후 목록 이동
│   ├─ 통신망 비교 3사 카드 #sec-networks (평균가·단가·5G 비중) + 히트 게이지
│   ├─ 가격·데이터 분포 #sec-dist (가격=SVG 바, 데이터=SVG 도넛, 시맨틱 table role)
│   ├─ 가성비 TOP 10 #sec-rank (망 필터 select: 전체/5G/LTE, 단위 스코어 표시)
│   └─ 수집·신규 추이 #sec-trends SVG 라인 (일별/시간별 토글 — 클릭 시 재조회·리렌더)
├─ section#planGridSection (요금제 탭에서만 표시)
    ├─ 정렬 드롭다운 + 검색바(키워드 실시간 + 브랜드 드롭다운) + 표시건수
    ├─ 카드 그리드 (auto-fill minmax 280px, 모바일 1열)
    └─ 페이지네이션 (20개씩, … 생략 표기)
통계 색상: primary #0066FF / warning #E65100 / success #34A853 / 3사 red·violet·blue
카드: 통신사뱃지+망뱃지+NEW / 요금제명 / 가격(+정가취소선)
      / 데이터·통화·문자 3행 / 특전(있을 때만) / 첫수집·최근 1행 / 출처뱃지(새탭 링크)
폰(≤600px) 1열·패드(601~900px) 2열, 버튼·칩 최소 44px 터치 영역
모바일(≤600px): lastUpdated 숨김, 검색바 풀폭, 탭/목차/그래뉼러리티 44px, 차트 min-width 해제
```

## 4. 웹 시각 규칙

- CSS 변수(--primary #0066FF 등), 카드 hover 상승 2px, 뱃지 pill 형태
- 접근성: role=list/listitem, tablist, aria-label, 키보드 포커스 가능 버튼
- 빈 상태: "조건에 맞는 요금제가 없습니다" + 초기화 유도
- 에러 상태: "데이터를 불러오는데 실패했습니다" (서버 미기동 시)
- 통계 대시보드: 📊 통계 버튼 → 패널 토글, KPI 3~4열 시맨틱 카드,
  SVG 차트는 바닐라 JS 렌더러(renderBars/renderMultiLines/renderDonut),
  막대/영역/도넛 애니메이션은 `prefers-reduced-motion`시 CSS 전환만 유지
  (모션·미적용 시 즉시 렌더)

## 5. 화면 플로우

1. 앱 실행 → autoStart면 서버 자동 시작 → 알림 표시 → 홈에 주소 노출
2. 브라우저에서 주소 접속 → 포털 로드 → 필터/정렬 탐색 → 출처뱃지 클릭(새탭)
3. 신규 요금제 확인 → 설정에서 수동 수집 또는 주기 수집 대기
4. 배터리예외 미허용 경고 확인 → 요청 버튼 → 시스템 설정에서 허용 → 복귀 시 상태 갱신
