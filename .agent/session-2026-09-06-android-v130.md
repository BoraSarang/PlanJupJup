# 세션 로그 — 2026-09-06 (android) v1.3.0

1. 무엇을: 통계·인사이트 대시보드 신설 (StatsRepository 집계+5분 메모리 캐시, /api/stats/* 8종,
   가성비 랭킹+자동 인사이트, 포털 📊 통계 섹션 5종 SVG 차트, PlanMetrics 파싱 보강)
2. 플랫폼: Android 네이티브(서버) + 포털(바닐라 JS+인라인 SVG, Chart.js 미도입), S22 SM-S901N 실기 검증
3. 빌드+PERF+CACHE: assembleDebug/Test 성공, 단위 49/49(PlanMetricsTest 17 추가), lint Error 0 /
   스탯 캐시 TTL 5분 메모리 + saveCrawlResults/cleanup 시 invalidate, 멀티소싱 없음, 오버헤드 미미
4. 남은TODO: unlimitedRatio 0.0 관찰 — 실데이터 중 "무제한" 문자열이 없는 수집 건 존재
   (PlanMetrics 파싱 재확인 필요, 신규요금제 실기 기회에 함께 점검), LG헬로모바일 실기 후 활성화
5. 전달로그: ① PRICE_BUCKETS 첫 버킷 `0 to null` → `0 to 10000`으로 수정(수치 비교 시
   NumberFormatException 방지), ② "100GB+3Mbps"는 무제한이 아님 — dataGb=100 사용 확정,
   "무제한" 문자열 있을 때만 isUnlimited=true, ③ 인사이트 한글 조사는 josa()로
   (일반 텍스트 외 ")" 등 비한글은 "가" 처리), ④ 주말 0시 collect 캐시 TTL 5분이라
   trends hourly는 days 최대 2일 제한
6. 문서갱신: CHANGELOG v1.3.0, ENDPOINTS(스탯 8종), DESIGN(포털 통계 레이아웃/시각),
   error_message_ko.json(E-AND-SRV-0105~0107), TODO S-001~005 완료
7. 큐상태: bd 미사용, docs/TODO.md 단일 진실
8. E2E: S22 설치 후 curl 8종 전부 검증(overview totalPlans=432/brandCount=26/networkCount=3,
   value-ranking 1위 A모바일 SKT 81000점, trends daily·hourly, insights 조사 정상),
   포털 통계 버튼 → 대시보드 렌더(KPI 8/인사이트 5/랭킹 10/통신망 3, SVG 5),
   시간별 gran 토글 재렌더, 가성비 TOP 10 망 필터 정상, JS 콘솔 에러 0