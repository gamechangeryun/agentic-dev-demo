# 02_policies · 실행 규칙 정본

RealField 저장소의 컨벤션과 실행 규칙 정본이다. `.claude/CLAUDE.md`, `.claude/skills/sdd/SKILL.md`,
`.codex/*`는 이 정책들을 참조한다.

| 정책 | 파일 | 요약 |
| --- | --- | --- |
| 회귀 검증 | `regression_verification.md` | 직접·상류·하류·공유 표면 선택을 retained 산출물로 남긴다 |
| Build 적합성(AST) | `build_ast_check.md` | run_arch_check.py가 MSA 구조 규칙을 기계로 강제한다 |
| 배포 순서 | `deployment_order.md` | main push → DEV 배포 → DEV 검증, 서비스 기동 순서 |
| 스키마 정합 | `schema_parity.md` | persistence 변경 시 DEV/PROD 스키마 실측 비교 |

> 환경 표기는 항상 `DEV(개발계)`. `local`/`localhost 환경`을 운영 용어로 쓰지 않는다.
