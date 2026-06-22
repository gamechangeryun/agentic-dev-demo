# RealField 부동산 실거래가 데모 (Python 포팅)

자바 Spring Cloud MSA(7모듈) 데모의 **개념 보존 축소판**입니다. MSA 인프라(Eureka·Config·Gateway)는
파이썬 대응물이 없어 1:1로 옮기지 않고, **시세 추정(analytics)을 중심으로 핵심 도메인 3개**를
단일 파이썬 프로세스로 합쳤습니다. 서비스 경계는 모듈(파일) 분리로 개념적으로 유지하고, 저장은 인메모리입니다.

## 도메인 경계 (모듈)

| 파이썬 모듈 | 자바 모듈 | 역할 |
| --- | --- | --- |
| `realfield/common.py` | `common` | 공유 모델 `AptTransaction`, 금액 파서 `DealAmountParser` |
| `realfield/ingestion.py` | `ingestion-service` | 수집·정규화. MOLIT API 는 `SampleMolitSource` 샘플로 대체 |
| `realfield/transaction.py` | `transaction-service` | 실거래 원장 적재·조회 (write model, 멱등) |
| `realfield/analytics.py` | `analytics-service` | 시세 통계 집계 (read model, CQRS) ← **포팅의 중심** |
| `realfield/app.py` | (MSA 7모듈 합본) | 단일 프로세스로 세 서비스를 조립한 진입점 |

## 보존한 도메인 규칙

- **중위가격**: 정렬 후 가운데 값(짝수면 두 가운데 값의 정수 평균)으로 계산합니다.
- **해제거래 제외 (AC-3)**: `canceled=True` 거래는 시세 집계에서 제외합니다.
- **㎡당 단가**: 각 거래의 ㎡당 단가를 구한 뒤 그 중위값을 돌려줍니다(자바 `Math.round` 와 동일한 반올림).
- **멱등 적재 (AC-4)**: 자연키로 중복을 차단해 재수집해도 원장이 늘지 않습니다.
- **금액 정규화 (AC-3)**: 만원 단위 콤마 문자열을 원 단위 정수로 변환합니다.

## 실행법

```bash
cd python

# 1) 가상환경 + 의존성 (proof 에만 pytest 필요, 런타임은 표준 라이브러리만 사용)
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt

# 2) 데모 실행 (수집 → 적재 → 시세 조회)
python3 -m realfield.app

# 3) 빌드(컴파일 검사)
python3 -m compileall -q .

# 4) 테스트(수용기준 검증)
python3 -m pytest -q
```

### 실행 예시 출력

```
수집·적재 건수: 4
시세 통계(read model, 해제거래 제외):
  대상 거래 수      : 3
  중위 거래금액(원) : 700,000,000
  중위 ㎡당 단가(원): 8,238,202
```

샘플 4건 중 1건은 해제거래(이상치)이므로 집계는 유효 3건 기준입니다.

## 테스트 구성 (`tests/`)

- `test_market_stat.py`: 중위가격(홀수/짝수), 해제 제외(AC-3), ㎡당 단가, 빈 통계
- `test_transaction.py`: 멱등 적재(AC-4), ㎡당 단가 반올림·0면적 처리
- `test_ingestion.py`: 정규화(AC-1), 해제 표시(AC-3), 금액 파서
- `test_end_to_end.py`: 수집 → 적재 → 시세 조회 전 흐름, 재수집 멱등성
