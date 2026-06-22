# Auth/OTP 데모 — 시작 버전 (init)

> 강의 데모용 **가상** 회원가입 OTP 서비스의 **시작 상태**입니다. 실재 개인정보 없음.
> 여기서 출발해 SDD 5단계(planning → operate)를 Claude Code 발화로 직접 채웁니다.
> 완성 형태는 `../complete/` 에 있습니다(막히면 참고하거나 대조하세요).

## 지금 들어 있는 것 (제공)

- `sdd/00_sources/02_requirements/auth-signup.md` : 요구사항 원문 (유일한 입력)
- `sdd/04_verify/10_test/ui_parity/signup_otp.html` : 화면 정합성 스냅샷 (구현이 맞춰야 할 설계 목표)
- `sdd/99_toolchain/01_automation/run_ui_parity.py` : UI parity 게이트 (제공 자동화)
- `.claude` · `.codex` · `.agentic-dev` : agentic-dev 하네스(sdd 스킬 + contract)
- `server/` : 빈 패키지 골격, `conftest.py` · `requirements.txt` · `proof/run_proof.py`

## 채워야 할 것 (planning → operate, 발화로 생성)

| 강의 | 단계 | 만드는 산출물 |
| --- | --- | --- |
| S07 | 01_planning | `sdd/01_planning/01_feature/auth_feature_spec.md` (EARS spec) |
| S08 | 02_plan | `sdd/02_plan/01_feature/auth_todos.md` (todos·분할) |
| S09 | 03_build | `server/contexts/auth/*` 구현 + `tests/*` + `sdd/03_build/01_feature/auth.md` |
| S11 | 99_toolchain | 자동화 게이트 통과 (UI parity) |
| S12 | 04_verify·05_operate | `sdd/04_verify/*` 회귀 4분면 + `sdd/05_operate/*` 런북·상태 |

## 발화 흐름 (Claude Code)

```
> 회원가입 화면에 OTP를 추가하는 기능을 planning부터 시작하자.
  sdd/00_sources 요구사항을 읽고 01_planning EARS spec을 만들어줘.       # S07
> 그 spec으로 02_plan todos와 비중첩 작업 분할을 정리해줘.               # S08
> todos대로 server/contexts/auth에 구현하고 tests와 03_build를 채워줘.   # S09
> UI parity 게이트와 proof를 돌려서 통과시켜줘.                          # S11
> 04_verify 회귀 4분면과 05_operate 런북·배포 상태까지 마무리해줘.        # S12
```

## 검증 (단계가 채워질수록 통과)

```bash
python3 -m compileall -q server                              # build
python3 proof/run_proof.py                                   # proof (구현·테스트가 생기면 통과)
python3 sdd/99_toolchain/01_automation/run_ui_parity.py      # verify_dev (screens 구현 후 1/1)
```

시작 시점에는 구현이 없어 proof·verify_dev가 아직 통과하지 않습니다. 그것을 통과시키는
것이 이 실습의 목표입니다. 완성 상태는 `../complete/` 와 같아집니다.
