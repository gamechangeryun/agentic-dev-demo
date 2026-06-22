# 03_templates · SDD 산출물 스캐폴드

SDD 6단계 워크플로우(planning → plan → implement → build → verify → operate)에서 재사용하는
산출물 템플릿이다. 각 단계 산출물을 새로 만들 때 해당 스캐폴드를 복사해 채운다.

| 단계 | 스캐폴드 | 대상 위치 |
| --- | --- | --- |
| plan | `plan.template.md` | `sdd/02_plan/<section>/` |
| build | `build.template.md` | `sdd/03_build/<section>/` |
| verify | `verify.template.md` | `sdd/04_verify/<section>/` |
| operate | `operate.template.md` | `sdd/05_operate/<runbooks|delivery_status>/` |

> 회귀 범위·DEV/PROD 게이트·rollback 트리거는 `02_policies/`의 정책을 따른다.
