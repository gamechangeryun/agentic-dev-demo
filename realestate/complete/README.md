# RealField 부동산 실거래가 대규모 MSA 데모 (15·16강)

> 실재하는 공개 데이터(data.go.kr 국토교통부 부동산 실거래가) 위에서 대규모 Spring Cloud MSA를
> **Claude Code 발화만으로** SDD 5단계로 개발하는 데모 타깃입니다. 발주 맥락(사업명·규모)은
> 강의용 가상이며 개인정보·실명은 없습니다. 15강은 요구사항·아키텍처 판단, 16강은 병렬 구현·검증입니다.

## 무엇인가
"시군구·계약월 실거래 수집 → 정규화·멱등 적재 → 시세 통계 CQRS 조회" 한 기능을, 대규모·외부연계
제약(회복력·정합·멱등·읽기분리)과 함께 구현한 Gradle 멀티모듈 Spring Cloud MSA입니다.

## 실습은 멱등합니다 (lab.sh)
실습은 몇 번이든 같은 결과로 반복됩니다. 발화 실습은 `reset`에서 출발합니다.
```bash
./lab.sh status     # 도메인 구현 존재 여부
./lab.sh reset      # 도메인 구현(common·ingestion·transaction·analytics)을 비운 시작 상태
./lab.sh verify     # 아키텍처 게이트(7) + gradle 단위 테스트(8)
./lab.sh solve      # 정답 구현 복원 (라이브 폴백·완성본 확인)
```
`reset → 발화로 구현 → verify → reset`을 반복해도 매번 **아키텍처 게이트 PASS(7/7) + 단위 8/8**로
수렴합니다. 단위 테스트는 외부 data.go.kr를 호출하지 않는 순수 도메인 로직이라 네트워크·인증키
없이 결정적입니다. 검증 실적: 이 워크스페이스(**JDK 17 + Gradle 8.5 래퍼**)에서 arch 7/7 · 단위 8/8.

진행 절차(15강 Stage 1~3, 16강 Stage 4)는 `HANDSON.md`를 따릅니다.

## 스택
- Java 17 · Spring Boot 3.5.x · Spring Cloud 2025.0 (Northfields) · Gradle 멀티모듈

## 모듈 (7)
| 모듈 | 포트 | 역할 |
| --- | --- | --- |
| service-discovery | 8761 | Eureka 디스커버리 |
| config-server | 8888 | 인증키·엔드포인트·회복력 정책 외부화 |
| api-gateway | 8080 | 단일 진입점·라우팅 |
| common | (lib) | 표준 DTO·금액 파서(정합 규칙 1곳) |
| ingestion-service | 8081 | data.go.kr 수집·정규화·회복력 |
| transaction-service | 8082 | 멱등 적재·조회 (write model) |
| analytics-service | 8083 | 시세 통계 (read model, CQRS) |

## 구조
- `sdd/00_sources/`: 주어지는 발주 입력 세 벌, 손대지 않음 (요구사항정의서·API 공개명세·데이터 명세서)
- `sdd/01_planning/`: 구조화·아키텍처 산출물 (01_feature·03_architecture·04_data·05_api·07_integration·08·09)
- `sdd/02_plan/`: 비중첩 작업 분할
- `sdd/99_toolchain/01_automation/run_arch_check.py`: 아키텍처 게이트
- `*/src/main/java`: 모듈별 구현 · `solution/`: 정답 스냅샷(lab.sh가 복원)
- `lab.sh`: 멱등 실습 하네스 (reset/solve/verify/status)
- `.claude/skills/sdd`·`.codex/skills/sdd`: 적용된 SDD 스킬
- `.agentic-dev/contract.json`: build/proof/deploy_dev/verify_dev 명령

## 경계 (정직)
단위 검증은 네트워크 없이 결정적입니다. 실제 서비스 부팅 E2E(eureka·gateway·도메인 동시 기동)와
data.go.kr 실호출은 Docker·인증키가 필요합니다(강사 환경). 인증키는 환경변수로만 주입하고
git·이미지에 넣지 않습니다.
