# API 계약 (05_api)

> 2단계 산출물입니다. 게이트웨이 라우팅과 서비스 엔드포인트 계약을 정의합니다.

## 게이트웨이 라우팅
| 경로 | 대상 서비스 |
| --- | --- |
| `/api/v1/ingest/**` | ingestion-service |
| `/api/v1/transactions/**` | transaction-service |
| `/api/v1/market-stats/**` | analytics-service |

## ingestion-service
```
POST /api/v1/ingest/apt-trade?lawdCd={5자리}&dealYmd={YYYYMM}
→ 200 {"lawdCd":"11110","dealYmd":"202405","upserted":143}
```

## transaction-service
```
POST /api/v1/transactions/bulk      body: AptTransaction[]
→ 200  (새로 적재된 건수, 멱등)

GET  /api/v1/transactions?sggCd={}&dealYear={}&dealMonth={}
→ 200  AptTransaction[]
```

## analytics-service
```
GET  /api/v1/market-stats?sggCd={}&dealYear={}&dealMonth={}
→ 200 {"sggCd":"11110","dealYear":2024,"dealMonth":5,
       "tradeCount":141,"medianPriceWon":825000000,"medianPricePerM2Won":9709309}
```

> 조회는 거래원장이 아니라 analytics read model을 통합니다(AC-5). 게이트웨이는 단일 진입점입니다.
