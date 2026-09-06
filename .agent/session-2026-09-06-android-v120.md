# 세션 로그 — 2026-09-06 (android) v1.2.0

1. 무엇을: 알림 센터 신설 (DB v4 + 수집완료/신규/5연속실패/일일요약 알림 + 포털 패널 +
   앱 알림 탭 + 설정 토글 3종 + 알림 API 7종 + 일일 09:00 요약 워커)
2. 플랫폼: Android 네이티브(ViewBinding) + 포털(바닐라 JS), S22 SM-S901N 실기 검증
3. 빌드+PERF+CACHE: assembleDebug/Test 성공, 단위 23/23(일일요약 예약 계산 추가), lint Error 0 /
   알림 저장량 미미, 캐시 영향 없음
4. 남은TODO: 신규 요금제 발생 시 NEW_PLANS_FOUND 상세 UI 실기 확인(현재 신규 0건),
   LG헬로모바일 실기 후 활성화, 통신사 별칭 정규화
5. 전달로그: ① 코틀린 직렬 컴파일러 미적용으로 `NotificationDetail` 직렬화 크래시 →
    buildJsonObject 수동 직렬화(detailToJson)로 해결, ② 크롤러 드래프트 isNew=false 고정으로
    신규 알림 미발생 → SaveResult.createdIds 반환 후 실제 생성 요금제 조회로 수정,
    ③ Room 마이그레이션 INTEGER PRIMARY KEY는 notNull=false로 판정 → NOT NULL 명시 필요
6. 문서갱신: CHANGELOG v1.2.0, ENDPOINTS(알림 7종), DESIGN(알림 센터/설정 토글),
   error_message_ko.json(E-AND-NOTIF-0701~0703), TODO N-001~009
7. 큐상태: bd 미사용, docs/TODO.md 단일 진실
8. E2E: 수동 수집→CRAWL_COMPLETE 생성, 토글 OFF 시 미생성·ON 재개(총9→9→11),
   상세 통계 일치(전체36/신규0/갱신36), 포털 패널·상세 모달, 앱 알림 탭 상세 다이얼로그 확인