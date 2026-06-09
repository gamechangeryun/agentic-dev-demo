# -*- coding: utf-8 -*-
"""DDD 경계 게이트: 레이어·컨텍스트 의존 규칙을 코드로 판정한다.

강의 toolchain 게이트의 이커머스판이다. 두 가지를 점수화한다.
  규칙 1: domain 레이어는 application·infrastructure·web 을 import 하지 않는다.
  규칙 2: bounded context 의존 그래프에 순환이 없다.
exit 0 = 두 규칙 모두 통과.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[3]
SRC = ROOT / "src/main/java/kr/elice/shop"
CONTEXTS = {"catalog", "inventory", "cart", "ordering", "payment", "checkout"}
LAYERS = ("domain", "application", "infrastructure", "web")
IMPORT_RE = re.compile(r"^import\s+kr\.elice\.shop\.([a-z]+)\.([a-z]+)", re.MULTILINE)


def analyze():
    layer_violations = []
    edges = set()  # (contextA -> contextB)
    for java in SRC.rglob("*.java"):
        rel = java.relative_to(SRC).parts
        ctx = rel[0]
        layer = rel[1] if len(rel) > 2 else None
        text = java.read_text(encoding="utf-8")
        for m in IMPORT_RE.finditer(text):
            dep_ctx, dep_layer = m.group(1), m.group(2)
            # 규칙 1: domain 이 상위 레이어를 참조하면 위반
            if layer == "domain" and dep_layer in ("application", "infrastructure", "web"):
                layer_violations.append(f"{'/'.join(rel)} → {dep_ctx}.{dep_layer}")
            # 규칙 2: 컨텍스트 간 엣지 수집 (shared·자기 자신 제외)
            if dep_ctx in CONTEXTS and ctx in CONTEXTS and dep_ctx != ctx:
                edges.add((ctx, dep_ctx))
    return layer_violations, edges


def find_cycle(edges):
    graph = {}
    for a, b in edges:
        graph.setdefault(a, set()).add(b)
    state = {}  # 0=visiting, 1=done

    def dfs(node, path):
        state[node] = 0
        for nxt in graph.get(node, ()):
            if state.get(nxt) == 0:
                return path + [node, nxt]
            if state.get(nxt) is None:
                r = dfs(nxt, path + [node])
                if r:
                    return r
        state[node] = 1
        return None

    for n in list(graph):
        if state.get(n) is None:
            r = dfs(n, [])
            if r:
                return r
    return None


def main():
    violations, edges = analyze()
    cycle = find_cycle(edges)
    print("[arch] DDD 경계 게이트")
    print(f"  규칙1 domain 순수성 위반: {len(violations)}건")
    for v in violations:
        print(f"    - {v}")
    print(f"  규칙2 컨텍스트 의존 엣지: {sorted(edges)}")
    print(f"  규칙2 순환 의존: {'없음' if not cycle else ' → '.join(cycle)}")
    ok = not violations and not cycle
    print(f"RESULT: arch_check {'PASS' if ok else 'FAIL'}")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
