#!/usr/bin/env bash
# 부동산 대규모 MSA 실습 하네스: 발화만으로 몇 번이든 같은 결과로 반복(멱등)하게 만든다.
#
#   ./lab.sh reset    깨끗한 시작 상태로 되돌린다. 도메인 구현(common·ingestion·
#                     transaction·analytics 의 src/main/java)을 비우고, sdd 문서·
#                     테스트(스펙)·인프라 모듈(discovery·config·gateway)·빌드 설정·
#                     게이트는 그대로 둔다.
#   ./lab.sh solve    정답 구현(solution/)을 복원한다. 라이브가 막혔을 때의 폴백,
#                     또는 완성본 확인용.
#   ./lab.sh verify   아키텍처 게이트 + gradle 단위 테스트(8개)를 돌린다.
#   ./lab.sh status   현재 상태(도메인 구현 존재 여부)를 보여준다.
#
# reset→(발화로 구현 또는 solve)→verify→reset 을 반복해도 매번 동일하게 수렴한다.
# 테스트는 난수·실시간·네트워크에 의존하지 않는다(외부 data.go.kr 호출은 런타임 전용,
# 단위 테스트는 순수 도메인 로직만 검증). 그래서 결정적이다.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
cd "$HERE"
DOMAIN=(common ingestion-service transaction-service analytics-service)
: "${JAVA_HOME:=$(/usr/libexec/java_home -v 17 2>/dev/null || true)}"
export JAVA_HOME

reset() {
  for m in "${DOMAIN[@]}"; do rm -rf "$m/src/main/java/kr"; done
  echo "[reset] 도메인 구현 제거 완료. 시작 상태로 되돌렸습니다."
  echo "        남아 있는 것: sdd/ 문서, src/test/ (스펙), 인프라 모듈, build.gradle, 게이트."
  status
}

solve() {
  for m in "${DOMAIN[@]}"; do
    if [ ! -d "solution/$m/kr" ]; then echo "[solve] solution/$m 스냅샷이 없습니다." >&2; exit 1; fi
    rm -rf "$m/src/main/java/kr"
    mkdir -p "$m/src/main/java"
    cp -R "solution/$m/kr" "$m/src/main/java/"
  done
  echo "[solve] 정답 구현을 복원했습니다."
  status
}

status() {
  local n=0
  for m in "${DOMAIN[@]}"; do
    n=$((n + $(find "$m/src/main/java" -name '*.java' 2>/dev/null | wc -l | tr -d ' ')))
  done
  if [ "$n" != "0" ]; then
    echo "[status] 도메인 구현 있음 (${n}개 .java) → verify 가능"
  else
    echo "[status] 도메인 구현 없음(시작 상태) → 발화로 구현 필요"
  fi
}

verify() {
  echo "[verify] 1/2 아키텍처 게이트"
  python3 sdd/99_toolchain/01_automation/run_arch_check.py
  echo "[verify] 2/2 gradle 단위 테스트"
  ./gradlew test --console=plain -q
  echo "[verify] 통과: 아키텍처 게이트 + 단위 8/8"
}

case "${1:-}" in
  reset)  reset ;;
  solve)  solve ;;
  verify) verify ;;
  status) status ;;
  *) echo "사용법: ./lab.sh {reset|solve|verify|status}"; exit 2 ;;
esac
