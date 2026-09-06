# 알뜰요금줍줍 (PlanJupJup)

갤럭시 S22를 서버로 삼아 알뜰폰(MVNO) 요금제를 주기 수집하고,
같은 네트워크의 브라우저에서 웹 포털로 보는 단일 Android 앱.

- 패키지: `com.borasarang.planjupjup` · 런처명: 요금줍줍
- 기본 포트: 3000 (설정 변경 가능) · 포털: `http://<S22 IP>:3000/`

## 빌드

```bash
./build_and_run.sh build          # assembleDebug + 디바이스 있으면 설치
./build_and_run.sh test           # 단위 테스트
./build_and_run.sh test full      # 단위 + connected 테스트
./build_and_run.sh lint           # Android Lint
./build_and_run.sh clean
```

요구: Android Studio 내장 JBR(JAVA_HOME 자동 설정), `~/Library/Android/sdk`.

## 문서

- `docs/plans/PLAN_v1.0_android.md` — 개발 계획서
- `docs/TODO.md` — 작업 체크리스트
- `docs/DESIGN.md` — UI/UX 설계
- `docs/api/ENDPOINTS.md` — API 명세
- `docs/PERMISSIONS.md` — 권한 사유서
- `error_message_ko.json` — 사용자 메시지 매핑

## 수집 소스 (MVP)

| 소스 | 방식 | 상태 |
|---|---|---|
| 알뜰폰허브 | HTML 카드 파싱 | 활성 (6h) |
| 모요 | /plans SSR 카드 파싱 | 활성 (6h) |
| KT엠모바일 공식 | 2단계 AJAX JSON | 활성 (12h) |
| LG헬로모바일 공식 | HTML 파싱 | STAGED (실기 검증 후 활성화) |
| 브랜드 목록 | brand.do → carrier_brands | 활성 (주 1회) |

## 생존성

Foreground Service(dataSync, START_STICKY) + BootReceiver + Watchdog +
설정 화면의 배터리 최적화 예외 요청. 상세는 PLAN 7장.
