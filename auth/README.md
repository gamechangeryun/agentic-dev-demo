# auth — 회원가입 OTP 데모 (init / complete)

SDD-stage 강의들이 공유하는 회원가입 OTP 러닝 예제입니다(S05·07·08·09·11·12·16).
같은 예제를 두 형태로 둡니다.

| 폴더 | 버전 | 무엇인가 |
| --- | --- | --- |
| `init/` | 초기 버전 | 강의 시작 상태. 요구사항(00_sources) + 하네스 + 게이트만 있고, 구현과 planning~operate 산출물은 비어 있습니다. 학습자가 발화로 채웁니다. |
| `complete/` | 완성 버전 | planning → operate 5단계가 모두 채워진 완성본. 구현·테스트·proof 통과(10/10). 정답·대조용. |

## 실습 방법

1. `cd init` 에서 시작합니다. `init/README.md` 의 발화 흐름(S07 planning → S12 operate)을 따라
   Claude Code 로 단계를 채웁니다.
2. 막히거나 결과를 대조하려면 `complete/` 를 봅니다.

## 각 버전 검증 (contract 명령 동일)

```bash
python3 -m compileall -q server                              # build
python3 proof/run_proof.py                                   # proof
python3 sdd/99_toolchain/01_automation/run_ui_parity.py      # verify_dev
```

- `complete/` : proof 10/10 · UI parity 1/1 통과.
- `init/` : 시작 시 구현이 없어 proof·verify_dev 미통과. 단계를 채우면 complete 와 같아집니다.

## 강의 ↔ 단계 ↔ 산출물

| 강의 | 단계 | 산출물 |
| --- | --- | --- |
| S07 | 01_planning | `sdd/01_planning/01_feature/auth_feature_spec.md` |
| S08 | 02_plan | `sdd/02_plan/01_feature/auth_todos.md` |
| S09 | 03_build | `server/contexts/auth/*` + `sdd/03_build/01_feature/auth.md` |
| S11 | 99_toolchain | `sdd/99_toolchain/01_automation/run_ui_parity.py` 게이트 통과 |
| S12 | 04_verify·05_operate | `sdd/04_verify/*` + `sdd/05_operate/*` |
