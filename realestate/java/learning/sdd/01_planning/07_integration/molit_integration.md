# 외부 연계 계약 — data.go.kr 아파트 매매 실거래가 (회복력)

> 출처(00_sources): API 공개명세(`getRTMSDataSvcAptTradeDev`)·요구사항정의서(SIR-001/002/005, CONR-001/002).
> data.go.kr 연계의 요청·응답·회복력·에러 처리 계약. 외부 의존은 `ingestion-service` 경계에만 둔다. (SDD 2단계 산출물 · AC-2)

## 1. 엔드포인트 / 요청
- `GET https://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev`
- 파라미터: `serviceKey`(환경변수 주입), `LAWD_CD`(5자리), `DEAL_YMD`(YYYYMM), `pageNo`, `numOfRows`(1000)
- 인증키는 `MOLIT_SERVICE_KEY` 환경변수로만 주입(SECR-001). 코드·설정·로그 평문 금지.

## 2. 응답 / 페이징
- 응답은 **XML**(`_type=json` 미보장) → 수집 서비스가 XML 파싱(SIR-002, CONR-002).
- `<header><resultCode>` `000`만 적재 대상. `<body>`의 `totalCount`/`numOfRows`/`pageNo`로 **전 페이지 전량** 수집(SFR-002).
- `pageNo`를 1부터 증가시키며 `누적 건수 ≥ totalCount`까지 반복.

## 3. 회복력 정책 (resilience4j) — AC-2
| 정책 | 값 | 동작 |
| --- | --- | --- |
| TimeLimiter | 호출 타임아웃 | 초과 시 실패로 간주 |
| Retry | 최대 3회, 지수 백오프 | 일시 오류(01/04/99) 재시도 |
| CircuitBreaker | 실패율 임계 초과 시 open | open 동안 **빈 결과 폴백**(부분 수집 계속) |
- 한 시군구·페이지 실패가 배치 전체를 멈추지 않는다(부분 수집, SFR-011).

## 4. 결과코드 처리 매핑
| resultCode | 의미 | 처리 |
| --- | --- | --- |
| 000 | 정상 | item 정규화·적재 |
| 03 | NODATA | 정상 종료(빈 결과) |
| 01 / 04 / 99 | 내부/HTTP/알수없음 | 재시도 대상 |
| 22 | 트래픽 초과 | 백오프 후 재시도 또는 익일 재개 |
| 12 / 20 / 30 / 31 / 32 | 서비스없음/권한/키/만료/IP | **즉시 실패·알림**(재시도 무의미) |

## 5. 트래픽 한도 가드 (CONR-001 / PER-004)
- 개발 일일 한도 **10,000건**. 시군구·계약월 단위 순차/제한 동시 수집으로 한도 보호.
- 호출 카운트를 추적해 한도 임박 시 백오프, 초과 전 중단·익일 재개.

> 정규화·금액 변환·해제 판정은 `common` 정합 규칙(`04_data`)을 따른다. 외부 응답 형태가 바뀌어도 이 경계 안에서 흡수한다.
