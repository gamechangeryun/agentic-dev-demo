# Build 적합성 검증 정책 (Build AST / Architecture Gate)

`sdd/03_build`는 runtime assembly를 설명하는 **current-state 문서**다. dated execution
narrative(언제 무엇을 돌렸다)를 남기지 않는다.

## 구조 게이트

발화로 만든 MSA 구조가 아키텍처 요구사항을 만족하는지 기계로 강제한다. 경계 판단(어느
모듈을 어떻게 나눌지)은 사람이 발화로 정하지만, 그 결과가 지켜야 할 구조 규칙은 게이트가
검증한다.

```bash
python3 sdd/99_toolchain/01_automation/run_arch_check.py
```

게이트가 강제하는 규칙:
1. 필수 7개 모듈 포함 (common·service-discovery·config-server·api-gateway·ingestion-service·transaction-service·analytics-service).
2. common이 도메인 모듈에 역의존하지 않음 (공유 계약 독립).
3. 각 도메인 모듈이 common 공유 계약에 의존함.
4. 게이트웨이가 3개 도메인 라우트를 단일 진입점으로 노출 (ingest·transactions·market-stats).
5. analytics(read model)가 transaction(write model)을 조회함 (CQRS 분리).

## 정책

- 이 게이트는 `verify` 단계에서 `./gradlew test`와 함께 돈다.
- 구조 변경이 위 규칙 중 하나라도 깨면 build/verify를 완료로 보고하지 않는다.
- 새 모듈·라우트를 추가하면 같은 작업에서 게이트 규칙을 확장한다.
