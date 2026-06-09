# realestate — 부동산 대규모 MSA 데모 (init / complete)

15·16강 "부동산 대규모 프로젝트" 실습의 러닝 예제입니다. 실재 공개 데이터
(data.go.kr 국토교통부 실거래가)를 소재로, Java 17 · Spring Boot · Spring Cloud 로
만든 마이크로서비스(MSA)를 SDD 5단계로 세웁니다. 도메인 모듈
(common · ingestion-service · transaction-service · analytics-service)과 인프라 모듈
(service-discovery · config-server · api-gateway)로 나뉩니다. 같은 예제를 두 형태로 둡니다.

| 폴더 | 버전 | 무엇인가 |
| --- | --- | --- |
| `init/` | 초기 버전 | 강의 시작 상태. sdd 설계 문서 + 테스트 스펙 + 아키텍처 게이트 + 인프라 모듈(discovery·config·gateway)만 있고, 도메인 4개 모듈의 `src/main/java` 구현은 비어 있습니다. 학습자가 발화로 채웁니다. |
| `complete/` | 완성 버전 | 도메인 4개 모듈이 모두 구현된 완성본. 아키텍처 게이트 7/7 PASS + gradle 단위 8/8 통과. 정답·대조용. |

## 실습 방법

1. `cd init` 에서 시작합니다. PPT(15·16강)와 `HANDSON.md`의 발화 흐름을 따라
   Claude Code 로 도메인 모듈을 구현합니다.
   - 15강: 요구사항 정제와 아키텍처 판단(Stage 1~3). `sdd/00_sources` 하나에서
     `01_planning` → `02_plan` 을 발화로 생성합니다.
   - 16강: 비중첩 플랜을 병렬로 코드에 내리고(Stage 4) 게이트·테스트로 검증합니다.
2. 막히거나 결과를 대조하려면 `complete/` 를 봅니다.

## 각 버전 검증 (동일 명령)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./lab.sh status     # 도메인 구현 존재 여부
./lab.sh verify     # 아키텍처 게이트(7) + gradle 단위 테스트(8)
./lab.sh e2e        # 6개 서비스 docker compose 부팅 + 게이트웨이 통과 E2E (Docker 필요)
```

- `complete/` : 아키텍처 게이트 7/7 PASS · 단위 8/8 통과.
- `init/` : 시작 시 도메인 구현이 없어 컴파일에서 멈춥니다. 도메인 4개 모듈을 채우면
  complete 와 같은 결과로 수렴합니다.

## 멱등성

단위 테스트는 난수·실시간·네트워크에 의존하지 않습니다. 외부 data.go.kr 호출은
런타임 전용이고 단위 검증은 순수 도메인 로직만 봅니다. 적재는 거래 자연키로 멱등
처리되어 같은 입력을 여러 번 넣어도 한 번만 반영됩니다. 학습자의 구현 코드가 매번
조금씩 달라도, 고정된 아키텍처 게이트와 테스트 스펙을 통과하는 한 항상 같은 결과로
수렴합니다.
