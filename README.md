# 📱 알뜰요금줍줍 (PlanJupJup)

갤럭시 S22를 **요금제 수집 서버**로 삼는 단일 Android 앱.
알뜰폰(MVNO) 요금제를 주기적으로 자동 수집해, 같은 네트워크의 브라우저에서
**웹 포털**(요금제 탐색 · 통계 대시보드)로 보여줍니다.

[![CI](https://github.com/BoraSarang/PlanJupJup/actions/workflows/ci.yml/badge.svg)](https://github.com/BoraSarang/PlanJupJup/actions/workflows/ci.yml)
[![GitHub Pages](https://github.com/BoraSarang/PlanJupJup/actions/workflows/pages.yml/badge.svg)](https://borasarang.github.io/PlanJupJup/)
[![Latest Release](https://img.shields.io/github/v/release/BoraSarang/PlanJupJup?sort=semver)](https://github.com/BoraSarang/PlanJupJup/releases)
[![APK](https://img.shields.io/badge/APK-Release-download?logo=android)](https://github.com/BoraSarang/PlanJupJup/releases/latest)

- **패키지**: `com.borasarang.planjupjup` · 런처명: 요금줍줍
- **기본 포트**: 3000 (앱 설정에서 변경 가능)
- **포털**: `http://<S22 IP>:3000/`
- **랜딩 페이지**: https://borasarang.github.io/PlanJupJup/

---

## ✨ 핵심 기능

| 기능 | 설명 |
|---|---|
| 🔄 자동 수집 | WorkManager 기반 6시간 간격 워치독 + 브랜드별 개별 주기, 수동 즉시 수집 |
| 📊 통계 대시보드 | 가격대·데이터 용량 분포 / 통신사 점유 / 수집·신규 추이 / 가성비 TOP10 / 수집 건강도 / 자동 인사이트 |
| 🔍 자연어·초성 검색 | `5만원 이하`, `10기가`, `무제한`, `ㄱㅅㅂ` 같은 조건 그대로 검색 |
| 📱 모바일 최적화 포털 | 요금제 탭 · 큐레이션 태그 · 통계 목차 내비게이션, S22 실기 기준 반응형 |
| 🔔 알림 센터 | 신규 요금제 / 수집 완료·실패 알림 + 상세 보기 |

## 📚 문서

| 문서 | 위치 |
|---|---|
| 개발 계획서 | [`docs/plans/`](docs/plans/) |
| 작업 체크리스트 | [`docs/TODO.md`](docs/TODO.md) |
| UI/UX 설계 | [`docs/DESIGN.md`](docs/DESIGN.md) |
| API 명세 | [`docs/api/ENDPOINTS.md`](docs/api/ENDPOINTS.md) |
| 권한 사유서 | [`docs/PERMISSIONS.md`](docs/PERMISSIONS.md) |
| 변경 이력 | [`docs/CHANGELOG.md`](docs/CHANGELOG.md) |
| 사용자 메시지 | `error_message_ko.json` |

## 💾 설치 (APK)

1. **[Releases](https://github.com/BoraSarang/PlanJupJup/releases/latest)**에서 최신 APK 다운로드
2. S22에 설치 후 앱 실행 → 자동 수집 시작
3. 같은 Wi-Fi의 브라우저에서 `http://<S22 IP>:3000/` 접속

> ⚠️ Release APK는 사이닝 키(시크릿)가 설정되기 전까지는 unsigned로 빌드됩니다.
> 직접 설치 테스트는 `assembleDebug` APK를 권장합니다.

## 🔨 빌드

요구: Android Studio 내장 JBR(JAVA_HOME 자동 설정), `~/Library/Android/sdk`.

```bash
./build_and_run.sh build          # assembleDebug + 디바이스 있으면 설치
./build_and_run.sh test           # 단위 테스트
./build_and_run.sh test full      # 단위 + connected 테스트
./build_and_run.sh lint           # Android Lint
./build_and_run.sh clean
```

GitHub Actions CI에서도 동일한 단위 테스트 + 린트가 실행됩니다 (`.github/workflows/ci.yml`).

## 🏗️ CI / Pages / Release

| 항목 | 워크플로우 | 설명 |
|---|---|---|
| CI (빌드·테스트·lint) | `.github/workflows/ci.yml` | push/PR(main) 시 실행 |
| GitHub Pages | `.github/workflows/pages.yml` | `landing/` 랜딩 페이지 배포 |
| 릴리즈 | `.github/workflows/release.yml` | `v*` 태그 push 시 APK 자동 빌드→Release 생성 |

### 릴리즈 사이닝 (선택)
리포지토리 **Secrets**에 아래를 등록하면 Release APK에 서명됩니다.

- `KEYSTORE_BASE64` — keystore `.jks` 파일 base64
- `KEYSTORE_PASSWORD` / `KEY_ALIAS` / `KEY_PASSWORD`

시크릿이 없으면 unsigned APK가 생성됩니다. 키 파일은 커밋되지 않습니다(`*.jks` gitignore).

## 📁 프로젝트 구조

```
PlanJupJup/
├── app/src/main/
│   ├── assets/web/        # 포털 웹 자산 (index.html / style.css / app.js)
│   └── kotlin/.../
│       ├── server/        # 3000 포트 서버 + /api/*
│       ├── crab/          # 수집기 (브랜드/요금제 파싱)
│       ├── data/          # Room(plans/crawl_logs/etc) + Repository
│       └── ui/            # 네이티브 탭 UI (홈/소스/설정/알림)
├── landing/               # GitHub Pages 랜딩 페이지
├── docs/                  # 계획·설계·API·변경이력
└── .github/workflows/     # CI / Pages / Release
```

## 🗂️ 수집 소스 (MVP)

| 소스 | 방식 | 주기 | 상태 |
|---|---|---|---|
| 알뜰폰허브 | HTML 카드 파싱 | 6h | 활성 |
| 모요 | /plans SSR 카드 파싱 | 6h | 활성 |
| KT엠모바일 공식 | 2단계 AJAX JSON | 12h | 활성 |
| LG헬로모바일 공식 | HTML 파싱 | 12h | 준비 중 (실기 검증 후 활성화) |
| 브랜드 목록 | brand.do → carrier_brands | 주 1회 | 활성 |

## 🙏 만든 사람

[BoRaSaRang](https://github.com/BoraSarang) · 이메일: leeborasarang@gmail.com

> 이 레포지토리는 알뜰폰 요금제 정보를 공개 정보에서 수집해 탐색 목적으로 제공합니다.
> 요금제·혜택 상세는 각 통신사 공식 채널에서 최종 확인하세요.