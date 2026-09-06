# 1. 개요

- 목표: 오픈소스 공개용 저장소 포장 — README 개편, 랜딩 페이지(GitHub Pages),
  CI(빌드+테스트+lint), 태그 기반 GitHub Releases(APK 자동 빌드·업로드), 첫 원격 push.
- 저장소: https://github.com/BoraSarang/PlanJupJup (gh 인증 계정 BoraSarang, 저장소 존재 확인)
- 플랫폼: android(앱 빌드) + web(랜딩)/github(CI·Pages·Release)
- 파일 수정 없이 순수 추가: README.md·app/build.gradle.kts(사이닝 조건부)만 수정, 나머지 신규 파일.

# 2. 작업 분해

## 2.1 README 개편
- 배지: CI workflow status / Latest Release / GitHub Pages / 라이선스(없음 → 제작자 뱃지 대체)
- 소개 요약, 핵심 기능(6h 자동 수집, 웹 포털, 통계 대시보드, 자연어·초성 검색, 모바일 최적화)
- 시작하기(빌드·설치·서버 포트), 프로젝트 구조 트리, 수집 소스 표(기존 유지), 문서/링크, 라이선스·제작자.

## 2.2 랜딩 페이지 + GitHub Pages
- `landing/` 디렉토리: index.html + styles.css + assets(스크린샷) — 순수 정적, 의존성 0.
- 섹션: 헤더 / 히어로(제목·설명·CTA 다운로드+GitHub) / 핵심 기능 카드(수집·대시보드·검색·모바일)
  / 파이프라인 표(수집 소스) / 사용 방법(3단계) / 문서 링크 / 푸터(BoRaSaRang).
- 스크린샷: 실기(S22)에서 포털 목록·통계 데스크톱/모바일 캡처 → landing/assets/*.png 참조.
- 워크플로우 `.github/workflows/pages.yml`: configure-pages → upload-pages-artifact(lettoin/landing) → deploy-pages.
- Pages Source: Settings→Pages→GitHub Actions (gh api 로 자동 설정).

## 2.3 GitHub Actions
- `ci.yml`: push/PR(타겟 main) → checkout → setup-java(17) → gradle setup → testDebugUnitTest + lintDebug.
- `release.yml`: tag `v*` → assembleRelease(시크릿 있으면 사이닝, 없으면 unsigned 경고) →
  CHANGELOG에서 해당 태그 섹션 추출 → GitHub Release(APK 첨부).
- Gradle 캐시: gradle/actions/setup-gradle.

## 2.4 릴리즈 사이닝(조건부)
- `app/build.gradle.kts`: env(KEYSTORE_PATH/PASSWORD/KEY_ALIAS/KEY_PASSWORD) 존재 시에만
  release signingConfig 적용 — 로컬 무변경, 시크릿 커밋 없음.
- 워크플로우가 KEYSTORE_BASE64 시크릿으로 jks 복원 → env 전달.

## 2.5 원격 push
- `git remote add origin https://github.com/BoraSarang/PlanJupJup` + main push.

# 3. 검증
- 로컬 랜딩 페이지 http 로드 확인(headless), 워크플로우 YAML 문법 검증(actionlint 또는 수동), 단위테스트 CI 사전 실행.
- push 후 gh run watch로 CI·Pages 진행 확인.

# 4. DoD 메모
- [필수] README·랜딩·CI·Release·Pages 모두 파일 존재 / worktree 정리 / push / Docs 갱신(CHANGELOG·TODO·세션 로그)