# 외부 연계 계약 (07_integration): data.go.kr 국토교통부 실거래가

> 2단계 산출물입니다. 실재 공개 API와의 연계 계약과 회복력 정책을 박제합니다.

## 엔드포인트
- 아파트 매매 실거래가: `https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade` (data.go.kr 15126469)
- 아파트 전월세 실거래가(확장): data.go.kr 15126474

## 요청 파라미터
| 파라미터 | 의미 |
| --- | --- |
| serviceKey | 인증키 (환경변수 `MOLIT_SERVICE_KEY`, 비노출) |
| LAWD_CD | 법정동코드 5자리(시군구). 예: 서울 종로구 11110 |
| DEAL_YMD | 계약월 YYYYMM. 예: 202405 |
| pageNo / numOfRows | 페이징 |

## 응답 필드(매매, XML item)
`sggCd`, `umdNm`, `jibun`, `aptNm`, `excluUseAr`, `dealYear/Month/Day`,
`dealAmount`(만원·콤마), `floor`, `buildYear`, `dealingGbn`(중개/직거래), `cdealType`(해제여부 O)

## 회복력 정책 (AC-2)
| 정책 | 값 |
| --- | --- |
| 재시도 | 3회, 0.5s 간격 |
| 서킷브레이커 | sliding window 20, 실패율 50%, open 10s |
| fallback | 빈 결과(부분 수집 허용) |

> data.go.kr는 트래픽·운영시간 제한이 있습니다. 외부 장애를 수집 경계 안에 가두어 거래원장·분석으로 전파시키지 않습니다.

## 법정동코드 조회
- 행정표준코드관리시스템 `https://www.code.go.kr/stdcode/regCodeL.do`
