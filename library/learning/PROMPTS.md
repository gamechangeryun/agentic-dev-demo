# 발화 대본: Claude Code 로 서브에이전트 병렬 작성·검증 실습 (PPT 13강 싱크)

> PPT 13강의 실습입니다. 서브에이전트로 세 모듈을 병렬 작성하고,
> 에이전트가 TDD·E2E로 대규모 검증합니다.
> 막히면 `./lab.sh solve` 로 완성본을 불러옵니다.
> 다시 처음부터 하려면 `./lab.sh reset` 으로 시작 상태로 되돌립니다.

## 시작 상태 확인

```bash
./lab.sh reset           # 시작 상태(전부 legacy, springboot 비어 있음)
./lab.sh status          # 세 모듈 모두 legacy, springboot 0개 확인
./lab.sh verify          # 레거시 상태에서도 수용기준 green 확인
```

## 전환 규약 (Claude 가 지켜야 할 약속)

- 신규 구현은 `src/main/java/kr/elice/library/springboot/` 에 둡니다.
- 클래스 이름은 `NewBookService`·`NewMemberService`·`NewLoanService` 로 합니다.
  (스프링 기본 빈 이름이 `newBookService` … 가 되어 라우터가 자동 선택합니다.)
- 각 신규 구현은 `kr.elice.library.api` 의 같은 인터페이스를 구현합니다.
- "라우터를 신규로 보낸다" = `src/main/resources/migration.properties` 의 해당 모듈을 `new` 로 바꿉니다.
- 신규 대출은 도서·회원을 직접 부르지 않고 `platform.CatalogRouter` 로 활성 구현을 받아 호출합니다.


## [메인] 서브에이전트 병렬 작성 + 에이전트 E2E 검증

### STEP 1 · 생성기로 SDD 스펙 만들기

```bash
python3 sdd/99_toolchain/01_automation/gen_strangler_spec.py
# → sdd/01_planning/01_feature/book.md   (도서 EARS 스펙)
# → sdd/01_planning/01_feature/member.md (회원 EARS 스펙)
# → sdd/01_planning/01_feature/loan.md   (대출 AC-1·AC-2 스펙)
```

### STEP 2 · 서브에이전트 발화 — 한 번에 세 모듈 병렬 작성

> sdd/01_planning/01_feature/ 의 스펙을 참고해서
> 서브에이전트를 병렬로 써서 도서·회원·대출 세 모듈을
> 한꺼번에 Spring Boot 로 새로 작성해줘.
> 각 모듈에 서브에이전트 하나씩 배정하고 동시에 실행해.
> 대출은 CatalogRouter 로 활성 구현을 호출하게 해줘.
> 완료 후 migration.properties 를 전부 new 로 바꿔줘.

### STEP 3 · 에이전트 검증 발화 — TDD·E2E 대규모 검증

> 방금 서브에이전트로 세 모듈을 새로 작성했어.
> 아래 세 단계를 순서대로 직접 실행하고 결과를 sdd/04_verify 에 기록해줘.
> 1) python3 sdd/99_toolchain/01_automation/run_strangler_check.py
> 2) ./gradlew test
> 3) python3 sdd/99_toolchain/01_automation/gen_proof_evidence.py

예상 결과:
```
[게이트 1] run_strangler_check.py → RESULT: strangler PASS  전환 3/3
[게이트 2] ./gradlew test         → BUILD SUCCESSFUL  7/7 green
[생성기  ] gen_proof_evidence.py  → sdd/04_verify/10_test/proof_evidence.md 자동 생성
```


## [참고] 순차 전환 실습 — 스트랭글러 패턴 이해용

> 서브에이전트를 이해하기 전에 직접 한 모듈씩 전환해보고 싶다면 아래 발화 세 개를 순서대로 실행합니다.

### 1단계 · 도서 모듈

> legacy 패키지의 도서(Book) 모듈을 SDD 스펙으로 정리하고, 그 스펙으로
> springboot 패키지에 NewBookService 를 구현해줘. migration.properties
> 에서 module.books 만 new 로 바꿔서 라우터가 도서만 신규로 보내게 해줘.
> 회원과 대출은 legacy 그대로 둬.

```bash
./lab.sh verify   # 도서=new, 회원·대출=legacy 상태에서도 수용기준 green
```

### 2단계 · 회원 모듈

> 같은 방식으로 legacy 회원(Member) 모듈을 springboot 의 NewMemberService 로
> 구현하고, migration.properties 의 module.members 를 new 로 바꿔줘.

```bash
./lab.sh verify
```

### 3단계 · 대출 모듈

> legacy 대출(Loan) 모듈을 springboot 의 NewLoanService 로 구현해줘. 대출은
> CatalogRouter 로 활성 도서·회원 구현을 받아 호출하고, AC-1 대출 한도 5권과
> AC-2 연체 시 거부를 그대로 지켜줘. migration.properties 의 module.loans 를
> new 로 바꿔서 세 모듈을 모두 신규로 전환해줘.

```bash
# 순차 전환 완료 확인 — 세 게이트 순서대로
python3 sdd/99_toolchain/01_automation/run_strangler_check.py   # 전환 3/3
./lab.sh verify                                                  # 수용기준 7/7 green
./gradlew test --tests 'kr.elice.library.acceptance.AllNewModeTest'  # 완료 게이트 4/4
```


## 멱등 재실행

```bash
./lab.sh reset   # 다시 시작 상태(전부 legacy, springboot 비워짐)
```

`reset → (서브에이전트 발화 또는 순차 발화 세 개) → 에이전트 검증` 을 몇 번 반복해도
매번 같은 결과(전환 3/3 + 7/7 green)로 수렴합니다.
