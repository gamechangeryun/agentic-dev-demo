# ecommerce — 이커머스 모놀리식 데모 (init / complete)

10강 "이커머스 모놀리식을 SDD 5단계로 처음부터 끝까지" 실습의 러닝 예제입니다.
Java 17 · Spring Boot 3.5 · Gradle 로 만든 단일 배포 단위 모놀리식이며, 내부는
DDD bounded context(catalog·inventory·cart·ordering·payment·checkout)로 나뉩니다.
같은 예제를 두 형태로 둡니다.

| 폴더 | 버전 | 무엇인가 |
| --- | --- | --- |
| `init/` | 초기 버전 | 강의 시작 상태. sdd 설계 문서(00~02) + 구현 지도(03_build) + 검증 기준(04_verify) + 테스트 스펙 + 경계 게이트만 있고, `src/main` 구현은 비어 있습니다. 학습자가 발화로 채웁니다. |
| `complete/` | 완성 버전 | 여섯 컨텍스트가 모두 구현된 완성본. 단위 14 + E2E 9 = 23/23 통과, DDD 경계 게이트 PASS. 정답·대조용. |

## 실습 방법

1. `cd init` 에서 시작합니다. PPT(10강)의 발화 흐름을 따라 Claude Code 로
   `src/main` 의 여섯 컨텍스트를 구현합니다.
2. 막히거나 결과를 대조하려면 `complete/` 를 봅니다.

## 각 버전 검증 (동일 명령)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./lab.sh status     # 구현 존재 여부
./lab.sh verify     # DDD 경계 게이트 + 단위·E2E 테스트
```

- `complete/` : 단위 14 + E2E 9 = 23/23 통과 · 경계 게이트 PASS.
- `init/` : 시작 시 구현이 없어 컴파일에서 멈춥니다. 여섯 컨텍스트를 채우면 complete 와 같은 23/23 으로 수렴합니다.

## 멱등성

빌드·테스트는 난수·실시간에 의존하지 않습니다. id는 시퀀스, 테스트는 @DirtiesContext 로
매번 초기화되고, API 는 Idempotency-Key 로 중복 요청을 한 번만 반영합니다. 학습자의
구현 코드가 매번 조금씩 달라도, 고정된 테스트 스펙 23개를 통과하는 한 항상 같은
결과(23/23)로 수렴합니다.
