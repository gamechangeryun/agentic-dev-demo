# CLAUDE.md

## What This Is

RealField(부동산 실거래 분석) MSA 저장소에서 Claude/Codex 실행 규칙과 자동화 하네스 위치를 설명하는 문서다.
이 저장소는 `sdd/`를 정본 delivery system으로 취급하는 SDD-first 레포다.

## Environment Naming Policy

- 문서/대화/로그에서 실행 환경은 항상 `DEV(개발계)`로 표기한다.
- `local`, `localhost 환경` 같은 표현은 운영 용어로 사용하지 않는다.

## Harness Layout

```text
realestate/
├── .claude/         # Claude 설정
│   ├── CLAUDE.md    # 본 문서 — 실행 규칙·하네스 layout
│   ├── agents/      # Claude role agents (backend-dev, frontend-dev, ...)
│   └── skills/      # Claude Code repo-local skills (.claude/skills/<name>/SKILL.md)
├── .codex/          # Codex 설정, 에이전트, 스킬
│   ├── config.toml  # multi-agent 설정
│   ├── agents/      # Codex role 서브에이전트 (*.toml)
│   └── skills/      # Codex repo-local skills
├── common/          # 공유 계약 모듈 (DTO·자연키·정합 규칙)
├── service-discovery/   # Eureka 서비스 디스커버리
├── config-server/   # 설정·인증키 외부화
├── api-gateway/     # 단일 진입점 (ingest·transactions·market-stats 라우팅)
├── ingestion-service/   # data.go.kr(MOLIT) 수집·정규화
├── transaction-service/ # 거래 write model (멱등 upsert)
├── analytics-service/   # 시세 통계 read model (CQRS)
├── web/             # 프론트엔드 (다른 담당자 영역)
└── sdd/             # SDD delivery system + toolchain 정책/자동화 문서
```

## Working Rules

- 컨벤션과 실행 규칙의 정본은 `sdd/99_toolchain/02_policies`에 둔다.
- DEV 반영이 필요한 작업은 항상 `main push -> DEV 배포 -> DEV 검증` 순서를 따른다.
- 저장소는 특정 서비스/실환경에 종속된 자격증명, 실제 인증키, 브라우저 상태를 커밋하지 않는다.
- `sdd/03_build`는 runtime assembly를 설명하는 current-state 문서이며 dated execution narrative를 남기지 않는다.
- AST-style build current-state 적합성과 MSA 구조 규칙은 `sdd/99_toolchain/01_automation/run_arch_check.py`로 검증한다.
- 회귀 검증 범위 선택은 retained 산출물이며 `sdd/02_plan`, `sdd/03_build`, `sdd/04_verify`로 이어진다.

## Claude Skills

- Claude Code 최신 project skill 표면은 `.claude/skills/<name>/SKILL.md`다.
- Claude Code의 custom commands와 skills는 merge된 표면으로 취급한다. 이 저장소는 공식 skills 디렉터리 구조를 기본값으로 쓴다.
- Codex와 Claude가 같은 실행 하네스를 공유해야 할 때는 `.codex/skills/*`의 정본 계약을 재사용하고, Claude skill은 그 진입 규칙과 운영 가이드를 얇게 감싼다.
- 이 저장소의 기본 제공 skill 표면은 `sdd`다 (SDD 6단계 워크플로우: planning → plan → implement → build → verify → operate).
