/**
 * 백엔드 계약(contract)을 1:1로 옮긴 타입 정의입니다.
 * 출처: common/AptTransaction.java, analytics/MarketStat.java
 */

/** 정규화된 아파트 실거래 한 건. 금액은 원(KRW) 단위 정수입니다. */
export interface AptTransaction {
  /** 법정동 시군구코드 (5자리) */
  sggCd: string;
  /** 법정동(읍면동)명 */
  umdNm: string;
  /** 단지명 */
  aptNm: string;
  /** 전용면적(㎡) */
  exclusiveArea: number;
  /** 층 */
  floor: number;
  /** 건축년도 */
  buildYear: number;
  /** 계약년 */
  dealYear: number;
  /** 계약월 */
  dealMonth: number;
  /** 계약일 */
  dealDay: number;
  /** 거래금액 (원 단위) */
  dealAmountWon: number;
  /** 해제여부 (해제거래는 시세 집계에서 제외) */
  canceled: boolean;
}

/** 시세 통계 read model. 해제거래를 제외한 집계 결과입니다. */
export interface MarketStat {
  /** 시군구코드 */
  sggCd: string;
  /** 계약년 */
  dealYear: number;
  /** 계약월 */
  dealMonth: number;
  /** 집계 대상 거래 수 (해제거래 제외) */
  tradeCount: number;
  /** 중위 거래금액(원) */
  medianPriceWon: number;
  /** 중위 ㎡당 단가(원) */
  medianPricePerM2Won: number;
}

/** POST /api/v1/ingest/apt-trade 응답 */
export interface IngestResult {
  /** 수집 시 사용한 5자리 지역코드 */
  lawdCd: string;
  /** 수집 대상 계약월 (YYYYMM) */
  dealYmd: string;
  /** 새로 적재된 건수 (멱등: 재수집 시 0) */
  upserted: number;
}

/** 3화면 공통 조회 파라미터. 조회는 sggCd 이름을 씁니다. */
export interface QueryParams {
  sggCd: string;
  dealYear: number;
  dealMonth: number;
}
