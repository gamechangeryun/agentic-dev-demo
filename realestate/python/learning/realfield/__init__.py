"""RealField 부동산 실거래가 데모: Spring Cloud MSA(자바)의 개념 보존 축소판입니다.

자바 데모는 7개 모듈의 Spring Cloud MSA(Eureka·Config·Gateway 포함)이지만,
이 파이썬 포팅은 시세 추정(analytics)을 중심으로 핵심 도메인 3개만 단일 프로세스로 합칩니다.
MSA 인프라(서비스 디스커버리·게이트웨이)는 재현하지 않고, 도메인 규칙의 동등성에 집중합니다.

서비스 경계는 모듈(파일) 분리로 개념적으로 유지합니다.
  - common      : 공유 도메인 모델·금액 파서 (자바 common 모듈)
  - transaction : 실거래 원장 적재·조회 (자바 transaction-service, write model)
  - analytics   : 시세 통계 집계 (자바 analytics-service, read model, CQRS)
  - ingestion   : 수집·정규화 (자바 ingestion-service, MOLIT 연동은 샘플 데이터로 대체)
"""
