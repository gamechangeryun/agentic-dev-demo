# 스키마 정합 검증 정책 (Schema Parity)

persistence·models·repositories·migrations·SQL·ORM 매핑을 건드리는 작업은 코드가 배포된
현실과 일치한다고 가정하지 않는다.

## 규칙

- migration 또는 model 의도를 **실제 DEV/PROD 스키마 상태**와 대조한다.
- migration 상태와 런타임 실측 스키마를 **따로** 확인한다.
- 변경이 건드린 테이블·컬럼·인덱스·제약·트리거·기본값·레거시 호환 객체를 검증한다.
- 사용한 명령/쿼리, 점검한 환경, drift 또는 parity 결과를 `sdd/04_verify`에 기록한다.

## 금지

- 로컬 테스트·migration head·현재 model 코드가 배포 스키마 정합을 증명한다고 가정하지 않는다.
- 스키마 drift가 동작에 영향을 줄 수 있는 persistence 작업에서 DEV/PROD 스키마 점검을 건너뛰지 않는다.

> RealField 거래 스키마는 자연키 기반 멱등 upsert에 의존한다. 키·유니크 제약 변경은 항상
> 스키마 정합 검증 대상이다.
