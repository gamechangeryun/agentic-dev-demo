#!/usr/bin/env python3
"""부동산 대규모 MSA 아키텍처 게이트.

발화로 생성한 MSA 구조가 아키텍처 요구사항을 만족하는지 결정적으로 검증한다.
경계 판단(어느 모듈을 어떻게 나눌지)은 사람이 발화로 정하지만, 그 결과가 지켜야 할
구조 규칙은 이 게이트가 기계로 강제한다. verify 단계에서 gradle 테스트와 함께 돈다.
"""
import os
import sys
import glob

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, "..", "..", ".."))  # realestate/


def read(path):
    try:
        return open(path, encoding="utf-8").read()
    except OSError:
        return ""


def java_src(module):
    files = glob.glob(os.path.join(ROOT, module, "src/main/java/**/*.java"), recursive=True)
    return "\n".join(read(f) for f in files)


checks = []


def check(name, ok, detail=""):
    checks.append((name, bool(ok), detail))


# 1) 필수 7개 모듈이 멀티모듈 구성에 포함되는가
settings = read(os.path.join(ROOT, "settings.gradle"))
required = ["common", "service-discovery", "config-server", "api-gateway",
           "ingestion-service", "transaction-service", "analytics-service"]
missing = [m for m in required if f"'{m}'" not in settings]
check("필수 7개 모듈 포함", not missing, "7/7" if not missing else f"누락: {missing}")

# 2) common(공유 계약)은 도메인 모듈에 역의존하지 않는가
common_src = java_src("common")
leak = [d for d in ["ingestion", "transaction", "analytics"]
        if f"kr.elice.realfield.{d}" in common_src]
check("common이 도메인 모듈에 역의존하지 않음", not leak,
      "공유 계약 독립" if not leak else f"역의존 발견: {leak}")

# 3) 각 도메인 모듈은 common 공유 계약에 의존하는가
for m in ["ingestion-service", "transaction-service", "analytics-service"]:
    check(f"{m} → common 계약 의존", "kr.elice.realfield.common" in java_src(m), "import 확인")

# 4) 게이트웨이가 3개 도메인 라우트를 단일 진입점으로 노출하는가
gw = read(os.path.join(ROOT, "api-gateway/src/main/resources/application.yml"))
routes_ok = all(p in gw for p in ["/api/v1/ingest", "/api/v1/transactions", "/api/v1/market-stats"])
check("게이트웨이 3개 라우트 정의", routes_ok, "ingest·transactions·market-stats")

# 5) analytics(read model)는 transaction(write model)을 조회하는가 (CQRS 분리)
an = java_src("analytics-service") + read(os.path.join(ROOT, "analytics-service/src/main/resources/application.yml"))
check("analytics가 transaction 조회 (CQRS read 분리)", "transaction-service" in an, "lb://transaction-service")

ok = all(c[1] for c in checks)
for name, passed, detail in checks:
    mark = "PASS" if passed else "FAIL"
    print(f"  [{mark}] {name}" + (f" : {detail}" if detail else ""))
passed_n = sum(1 for c in checks if c[1])
print(f"RESULT: {'PASS' if ok else 'FAIL'} ({passed_n}/{len(checks)})")
sys.exit(0 if ok else 1)
