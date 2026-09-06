# 세션 로그 — 2026-09-06 (android) v1.0.2

1. 무엇을: 홈 3건 수정 (서버 중지됨 오표시 / IP 확인중 / 포털 버튼 없음 / 탭 아이콘 없음)
2. 플랫폼: Android 네이티브(ViewBinding), S22 SM-S901N 실기 검증
3. 빌드+PERF+CACHE: assembleDebug 성공 / 단위 18/18·Espresso 2/2·lint Error 0 / 변경 없음
4. 남은TODO: LG헬로모바일 실기 검증 후 활성화 / 통신사 별칭 정규화 / lint 경고 정리
5. 전달로그: 오표시 원인=메인스레드 소켓(NetworkOnMainThreadException) → IO 이동 해결.
   IP 이중 탐색으로 10.205.96.43 표시, 브라우저 인텐트 발사 실기 확인
6. 문서갱신: CHANGELOG v1.0.2, Espresso 포털 버튼 검증 추가
7. 큐상태: bd 미사용, docs/TODO.md 단일 진실
8. E2E: 재설치 후 서버 기동·주기수집 자동 재개 확인(36건 수집 중)
