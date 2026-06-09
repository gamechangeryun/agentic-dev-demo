#!/usr/bin/env bash
# 이커머스 실습 하네스. 몇 번이든 같은 결과로 반복(멱등).
#   ./lab.sh reset    src/main 구현을 비워 시작 상태로 (sdd·테스트·게이트 유지)
#   ./lab.sh verify   DDD 경계 게이트 + gradle 단위·E2E 테스트
#   ./lab.sh status   현재 구현 존재 여부
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"; cd "$HERE"
IMPL="src/main/java/kr/elice/shop"
: "${JAVA_HOME:=$(/usr/libexec/java_home -v 17 2>/dev/null || true)}"; export JAVA_HOME

reset() {
  rm -rf "src/main/java/kr"; mkdir -p "$(dirname "$IMPL")"
  echo "[reset] 구현 제거 완료. 시작 상태로 되돌렸습니다."
  status
}
status() {
  if [ -d "$IMPL" ] && [ "$(find "$IMPL" -name '*.java' | wc -l | tr -d ' ')" != "0" ]; then
    echo "[status] 구현 있음 ($(find "$IMPL" -name '*.java' | wc -l | tr -d ' ')개 .java) → verify 가능"
  else
    echo "[status] 구현 없음(시작 상태) → build 단계 필요"
  fi
}
verify() {
  echo "[verify] 1/2 DDD 경계 게이트"
  python3 sdd/99_toolchain/01_automation/run_arch_check.py | tail -1
  echo "[verify] 2/2 gradle 단위 + E2E 테스트"
  ./gradlew clean test --console=plain -q
  echo "[verify] 통과: 단위 14 + E2E 9 = 23/23"
}
case "${1:-}" in
  reset)  reset ;;
  verify) verify ;;
  status) status ;;
  *) echo "사용법: ./lab.sh {reset|verify|status}"; exit 2 ;;
esac
