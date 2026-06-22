# 99_toolchain · 자동화 게이트

이커머스 데모의 완료 기준은 "사람이 봤다"가 아니라 "게이트가 통과시켰다"입니다.
두 게이트가 있습니다.

## 게이트 1 · 테스트 게이트 (proof)

```
./gradlew test
```

JUnit 단위 14 + E2E 9 = 23개를 결정적으로 실행합니다. E2E 는 @SpringBootTest +
MockMvc 로 실제 HTTP 라우팅을 통과시킵니다. exit 0 이면 통과입니다.

## 게이트 2 · DDD 경계 게이트 (구조)

```
python3 sdd/99_toolchain/01_automation/run_arch_check.py
```

두 가지 구조 규칙을 코드로 판정합니다.

- 규칙 1: domain 레이어는 application·infrastructure·web 을 import 하지 않는다. 도메인 순수성을 지킵니다.
- 규칙 2: bounded context 의존 그래프에 순환이 없다. 단방향 의존을 강제합니다.

이 게이트는 모놀리식 안에서 컨텍스트 경계가 무너지는 것을 막습니다. 예를 들어
주문 컨트롤러가 체크아웃을 역참조하면 순환이 생기는데, 게이트가 이를 즉시
FAIL 로 잡아냅니다. 실제로 이 데모를 만드는 과정에서 게이트가 한 번 순환을
잡아냈고, 취소 엔드포인트를 checkout 컨텍스트로 옮겨 단방향으로 바로잡았습니다.
