# <기능명> · 작업 계획 (02_plan)

> SDD 'plan' 산출물. 모듈 의존·런타임 흐름·proof 게이트를 확정한다.
> 대상 위치: `sdd/02_plan/<section>/`

## Scope
- 변경 대상 모듈/엔드포인트/화면:
- 범위 밖 (제외):

## Assumptions
- 환경: DEV(개발계)
- 의존/선결 조건:

## Acceptance Criteria
- AC-1:
- (배포가 범위면) DEV 게이트 / 대응 PROD 게이트 / retained full-layer 검증 표면 / rollback 트리거·경로:
- (persistence 변경이면) DEV/PROD 스키마 검증:

## 모듈 의존 그래프
```
common ──┬─→ ingestion-service ──(WebClient)──→ transaction-service
         ├─→ transaction-service
         └─→ analytics-service ──(WebClient, lb)──→ transaction-service
```

## 런타임 흐름 (end-to-end)
```
<요청 → gateway → 서비스 → 결과>
```

## 회귀 범위 (regression scope)
- 직접:
- 상류:
- 하류:
- 공유(common·gateway·config·공통 컴포넌트):
- 정당화된 제외:

## Execution Checklist
- [ ] (정확히 하나만 in-progress 유지)

## Current Notes
-

## Validation (proof 게이트)
- `./gradlew test` exit 0
- `python3 sdd/99_toolchain/01_automation/run_arch_check.py` exit 0
