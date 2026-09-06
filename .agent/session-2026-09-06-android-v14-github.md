# 세션 로그 — 2026-09-06 Android (v1.4 GitHub 공개)

- **무엇을**: README 개편, 랜딩 페이지 + GitHub Pages, CI/Release/pages 워크플로우 3종, 기기 릴리즈 설치, 첫 원격 push
- **플랫폼**: android(앱) + web(랜딩) + github(CI/Pages/Release)
- **빌드**: assembleDebug·assembleRelease(unsigned·debug서명) 모두 성공 / lintDebug 통과 / 단위 테스트 49건 green(로컬 3회·UTC 1회)
- **검증**: CI·Pages green(gh run), 랜딩 https://borasarang.github.io/PlanJupJup/ HTTP 200 + 콘텐츠 확인,
  v1.0.0 Release 생성 + app-release-unsigned.apk(8.4MB) 첨부, S22에 릴리즈 1.0.0 설치 후 포털 200(데이터 유지)
- **수정**: CI 플래키 — millisUntilNextHour 내부 now ms 언더슈트로 ScheduleLogicTest 실패 → 1초 언더슈트/1분 오버슈트 허용
- **이슈**: step if: 의 secrets 컨텍스트 파싱 오류(env 경유로 수정), setup-java@v4 → v5, 릴리즈 본문 CHANGELOG 섹션 매칭 보강
- **남은 TODO**: 없음(G-101~106 완료) / 릴리즈 사이닝 시크릿 등록은 선택 사항
- **전달**: 서명 키 미등록 시 unsigned APK — 사용자에게 시크릿 설정 방법 안내 필요
- **문서**: PLAN_v1.4_web_github·CHANGELOG v1.4·TODO G-101~106 갱신 / queue 상태: 없음
- **E2E**: 랜딩 200·콘텐츠 일치 / 통계·검색 회귀는 장치 포털에서 유지 확인