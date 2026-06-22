# 이커머스 모놀리식 데모 — 시작 버전 (init)

10강 실습의 시작 상태입니다. 설계 문서와 테스트 스펙·경계 게이트는 제공되고,
`src/main` 의 여섯 컨텍스트 구현은 비어 있습니다. PPT 발화를 따라 Claude Code 로 채웁니다.

## 제공 (강사)
- `sdd/00_sources·01_planning·02_plan` : 요구사항·EARS·AC 27개·todos
- `sdd/03_build` : 모듈 구조·API 21개 구현 지도
- `sdd/04_verify` : 검증 기준·AC 커버리지
- `sdd/99_toolchain/01_automation/run_arch_check.py` : DDD 경계 게이트
- `src/test` : 단위 14 + E2E 9 = 23개 테스트 스펙 (통과 대상)

## 학습자 구현
- `src/main/java/kr/elice/shop/` : catalog·inventory·cart·ordering·payment·checkout·shared

## 실습 흐름

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./lab.sh status     # '구현 없음(시작 상태)'
# PPT 발화로 Claude Code 가 여섯 컨텍스트 구현
./lab.sh verify     # 경계 게이트 PASS + 23/23
```

막히면 옆 `../complete/` 의 완성본을 참고합니다.

