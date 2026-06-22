# 외부 API 공개 명세 원문: 국토교통부 아파트 매매 실거래가 자료 (data.go.kr)

> SDD 0단계(00_sources) 입력 문서입니다. 외부 공개 API의 공개 명세를 가공 없이 원문 그대로 박제합니다.
> 이 문서는 01_planning의 데이터 모델·연계 설계가 참조하는 단일 사실 원천(Single Source of Truth)입니다.
> 항목 코드 도메인·표기·품질 기준은 데이터 명세서 `00_sources/03_data_spec/realprice_data_spec.md`를 함께 참조합니다.
> 출처: 공공데이터포털(data.go.kr) 데이터셋 15126469 / 15126468(상세), 국토교통부 부동산소비자보호기획단 제공 OpenAPI 기술문서.

---

## 1. 서비스 개요

| 항목 | 내용 |
| --- | --- |
| 서비스명(국문) | 국토교통부_아파트 매매 실거래가 자료 |
| 제공기관 | 국토교통부 (부동산소비자보호기획단) |
| 데이터셋 ID | 15126469 (기본), 15126468 (상세 자료) |
| API 유형 | REST (OpenAPI) |
| 인증 방식 | serviceKey (공공데이터포털 발급 인증키) 쿼리 파라미터 |
| 응답 포맷 | XML (기본). 일부 운영 엔드포인트는 _type=json 미지원, XML 파싱 전제 |
| 업데이트 주기 | 실시간(신고 접수 반영) |
| 심의유형 | 자동승인 (개발단계·운영단계 모두) |
| 비용 | 무료 |
| 이용허락범위 | 이용허락범위 제한 없음 |

서비스 설명: 행정표준코드관리시스템의 법정동시군구코드(앞 5자리)와 계약년월(YYYYMM)을 입력하면, 해당 지역·해당 기간에 신고된 아파트 매매 실거래 상세 정보를 조회한다. 개인정보 보호를 위해 아파트의 층 정보만 기본 제공되며, 소유권 이전등기가 완료된 건에 한하여 동(棟) 정보가 추가로 공개된다.

---

## 2. 엔드포인트

| 단계 | 엔드포인트 |
| --- | --- |
| 운영(상세, 권장) | `http://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev` |
| 운영(기본) | `http://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade` |

- 운영기관 코드 `1613000`은 국토교통부를 의미한다.
- `RTMSDataSvcAptTradeDev`(상세, Dev) 엔드포인트가 2024년 신규 항목(매도자/매수자 구분, 등기일자, 거래유형, 중개사 소재지, 토지임대부 여부 등)을 모두 포함한다. 본 사업은 상세 엔드포인트를 표준으로 채택한다.
- HTTPS(`https://apis.data.go.kr/...`) 호출을 권장한다.

---

## 3. 요청 변수 (Request Parameters)

| 변수명 | 한글명 | 필수 | 타입 | 설명 | 예시 |
| --- | --- | --- | --- | --- | --- |
| serviceKey | 서비스키 | 필수 | String | 공공데이터포털에서 발급받은 인증키. Encoding/Decoding 두 종류가 발급되며, 라이브러리 호출 시 Decoding 키 권장. | (발급 인증키) |
| LAWD_CD | 지역코드 | 필수 | String(5) | 행정표준코드관리시스템의 법정동코드 10자리 중 앞 5자리(시군구). | `11110` (서울 종로구) |
| DEAL_YMD | 계약년월 | 필수 | String(6) | 거래(계약) 발생 연월. YYYYMM 형식. | `202405` |
| pageNo | 페이지번호 | 선택 | Integer | 결과 페이지 번호. 기본값 1. | `1` |
| numOfRows | 페이지당 건수 | 선택 | Integer | 한 페이지 결과 수. 기본값 10. 실무상 1000까지 지정해 페이징 호출을 줄인다. | `1000` |

요청 예시(상세 엔드포인트):

```
GET http://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev
    ?serviceKey={DECODING_KEY}
    &LAWD_CD=11110
    &DEAL_YMD=202405
    &pageNo=1
    &numOfRows=1000
```

---

## 4. 응답 구조 (Response)

응답은 `<response>` 루트 아래 `<header>`(처리 결과)와 `<body>`(데이터 본문)로 구성된다. `<body>` 안의 `<items>`는 거래 건마다 `<item>`을 반복하며, `<numOfRows>`, `<pageNo>`, `<totalCount>`로 페이징 메타데이터를 제공한다.

### 4.1 헤더 항목

| 필드명 | 한글명 | 설명 |
| --- | --- | --- |
| resultCode | 결과코드 | `000`이면 정상. 그 외는 에러코드. |
| resultMsg | 결과메시지 | 처리 결과 메시지(NORMAL SERVICE 등). |

### 4.2 본문 페이징 항목

| 필드명 | 한글명 | 설명 |
| --- | --- | --- |
| numOfRows | 페이지당 건수 | 응답에 포함된 행 수. |
| pageNo | 페이지번호 | 현재 페이지. |
| totalCount | 전체 건수 | 해당 지역·계약월 전체 신고 건수. 페이징 종료 판단에 사용. |

### 4.3 거래 항목 (item) 전체 필드

> 아래는 상세 엔드포인트(`getRTMSDataSvcAptTradeDev`)가 반환하는 item 필드 전체이다. 타입은 모두 XML 텍스트(String)로 전달되며, 숫자 항목도 문자열로 내려온다. 거래금액은 만원 단위이며 천 단위 콤마와 선행 공백을 포함할 수 있다.

| 필드명 | 한글명 | 타입 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| sggCd | 법정동시군구코드 | String(5) | 시군구 법정동코드 5자리. 요청 LAWD_CD와 동일 체계. | `11110` |
| umdNm | 법정동 | String | 법정동 읍면동명. | `청운동` |
| umdCd | 법정동읍면동코드 | String | 읍면동 법정동코드. | `11500` |
| landCd | 법정동지번코드 | String | 토지 지번 구분 코드(1=대지 등). | `1` |
| bonbun | 법정동본번코드 | String | 지번 본번. | `0028` |
| bubun | 법정동부번코드 | String | 지번 부번. | `0000` |
| jibun | 지번 | String | 표시용 지번 문자열. | `28` |
| roadNm | 도로명 | String | 도로명 주소의 도로명. | `자하문로` |
| roadNmSggCd | 도로명시군구코드 | String | 도로명 기준 시군구코드. | `11110` |
| roadNmCd | 도로명코드 | String | 도로명 식별 코드. | `4100388` |
| roadNmSeq | 도로명일련번호코드 | String | 도로명 일련번호. | `01` |
| roadNmbCd | 도로명지하여부코드 | String | 지하 여부 코드. | `0` |
| roadNmBonbun | 도로명건물본번호코드 | String | 건물 본번호. | `00028` |
| roadNmBubun | 도로명건물부번호코드 | String | 건물 부번호. | `00000` |
| aptNm | 단지명(아파트) | String | 아파트 단지명. | `청운현대` |
| aptDong | 아파트 동명 | String | 소유권 이전등기 완료 건에 한해 공개. 미공개 시 공백. | `101` |
| aptSeq | 단지일련번호 | String | 단지 식별 일련번호(시군구코드-단지번호). | `11110-2339` |
| excluUseAr | 전용면적 | String(숫자) | 전용면적(㎡). 소수점 포함. | `84.97` |
| dealYear | 계약년도 | String(숫자) | 계약 발생 연도(YYYY). | `2024` |
| dealMonth | 계약월 | String(숫자) | 계약 발생 월(1~12). | `5` |
| dealDay | 계약일 | String(숫자) | 계약 발생 일(1~31). | `23` |
| dealAmount | 거래금액 | String | 거래금액(만원 단위). 천 단위 콤마·선행 공백 포함. | `  82,500` |
| floor | 층 | String(숫자) | 거래 층. | `10` |
| buildYear | 건축년도 | String(숫자) | 준공 연도(YYYY). | `2013` |
| dealingGbn | 거래유형 | String | 중개거래 / 직거래 구분. 빈 값일 수 있음. | `중개거래` |
| estateAgentSggNm | 중개사소재지 | String | 중개업소 소재 시군구(시도+시군구 수준). | `서울 종로구` |
| cdealType | 해제여부 | String | 계약 해제 시 `O`. 미해제는 공백. | `O` |
| cdealDay | 해제사유발생일 | String | 해제 신고 발생일(YY.MM.DD 형식). 미해제는 공백. | `24.06.01` |
| rgstDate | 등기일자 | String | 소유권 이전등기 일자. 미완료 시 공백. | `24.07.10` |
| slerGbn | 매도자 | String | 매도자 구분(개인 / 법인 / 공공기관 / 기타). | `개인` |
| buyerGbn | 매수자 | String | 매수자 구분(개인 / 법인 / 공공기관 / 기타). | `개인` |
| landLeaseholdGbn | 토지임대부 아파트 여부 | String | 토지임대부 아파트 여부(Y / N). | `N` |

> 2024년 신규/확장 항목: `dealingGbn`(거래유형), `estateAgentSggNm`(중개사소재지), `cdealType`/`cdealDay`(해제 정보), `rgstDate`(등기일자), `slerGbn`/`buyerGbn`(매도자·매수자 구분), `landLeaseholdGbn`(토지임대부 여부). 기본(비-Dev) 엔드포인트에는 일부가 없을 수 있어, 본 사업은 상세 엔드포인트로 고정한다.

---

## 5. 응답 샘플

### 5.1 XML (실제 응답 형태)

```xml
<response>
  <header>
    <resultCode>000</resultCode>
    <resultMsg>OK</resultMsg>
  </header>
  <body>
    <items>
      <item>
        <sggCd>11110</sggCd>
        <umdNm>청운동</umdNm>
        <jibun>28</jibun>
        <aptNm>청운현대</aptNm>
        <aptSeq>11110-2339</aptSeq>
        <excluUseAr>84.97</excluUseAr>
        <dealYear>2024</dealYear>
        <dealMonth>5</dealMonth>
        <dealDay>23</dealDay>
        <dealAmount>  82,500</dealAmount>
        <floor>10</floor>
        <buildYear>2013</buildYear>
        <dealingGbn>중개거래</dealingGbn>
        <estateAgentSggNm>서울 종로구</estateAgentSggNm>
        <cdealType></cdealType>
        <cdealDay></cdealDay>
        <rgstDate>24.07.10</rgstDate>
        <slerGbn>개인</slerGbn>
        <buyerGbn>개인</buyerGbn>
        <landLeaseholdGbn>N</landLeaseholdGbn>
      </item>
      <!-- ... item 반복 ... -->
    </items>
    <numOfRows>1000</numOfRows>
    <pageNo>1</pageNo>
    <totalCount>143</totalCount>
  </body>
</response>
```

### 5.2 JSON (참고: 일부 클라이언트가 XML을 매핑한 형태)

> 본 API의 표준 응답은 XML이다. 아래는 내부 수집 서비스가 XML을 파싱해 표준화하기 직전 단계로 정규화한 참고 표현이며, 외부 API가 JSON을 직접 보장하지는 않는다.

```json
{
  "response": {
    "header": { "resultCode": "000", "resultMsg": "OK" },
    "body": {
      "items": {
        "item": [
          {
            "sggCd": "11110",
            "umdNm": "청운동",
            "jibun": "28",
            "aptNm": "청운현대",
            "aptSeq": "11110-2339",
            "excluUseAr": "84.97",
            "dealYear": "2024",
            "dealMonth": "5",
            "dealDay": "23",
            "dealAmount": "  82,500",
            "floor": "10",
            "buildYear": "2013",
            "dealingGbn": "중개거래",
            "estateAgentSggNm": "서울 종로구",
            "cdealType": "",
            "cdealDay": "",
            "rgstDate": "24.07.10",
            "slerGbn": "개인",
            "buyerGbn": "개인",
            "landLeaseholdGbn": "N"
          }
        ]
      },
      "numOfRows": 1000,
      "pageNo": 1,
      "totalCount": 143
    }
  }
}
```

---

## 6. 에러/결과 코드

> 공공데이터포털 공통 OpenAPI 에러 코드 체계를 따른다. resultCode가 `000`이 아니면 비정상이며, 수집 서비스는 본문을 적재하지 않고 회복력 정책(재시도·서킷)으로 처리한다.

| 코드 | 메시지 | 의미 | 수집 서비스 처리 |
| --- | --- | --- | --- |
| 000 | NORMAL SERVICE / OK | 정상 | item 정규화·적재 |
| 01 | APPLICATION ERROR | 서비스 내부 오류 | 재시도 대상 |
| 03 | NODATA ERROR | 해당 조건 데이터 없음 | 정상 종료(빈 결과) |
| 04 | HTTP ERROR | HTTP 오류 | 재시도 대상 |
| 12 | NO OPENAPI SERVICE ERROR | 폐기되었거나 없는 서비스 | 즉시 실패·알림 |
| 20 | SERVICE ACCESS DENIED ERROR | 접근 거부(권한 없음) | 즉시 실패·알림 |
| 22 | LIMITED NUMBER OF SERVICE REQUESTS EXCEEDS ERROR | 트래픽 초과 | 백오프 후 재시도 또는 익일 재개 |
| 30 | SERVICE KEY IS NOT REGISTERED ERROR | 미등록 인증키 | 즉시 실패·키 점검 |
| 31 | DEADLINE HAS EXPIRED ERROR | 활용기간 만료 | 즉시 실패·키 갱신 |
| 32 | UNREGISTERED IP ERROR | 미등록 IP | 즉시 실패·IP 등록 |
| 99 | UNKNOWN ERROR | 알 수 없는 오류 | 재시도 대상 |

---

## 7. 트래픽 한도·활용 정책

| 항목 | 내용 |
| --- | --- |
| 개발계정 일일 트래픽 | 10,000건 |
| 운영계정 트래픽 | 활용사례 등록 시 증액 신청 가능 |
| 활용신청 | data.go.kr에서 [활용신청] 후 활용 목적 제출, 자동승인 |
| 키 발급 형태 | Encoding 키 / Decoding 키 2종. 코드에서 직접 URL 조립 시 Decoding 키 사용 권장 |
| 인증키 반영 지연 | 신규 신청 후 키 활성화까지 최대 1영업일 소요될 수 있음 |
| 동시성 | 명시적 동시호출 제한은 없으나, 일일 한도 내에서 호출. 본 사업은 시군구·계약월 단위 순차/제한 동시 수집으로 한도를 보호 |

---

## 8. 라이선스

| 항목 | 내용 |
| --- | --- |
| 비용 | 무료 |
| 이용허락범위 | 제한 없음 |
| 개인정보 | 거래 당사자 식별정보 미포함. 동(棟) 정보는 등기 완료 건에 한해 제한적으로만 공개. 본 API는 개인정보를 포함하지 않는다 |
| 출처 표기 | 공공데이터 이용 시 출처(국토교통부, 공공데이터포털) 표기 권장 |

---

## 9. 부속 원천: 법정동코드

- LAWD_CD(시군구 5자리)는 행정표준코드관리시스템에서 조회·관리한다.
- 조회: `https://www.code.go.kr/stdcode/regCodeL.do` (법정동코드 10자리 중 앞 5자리 사용).
- 본 사업은 250개 시군구 코드를 마스터로 보관하고, 매월 (시군구 × 계약월) 조합으로 수집을 구동한다.
