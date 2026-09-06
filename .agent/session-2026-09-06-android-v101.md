# 세션 로그 — 2026-09-06 (android) v1.0.1

1. 무엇을: 3건 수정 (상태바 가림+홈 개선 / 최초·최근 수집시각 / 포털 100개 상한)
2. 플랫폼: Android 네이티브(ViewBinding), S22 SM-S901N 실기 검증
3. 빌드+PERF+CACHE: assembleDebug 성공 / 단위 18/18·Espresso 2/2·lint Error 0 / 포털 전체 432건 로드 확인
4. 남은TODO: LG헬로모바일 실기 검증 후 활성화 / 통신사 별칭 정규화 / lint 경고 정리
5. 전달로그: v1→v2 업그레이드 432건 보존·백필 성공, 모요 재수집 후 first 유지·recent 전진 확인
6. 문서갱신: CHANGELOG v1.0.1, ENDPOINTS(firstCollectedAt), error_message_ko.json(UI-0701/DB-0403)
7. 큐상태: bd 미사용, docs/TODO.md 단일 진실(전항 체크 유지)
8. E2E: /api/plans?pageSize=1000 → 432/432, 포털 뱃지 "전체 N개", 카드 수집시각 줄 표시
