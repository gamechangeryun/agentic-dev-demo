# <기능명> · 검증 요약 (04_verify)

> SDD 'verify' 산출물. retained — 커맨드 레벨 증거 없이 완료 주장 금지.
> 대상 위치: `sdd/04_verify/<section>/`

## 검증 게이트 (커맨드 + 결과)
- `./gradlew test` → exit:
- `python3 sdd/99_toolchain/01_automation/run_arch_check.py` → exit:

## 회귀 검증 결과
- 직접 표면:
- 상류/하류/공유 표면:
- 자동화 없는 슬라이스(수동 검증 + 공백):

## (배포 범위일 때) DEV/PROD 검증
- DEV full-layer 게이트 통과:
- PROD 동일 표면 재실행 결과:
- 실패 시 rollback/복구 결과:

## (persistence 범위일 때) 스키마 정합
- 점검 명령/쿼리·환경·drift 결과:

## 현재 잔여 위험
-
