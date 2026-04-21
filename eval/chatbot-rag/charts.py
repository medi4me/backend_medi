"""평가 결과 JSON 으로부터 발표·포트폴리오용 차트 PNG 를 생성합니다.

사용법:
    python charts.py \
        --a results/A-baseline-20260421T062912Z.json \
        --b results/B-anchoring-20260421T063012Z.json \
        --out reports/charts \
        --date 2026-04-21

생성물 (한·영 각 1 세트):
    <date>-hero-metric[-en].png
    <date>-core-metrics[-en].png
    <date>-hallucination-by-drug-group[-en].png
    <date>-category-breakdown[-en].png
"""
import argparse
import json
import sys
from pathlib import Path

import matplotlib as mpl
import matplotlib.pyplot as plt
import numpy as np
from matplotlib import font_manager

COLOR_A = "#9CA3AF"
COLOR_B = "#2563EB"
COLOR_DELTA_UP = "#10B981"
COLOR_DELTA_DOWN = "#DC2626"

CATEGORY_EN = {
    "효능": "Indication",
    "용법용량": "Dosage",
    "부작용": "Adverse",
    "금기": "Contraind.",
    "임신수유": "Pregnancy",
    "상호작용": "Interact.",
    "주의보관": "Warning",
    "비교": "Comparison",
    "한국가용성": "KR Avail.",
}

V01_DRUGS = {"타이레놀", "부루펜(이부프로펜)", "알레그라", "가스터(파모티딘)",
             "지르텍(세티리진)", "메트포르민", "아스피린", "무코펙트(암브록솔)"}
V02_DRUGS = {"위고비", "오젬픽", "마운자로", "젭바운드", "팍스로비드",
             "애더럴", "플랜비", "레켐비"}

L = {
    "ko": {
        "ver_a": "Version A (현재)",
        "ver_b": "Version B (앵커링)",
        "hero_title": "Hallucination Rate",
        "hero_sub": "— 환각 응답 비율 —",
        "before": "도입 전",
        "after": "도입 후",
        "change": "변화",
        "core_title": "핵심 지표 비교",
        "core_xlabel": "",
        "core_ylabel": "점수 (1~5, hallucination 은 0~1)",
        "halluc_title": "약물군별 Hallucination Rate",
        "halluc_sub": "국내 OTC (v0.1) vs 최신·해외 (v0.2)",
        "halluc_ylabel": "Hallucination Rate",
        "group_domestic": "국내 OTC",
        "group_foreign": "최신·해외",
        "cat_title": "카테고리별 정확도 (Accuracy, 1~5)",
        "cat_xlabel": "",
        "cat_ylabel": "정확도",
        "metric_keyword": "키워드 일치",
        "metric_acc": "정확도",
        "metric_comp": "완전성",
        "metric_help": "유용성",
    },
    "en": {
        "ver_a": "Version A (baseline)",
        "ver_b": "Version B (anchoring)",
        "hero_title": "Hallucination Rate",
        "hero_sub": "— share of answers with unsupported claims —",
        "before": "Before",
        "after": "After",
        "change": "Change",
        "core_title": "Core Metric Comparison",
        "core_xlabel": "",
        "core_ylabel": "Score (1–5; hallucination in 0–1)",
        "halluc_title": "Hallucination Rate by Drug Group",
        "halluc_sub": "Korean OTC (v0.1) vs Modern/Foreign (v0.2)",
        "halluc_ylabel": "Hallucination Rate",
        "group_domestic": "Korean OTC",
        "group_foreign": "Modern/Foreign",
        "cat_title": "Accuracy by Category (1–5)",
        "cat_xlabel": "",
        "cat_ylabel": "Accuracy",
        "metric_keyword": "Keyword Recall",
        "metric_acc": "Accuracy",
        "metric_comp": "Completeness",
        "metric_help": "Helpfulness",
    },
}


def setup_font(lang: str) -> None:
    if lang == "ko":
        candidates = ["Apple SD Gothic Neo", "AppleGothic", "Noto Sans KR", "NanumGothic"]
        available = {f.name for f in font_manager.fontManager.ttflist}
        for name in candidates:
            if name in available:
                mpl.rcParams["font.family"] = name
                break
    else:
        for name in ["Helvetica", "Arial", "DejaVu Sans"]:
            if name in {f.name for f in font_manager.fontManager.ttflist}:
                mpl.rcParams["font.family"] = name
                break
    mpl.rcParams["axes.unicode_minus"] = False
    mpl.rcParams["axes.spines.top"] = False
    mpl.rcParams["axes.spines.right"] = False


def load(path: str) -> dict:
    return json.loads(Path(path).read_text(encoding="utf-8"))


def group_for(drug_name: str) -> str:
    if drug_name in V01_DRUGS:
        return "domestic"
    if drug_name in V02_DRUGS:
        return "foreign"
    return "unknown"


def compute_hallucination_by_group(result_items: list[dict]) -> dict:
    counts: dict[str, list[int]] = {"domestic": [], "foreign": []}
    for it in result_items:
        g = group_for(it["drug"])
        if g == "unknown":
            continue
        flag = it.get("judge", {}).get("hallucination_flag")
        if isinstance(flag, bool):
            counts[g].append(1 if flag else 0)
    return {g: (sum(v) / len(v) if v else None) for g, v in counts.items()}


def hero_chart(a: dict, b: dict, out: Path, lang: str) -> None:
    t = L[lang]
    ha = a["summary"]["metrics"]["hallucination_rate"] or 0
    hb = b["summary"]["metrics"]["hallucination_rate"] or 0
    delta = hb - ha
    delta_pct = (delta / ha * 100) if ha > 0 else 0

    fig, ax = plt.subplots(figsize=(14, 7), dpi=300)
    ax.axis("off")
    ax.set_title(t["hero_title"], fontsize=36, fontweight="bold", pad=20)
    ax.text(0.5, 0.88, t["hero_sub"], ha="center", fontsize=14, color="#6B7280", transform=ax.transAxes)

    ax.text(0.20, 0.55, f"{ha*100:.1f}%", ha="center", fontsize=72,
            fontweight="bold", color=COLOR_A, transform=ax.transAxes)
    ax.text(0.20, 0.35, t["before"], ha="center", fontsize=18, color="#6B7280", transform=ax.transAxes)

    ax.annotate("", xy=(0.70, 0.55), xytext=(0.32, 0.55),
                xycoords="axes fraction",
                arrowprops=dict(arrowstyle="->", lw=3, color="#6B7280"))

    ax.text(0.80, 0.55, f"{hb*100:.1f}%", ha="center", fontsize=72,
            fontweight="bold", color=COLOR_B, transform=ax.transAxes)
    ax.text(0.80, 0.35, t["after"], ha="center", fontsize=18, color="#6B7280", transform=ax.transAxes)

    if lang == "ko":
        sign = "▼" if delta < 0 else "▲"
    else:
        sign = "down" if delta < 0 else "up"
    color = COLOR_DELTA_DOWN if delta < 0 else COLOR_DELTA_UP
    ax.text(0.50, 0.13, f"{sign} {abs(delta_pct):.1f}% ({t['change']})",
            ha="center", fontsize=22, fontweight="bold", color=color, transform=ax.transAxes)

    plt.tight_layout()
    fig.savefig(out, dpi=300, bbox_inches="tight")
    plt.close(fig)


def core_metrics_chart(a: dict, b: dict, out: Path, lang: str) -> None:
    t = L[lang]
    labels = [t["metric_keyword"], t["metric_acc"], t["metric_comp"], t["metric_help"]]
    keys = ["keyword_recall_mean", "judge_accuracy_mean",
            "judge_completeness_mean", "judge_helpfulness_mean"]
    a_vals = [(a["summary"]["metrics"][k] or 0) for k in keys]
    b_vals = [(b["summary"]["metrics"][k] or 0) for k in keys]

    # keyword_recall 은 0~1 이므로 5 배 스케일해 비교 표시 (주석 달아 실제 값 표시)
    a_disp = [a_vals[0] * 5] + a_vals[1:]
    b_disp = [b_vals[0] * 5] + b_vals[1:]
    a_raw = a_vals
    b_raw = b_vals

    x = np.arange(len(labels))
    width = 0.35

    fig, ax = plt.subplots(figsize=(12, 7), dpi=300)
    bars_a = ax.bar(x - width/2, a_disp, width, label=t["ver_a"], color=COLOR_A)
    bars_b = ax.bar(x + width/2, b_disp, width, label=t["ver_b"], color=COLOR_B)

    for bar, raw in zip(bars_a, a_raw):
        ax.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.05,
                f"{raw:.2f}", ha="center", fontsize=11, color="#374151")
    for bar, raw in zip(bars_b, b_raw):
        ax.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.05,
                f"{raw:.2f}", ha="center", fontsize=11, color="#374151", fontweight="bold")

    ax.set_ylabel(t["core_ylabel"], fontsize=12)
    ax.set_title(t["core_title"], fontsize=18, fontweight="bold", pad=15)
    ax.set_xticks(x)
    ax.set_xticklabels(labels, fontsize=12)
    ax.set_ylim(0, 5.5)
    ax.legend(fontsize=12, loc="upper left")
    ax.grid(axis="y", linestyle="--", alpha=0.4)

    plt.tight_layout()
    fig.savefig(out, dpi=300, bbox_inches="tight")
    plt.close(fig)


def hallucination_by_group_chart(a: dict, b: dict, out: Path, lang: str) -> None:
    t = L[lang]
    ha = compute_hallucination_by_group(a["results"])
    hb = compute_hallucination_by_group(b["results"])
    labels = [t["group_domestic"], t["group_foreign"]]
    a_vals = [(ha.get("domestic") or 0), (ha.get("foreign") or 0)]
    b_vals = [(hb.get("domestic") or 0), (hb.get("foreign") or 0)]
    x = np.arange(len(labels))
    width = 0.35

    fig, ax = plt.subplots(figsize=(11, 7), dpi=300)
    bars_a = ax.bar(x - width/2, a_vals, width, label=t["ver_a"], color=COLOR_A)
    bars_b = ax.bar(x + width/2, b_vals, width, label=t["ver_b"], color=COLOR_B)

    for bar, v in zip(bars_a, a_vals):
        ax.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.01,
                f"{v*100:.1f}%", ha="center", fontsize=11, color="#374151")
    for bar, v in zip(bars_b, b_vals):
        ax.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.01,
                f"{v*100:.1f}%", ha="center", fontsize=11, color="#374151", fontweight="bold")

    ax.set_ylabel(t["halluc_ylabel"], fontsize=12)
    fig.suptitle(t["halluc_title"], fontsize=18, fontweight="bold", y=0.98)
    ax.set_title(t["halluc_sub"], fontsize=12, color="#6B7280", pad=12)
    ax.set_xticks(x)
    ax.set_xticklabels(labels, fontsize=13)
    ymax = max(max(a_vals), max(b_vals), 0.1) * 1.25
    ax.set_ylim(0, ymax)
    ax.yaxis.set_major_formatter(mpl.ticker.PercentFormatter(1.0))
    ax.legend(fontsize=12, loc="upper left")
    ax.grid(axis="y", linestyle="--", alpha=0.4)

    plt.tight_layout()
    fig.savefig(out, dpi=300, bbox_inches="tight")
    plt.close(fig)


def category_chart(a: dict, b: dict, out: Path, lang: str) -> None:
    t = L[lang]
    cats_a = a["summary"]["by_category"]
    cats_b = b["summary"]["by_category"]
    cats = sorted(set(cats_a) | set(cats_b))
    a_vals = [(cats_a.get(c, {}).get("judge_accuracy_mean") or 0) for c in cats]
    b_vals = [(cats_b.get(c, {}).get("judge_accuracy_mean") or 0) for c in cats]
    display = cats if lang == "ko" else [CATEGORY_EN.get(c, c) for c in cats]
    x = np.arange(len(cats))
    width = 0.35

    fig, ax = plt.subplots(figsize=(14, 7), dpi=300)
    ax.bar(x - width/2, a_vals, width, label=t["ver_a"], color=COLOR_A)
    ax.bar(x + width/2, b_vals, width, label=t["ver_b"], color=COLOR_B)

    for i, (va, vb) in enumerate(zip(a_vals, b_vals)):
        d = vb - va
        if abs(d) > 0.01:
            sign = "+" if d > 0 else ""
            color = COLOR_DELTA_UP if d > 0 else COLOR_DELTA_DOWN
            ax.text(i, max(va, vb) + 0.18, f"{sign}{d:.2f}",
                    ha="center", fontsize=10, color=color, fontweight="bold")

    ax.set_ylabel(t["cat_ylabel"], fontsize=12)
    ax.set_title(t["cat_title"], fontsize=18, fontweight="bold", pad=15)
    ax.set_xticks(x)
    ax.set_xticklabels(display, fontsize=11, rotation=20, ha="right")
    ax.set_ylim(0, 5.5)
    ax.legend(fontsize=12, loc="upper left")
    ax.grid(axis="y", linestyle="--", alpha=0.4)

    plt.tight_layout()
    fig.savefig(out, dpi=300, bbox_inches="tight")
    plt.close(fig)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--a", required=True)
    ap.add_argument("--b", required=True)
    ap.add_argument("--out", default="reports/charts")
    ap.add_argument("--date", required=True, help="YYYY-MM-DD, 파일명 접두사에 사용")
    args = ap.parse_args()

    a = load(args.a)
    b = load(args.b)

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)

    for lang in ("ko", "en"):
        setup_font(lang)
        suffix = "" if lang == "ko" else "-en"
        hero_chart(a, b, out_dir / f"{args.date}-hero-metric{suffix}.png", lang)
        core_metrics_chart(a, b, out_dir / f"{args.date}-core-metrics{suffix}.png", lang)
        hallucination_by_group_chart(a, b, out_dir / f"{args.date}-hallucination-by-drug-group{suffix}.png", lang)
        category_chart(a, b, out_dir / f"{args.date}-category-breakdown{suffix}.png", lang)
        print(f"[charts] {lang} set generated")

    return 0


if __name__ == "__main__":
    sys.exit(main())
