# 배포 순서 정책 (Deployment Order)

DEV 반영이 필요한 작업은 항상 다음 순서를 따른다.

```text
main push  ->  DEV 배포  ->  DEV 검증
```

## 환경 표기

- 실행 환경은 항상 `DEV(개발계)`로 표기한다.
- `local`, `localhost 환경`을 운영 용어로 쓰지 않는다.

## main 완료 기준

DEV 배포 baseline이 `main`에 묶이는 팀 규칙에서는, 임시 브랜치/워크트리는 작업 공간일
뿐이다. 작업을 "배포됨"이라 부르기 전에 최종 retained 변경을 `main`에 올리고 `origin/main`을
push한다. 사이드 브랜치 push만으로 DEV rollout 완료라 보고하지 않는다.

## 서비스 기동 순서 (MSA)

도메인 서비스보다 인프라 서비스를 먼저 띄운다.

```text
1) service-discovery (Eureka)      # 모든 서비스 등록 대상
2) config-server                   # 설정·인증키 외부화
3) api-gateway                     # 단일 진입점
4) ingestion-service / transaction-service / analytics-service
```

- 인증키(data.go.kr 등)는 config-server로 외부화하고 저장소 자산에 하드코딩하지 않는다.

## 스테이지드 릴리스 (DEV → PROD)

DEV와 PROD가 분리된 환경에서 rollout이 명시적으로 범위에 들어올 때:

1. 먼저 DEV에 배포하고 retained full-layer 검증 표면을 통과시킨다(하드 게이트).
2. DEV 통과 후에만 PROD로 승격한다.
3. PROD 배포 후 **동일한** retained 검증 표면을 다시 돌린다.
4. PROD 검증 실패 시 즉시 rollback 또는 승인된 복구 절차를 실행하고 결과를 기록한다.

`sdd/05_operate`의 존재만으로 rollout 범위를 추론하지 않는다. 사용자가 배포를 요청했거나
현재 정책/플랜이 rollout을 완료 조건으로 만들 때에만 rollout을 요구한다.
