# MyLink 금융·공공 데모 타깃 · Java (S13)

> 강의 13강 데모용 **가상** 금융·공공 서비스(자바·Spring Boot). 실재 기관·시스템·개인정보·실명 없음.
> 이 폴더는 자바 단일 언어로 자립합니다. agentic-dev 하네스(`.claude/skills/sdd`)를 적용해 SDD 워크플로우를 실제로 관통한 결과입니다.

## 무엇인가
"동의 완료 후 전자민원 자동 발급" 한 기능을, 대규모·고규제 제약(회복력·근거인용·멱등·
동의철회)과 함께 end-to-end로 구현하고 결정적 proof 게이트로 증명한 최소 실동작 repo입니다.

## 실행
```bash
./gradlew build -x test                  # build
./gradlew test                           # proof  → 2/2 PASS, tmp/proof-results.json
./gradlew uiParity                        # verify_dev (회귀 게이트 재실행)
```

(또는 `.agentic-dev/contract.json` 의 `commands.build` / `commands.proof` / `commands.verify_dev` 그대로.)

## 구조
- `src/main/java/com/datasense/finance/`: 도메인 구현 (eminwon·settlement·advisory·gw 어댑터·shared 계약)
- `src/test/java/...`: proof 게이트 = AC-1~AC-5 + 회귀
- `sdd/`: SDD 5단계 산출물 (00_sources → 01_planning → 02_plan → 03_build → 04_verify → 05_operate → 99_toolchain)
- `.agentic-dev/contract.json`: build/proof/deploy_dev/verify_dev 명령
- `.claude/skills/sdd`, `.codex/skills/sdd`: 적용된 SDD 스킬
