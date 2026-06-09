# 00_sources · RealField 부동산 실거래가 통합 플랫폼 (baseline)

> SDD 0단계 입력입니다. 이 문서는 손대지 않고 보존하며, 01_planning에서 검증 가능한 EARS로 정제합니다.
> 원문 전체는 `1_references/demo-realestate-public/00_요구사항.md` 입니다.

## 발주 개요 (가상)
- 사업명(가칭): RealField, 전국 부동산 실거래가 통합 수집·분석 플랫폼
- 규모: 전국 250개 시군구 × 매월 수집, 누적 수억 건, 마이크로서비스 20+
- 스택: Java 21, Spring Boot 3.5.x, Spring Cloud 2025.0(Northfields), Gradle 멀티모듈

## 외부 연계 원천 (실재 공개 API)
- 국토교통부 아파트 매매 실거래가 OpenAPI (data.go.kr 15126469)
  - 엔드포인트: `https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade`
  - 요청: `serviceKey`, `LAWD_CD`(법정동 5자리), `DEAL_YMD`(YYYYMM), `pageNo`, `numOfRows`
  - 응답(매매): `sggCd`, `umdNm`, `aptNm`, `excluUseAr`, `dealAmount`(만원·콤마), `dealYear/Month/Day`, `floor`, `buildYear`, `dealingGbn`, `cdealType`(해제여부)

## 핵심 요구 (정제 전 원문 발췌)
1. 시군구·계약월 단위로 실거래를 수집해 표준 스키마로 적재한다.
2. 외부 API 지연·실패에도 파이프라인이 멈추지 않게 한다(회복력).
3. 거래금액 콤마 문자열을 정확히 변환하고 해제거래를 집계에서 제외한다(정합).
4. 같은 구간을 재수집해도 중복이 생기지 않게 한다(멱등).
5. 시세 통계는 거래원장과 분리된 조회 모델에서 빠르게 제공한다(CQRS).
