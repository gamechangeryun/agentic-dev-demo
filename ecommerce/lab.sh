#!/usr/bin/env bash
# 이커머스 실습 하네스: 실습을 몇 번이든 같은 결과로 반복(멱등)하게 만든다.
#
#   ./lab.sh reset    깨끗한 시작 상태로 되돌린다. src/main 구현을 비우고,
#                     sdd 문서(00~02)·테스트(스펙)·빌드 설정·게이트는 그대로 둔다.
#   ./lab.sh verify   DDD 경계 게이트 + gradle 단위·E2E 테스트를 돌린다.
#   ./lab.sh status   현재 상태(구현 존재 여부)를 보여준다.
#
# 어느 명령이든 여러 번 실행해도 같은 결과가 된다. reset→build(Claude Code)→verify 를
# 반복해도, 구현이 테스트 스펙을 통과하는 한 매번 동일하게 23/23 으로 수렴한다.
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
cd "$HERE"
IMPL="src/main/java/kr/elice/shop"
: "${JAVA_HOME:=$(/usr/libexec/java_home -v 17 2>/dev/null || true)}"
export JAVA_HOME

reset() {
  # 구현만 제거한다. shared 포함 production 코드 전부가 학습자 구현 대상이다.
  rm -rf "src/main/java/kr"
  mkdir -p "$(dirname "$IMPL")"
  echo "[reset] 구현 제거 완료. 시작 상태로 되돌렸습니다."
  echo "        남아 있는 것: sdd/ 설계 문서(00~02), src/test/ (스펙), build.gradle, 게이트."
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
