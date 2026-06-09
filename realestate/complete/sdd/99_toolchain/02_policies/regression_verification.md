# 회귀 검증 정책 (Regression Verification)

회귀 검증 범위 선택은 **선택 사항이 아니라 retained 산출물**이다. `sdd/02_plan` →
`sdd/03_build` → `sdd/04_verify`로 동일한 범위가 이어져야 한다.

## 범위 선택 규칙

구현을 완료라 부르기 전에 회귀 표면을 정의한다.

1. **직접 표면(direct)** — 변경한 모듈/엔드포인트/화면 자체.
2. **상류(upstream)** — 변경 대상을 호출하는 쪽 (예: api-gateway 라우트, ingestion → transaction).
3. **하류(downstream)** — 변경 대상이 호출하는 쪽 (예: analytics → transaction read).
4. **공유(shared)** — common 공유 계약, 라우팅, 인증/설정, 공통 컴포넌트, 생성 자산.

변경이 다음을 건드리면 편집한 모듈만 검증하지 말고 **범위를 넓힌다**:
- common 공유 계약(DTO·자연키·정합 규칙)
- api-gateway 라우팅 / 서비스 간 WebClient 계약
- config-server 설정 / 인증·시크릿 경계
- 멱등 upsert·CQRS read 모델 같은 공유 데이터 흐름

## 검증 게이트

- `./gradlew test` — 모듈 단위 + proof 게이트 (exit 0).
- `python3 sdd/99_toolchain/01_automation/run_arch_check.py` — MSA 구조 규칙 (exit 0).
- 자동화가 없는 슬라이스는 가능한 수동/커맨드 검증을 돌리고 그 공백을 **현재 잔여 위험**으로 기록한다.

## 산출물

- `sdd/02_plan/...` acceptance criteria에 선택한 회귀 범위와 정당화된 제외 항목을 명시한다.
- `sdd/03_build`에 실제 검증한 범위를 current-state로 남긴다.
- `sdd/04_verify`에 커맨드 레벨 증거(명령어·결과)와 잔여 위험을 남긴다.

회귀 범위 선택을 retained SDD trail에서 누락하지 않는다.
