# PLAN_v1.3.1_android — 포털 개선 (모바일 최적화 + 통계 내비 + 검색)

> 작성: 2026-09-06 · 상태: 승인 완료(Build 모드) · 대상: 웹 포털 assets/web
> 범위 제한: **서버·DB·네이티브 변경 없음** — 검색은 기존 `state.allPlans` 클라이언트 필터 재사용

## 1. 한 줄 설명
통계 대시보드의 S22 모바일 깨짐을 수정하고, 상단 고정 탭(요금제 목록|통계) 전환과
통계 섹션 목차 내비게이션, 키워드·브랜드 검색을 추가한다.

## 2. 확정 사항 (사용자 결정)
- 검색 UX: **키워드 실시간 필터(디바운스 200ms) + 브랜드 드롭다운(전체 브랜드 + 26개)**
- 상단 고정 내비: **화면 전환식 탭** (요금제 목록 ↔ 통계 — 하나만 노출)
- 모바일 최적화 범위: **통계 대시보드 중심**
- 통계 목차: 섹션별 앵커 스크롤 이동 (요약/인사이트/브랜드/통신망/추이/분포/가성비/건강도)
- 브랜드 칩 클릭 → 브랜드 검색 적용 후 목록 탭으로 이동 (드릴다운 연계)

## 3. 모바일 깨짐 원인 (조사 결과)
- 추이 차트 SVG `min-width:420px`가 `.trend-charts`(grid `1fr`)의 `min-width:auto`를
  밀어내며 가로 초과 → S22(360px)에서 레이아웃 붕괴
- `.main`/`.stats-section`에 `min-width:0` 보장 없음 → 초과가 뷰포트까지 전파
- 헤더 `header-info` 5개 요소가 2줄 래핑 (정돈 필요)

## 4. 변경 파일
- `app/src/main/assets/web/index.html` — 탭 내비, 검색바, 통계 목차/앵커 위치
- `app/src/main/assets/web/style.css` — 그리드 min-width:0 체인, 탭/목차/검색 스타일, 모바일 규칙
- `app/src/main/assets/web/app.js` — `setTab()` 전환, 목차 렌더+점프, keyword/brand 필터, 브랜드 드롭다운 생성

## 5. 구현 세부
### index.html
- 헤더: `header-info`에 탭 그룹 `<nav class="tab-nav" role="tablist">` 삽입
  - `button#tabList` "요금제 목록" / `button#tabStats` "📊 통계" (기존 `statsButton` 제거)
- 목록 섹션: `grid-header`에 `<div class="search-bar">` (keyword input + brand select)
- 통계 섹션: `stats-head` 아래 `<nav id="statsNav" class="stats-nav">` 컨테이너
- loadData 후 브랜드 드롭다운을 JS로 채움

### style.css
- `.main`, `.stats-section`, `.chart-card`, `.net-card`, `.dist-chart` → `min-width:0`
- `.trend-charts`, `.dist-grid` → `grid-template-columns: repeat(auto-fit, minmax(0,1fr))`
- `.tab-nav` 탭/액티브 스타일, `.search-bar`(모바일 1열), `.stats-nav` sticky 칩,
- `≤600px`: 키워드 풀폭, 탭 44px, 차트 min-width 해제, lastUpdated 태블릿 이상 노출

### app.js
- `state.tab='list'`, `state.keyword=''`, `state.brand=''` 추가
- `setTab(tab)`: hidden 토글 + aria + 액티브 클래스, 탭 진입 시 lazyload(통계 첫 진입만 loadStats)
- `renderStats()`: 각 stats-block에 `id="sec-*"`(렌더 내용이므로 renderStats에서 부여),
  `renderStatsNav()`로 목차 칩 생성, click → `scrollIntoView`
- 브랜드 바(`brandBars`)와 인사이트·통신망 카드에 클릭 시 `goBrand(name)` →
  `state.brand=name` + `setTab('list')` (인사이트/통신망은 해당 없음 — 브랜드 차트만)
- `applyFilters()`: keyword(planName·carrierName·dataAmount·voice·sms·eventBadge 소문자 부분일치),
  brand(carrierName 일치) 추가
- `resetFilters`: keyword/brand 초기화 + select 초기화
- 브랜드 드롭다운 `buildBrandOptions()`: allPlans 고유 carrierName 정렬

## 6. 검증
- gradle `assembleDebug` 성공 (assets 포함), lint 무관(웹 불변)
- S22 설치 후 모바일 실기: 탭 전환, 목차 점프, 키워드("100분")·브랜드("KT엠모바일") 검색,
  가로 오버플로 없음(스크린샷 + 접근성 트리 + JS eval)
- 데스크톱 폭: 그리드/탭/검색 회귀 확인 (agent-browser headless)
- 실기 확인 전 smoke/단위 테스트는 변경 파일이 웹뿐이므로 건너뜀

## 7. 문서 갱신
- docs/TODO.md v1.3.1 신규 4건, docs/plans/PLAN_v1.3.1_android.md
- docs/CHANGELOG.md v1.3.1, docs/DESIGN.md 포털 구조 반영
- .agent/session-2026-09-06-android-v131.md

## 8. 에러코드
- 신규 없음 (프론트 UX 변경, E-AND-SRV-* 기존 재사용)