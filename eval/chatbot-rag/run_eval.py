"""Chatbot RAG 평가 오케스트레이터.

사용법:
    python run_eval.py --version A --dry-run             # 로직 검증용 (API 미호출)
    python run_eval.py --version A                       # 실제 평가
    python run_eval.py --version A --limit 5             # 상위 5문항만
    python run_eval.py --version A --category 상호작용    # 카테고리 필터

결과: results/<version>-<timestamp>.json  +  results/<version>-<timestamp>.md
"""
import argparse
import json
import statistics
import sys
from datetime import datetime, timezone
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from config import GOLDENSET_FILES, RESULTS_DIR  # noqa: E402
from evaluators import keyword as kw_eval  # noqa: E402
from evaluators import llm_judge  # noqa: E402
from loader import load_goldenset  # noqa: E402
from runners import version_a, version_b_anchoring, version_c_rag  # noqa: E402

RUNNERS = {"A": version_a, "B": version_b_anchoring, "C": version_c_rag}


def parse_args() -> argparse.Namespace:
    ap = argparse.ArgumentParser()
    ap.add_argument("--version", choices=list(RUNNERS), default="A")
    ap.add_argument("--dry-run", action="store_true")
    ap.add_argument("--limit", type=int, default=None)
    ap.add_argument("--category", default=None)
    ap.add_argument("--skip-judge", action="store_true",
                    help="LLM-judge 생략 (keyword 만). 비용 절감용")
    return ap.parse_args()


def main() -> int:
    args = parse_args()
    runner = RUNNERS[args.version]

    items = load_goldenset(GOLDENSET_FILES)
    if args.category:
        items = [i for i in items if i["category"] == args.category]
    if args.limit:
        items = items[: args.limit]
    print(f"[load] {len(items)} items, version={runner.VERSION_NAME}, dry_run={args.dry_run}")

    print(f"[run] {runner.VERSION_NAME} 답변 생성 중...")
    answers = runner.run(items, dry_run=args.dry_run)
    answers_by_id = {a["id"]: a for a in answers}

    print("[score] 평가 중...")
    results = []
    for i, item in enumerate(items, 1):
        ans = answers_by_id[item["id"]]
        kw = kw_eval.score(item, ans["answer"])
        if args.skip_judge:
            judge = {"accuracy": None, "completeness": None, "helpfulness": None,
                     "hallucination_flag": None, "rationale": "[skipped]"}
        elif args.dry_run:
            judge = llm_judge.dry_judge(item, ans["answer"])
        else:
            judge = llm_judge.judge(item, ans["answer"])
        results.append({
            "id": item["id"],
            "drug": item["drug"]["name_ko"],
            "category": item["category"],
            "difficulty": item["difficulty"],
            "question": item["question"],
            "answer": ans["answer"],
            "latency_ms": ans.get("latency_ms"),
            "tokens_in": ans.get("tokens_in"),
            "tokens_out": ans.get("tokens_out"),
            "keyword": kw,
            "judge": judge,
        })
        if i % 10 == 0:
            print(f"  ...{i}/{len(items)}")

    summary = _summarize(results, runner.VERSION_NAME, args.dry_run)
    out_json, out_md = _save(summary, results, runner.VERSION_NAME)
    print(f"[done] {out_json}")
    print(f"[done] {out_md}")
    _print_summary(summary)
    return 0


def _summarize(results: list[dict], version: str, dry: bool) -> dict:
    def _avg(values):
        vs = [v for v in values if isinstance(v, (int, float))]
        return round(statistics.mean(vs), 3) if vs else None

    kw_recalls = [r["keyword"]["keyword_recall"] for r in results if r["keyword"]["keyword_recall"] is not None]
    accuracies = [r["judge"]["accuracy"] for r in results]
    completenesses = [r["judge"]["completeness"] for r in results]
    helpfulnesses = [r["judge"]["helpfulness"] for r in results]
    hallucinations = [r["judge"]["hallucination_flag"] for r in results if r["judge"]["hallucination_flag"] is not None]
    latencies = [r["latency_ms"] for r in results if r["latency_ms"] is not None]
    tokens_in = sum(r["tokens_in"] for r in results if r["tokens_in"])
    tokens_out = sum(r["tokens_out"] for r in results if r["tokens_out"])

    return {
        "version": version,
        "dry_run": dry,
        "timestamp": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "n_items": len(results),
        "metrics": {
            "keyword_recall_mean": _avg(kw_recalls),
            "judge_accuracy_mean": _avg(accuracies),
            "judge_completeness_mean": _avg(completenesses),
            "judge_helpfulness_mean": _avg(helpfulnesses),
            "hallucination_rate": round(sum(1 for h in hallucinations if h) / len(hallucinations), 3) if hallucinations else None,
        },
        "operational": {
            "p50_latency_ms": round(statistics.median(latencies), 1) if latencies else None,
            "p95_latency_ms": round(_percentile(latencies, 95), 1) if latencies else None,
            "tokens_in_total": tokens_in,
            "tokens_out_total": tokens_out,
        },
        "by_category": _by_group(results, "category"),
        "by_difficulty": _by_group(results, "difficulty"),
    }


def _by_group(results: list[dict], key: str) -> dict:
    groups: dict[str, list[dict]] = {}
    for r in results:
        groups.setdefault(r[key], []).append(r)
    out = {}
    for g, rs in groups.items():
        kws = [r["keyword"]["keyword_recall"] for r in rs if r["keyword"]["keyword_recall"] is not None]
        accs = [r["judge"]["accuracy"] for r in rs if isinstance(r["judge"]["accuracy"], (int, float))]
        out[g] = {
            "n": len(rs),
            "keyword_recall_mean": round(statistics.mean(kws), 3) if kws else None,
            "judge_accuracy_mean": round(statistics.mean(accs), 3) if accs else None,
        }
    return out


def _percentile(values: list[float], pct: float) -> float:
    if not values:
        return 0.0
    s = sorted(values)
    k = (len(s) - 1) * pct / 100
    lo, hi = int(k), min(int(k) + 1, len(s) - 1)
    return s[lo] + (s[hi] - s[lo]) * (k - lo)


def _save(summary: dict, results: list[dict], version: str) -> tuple[Path, Path]:
    RESULTS_DIR.mkdir(parents=True, exist_ok=True)
    ts = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    json_path = RESULTS_DIR / f"{version}-{ts}.json"
    md_path = RESULTS_DIR / f"{version}-{ts}.md"
    json_path.write_text(json.dumps({"summary": summary, "results": results},
                                    ensure_ascii=False, indent=2), encoding="utf-8")
    md_path.write_text(_render_md(summary), encoding="utf-8")
    return json_path, md_path


def _render_md(s: dict) -> str:
    m, op = s["metrics"], s["operational"]
    lines = [
        f"# 평가 리포트 — {s['version']}",
        f"- 시각: {s['timestamp']}  |  문항: {s['n_items']}  |  dry_run: {s['dry_run']}",
        "",
        "## 핵심 지표",
        f"- keyword_recall 평균: **{m['keyword_recall_mean']}**",
        f"- judge accuracy 평균: **{m['judge_accuracy_mean']}**  (1-5)",
        f"- judge completeness 평균: **{m['judge_completeness_mean']}**",
        f"- judge helpfulness 평균: **{m['judge_helpfulness_mean']}**",
        f"- hallucination rate: **{m['hallucination_rate']}**",
        "",
        "## 운영 지표",
        f"- p50 latency: {op['p50_latency_ms']} ms  |  p95: {op['p95_latency_ms']} ms",
        f"- 총 입력 토큰: {op['tokens_in_total']}  |  출력: {op['tokens_out_total']}",
        "",
        "## 카테고리별",
    ]
    for g, v in s["by_category"].items():
        lines.append(f"- {g} (n={v['n']}): kw={v['keyword_recall_mean']}, acc={v['judge_accuracy_mean']}")
    lines += ["", "## 난이도별"]
    for g, v in s["by_difficulty"].items():
        lines.append(f"- {g} (n={v['n']}): kw={v['keyword_recall_mean']}, acc={v['judge_accuracy_mean']}")
    return "\n".join(lines) + "\n"


def _print_summary(s: dict) -> None:
    m = s["metrics"]
    print(f"  keyword_recall={m['keyword_recall_mean']}  "
          f"judge_acc={m['judge_accuracy_mean']}  "
          f"halluc={m['hallucination_rate']}")


if __name__ == "__main__":
    sys.exit(main())
