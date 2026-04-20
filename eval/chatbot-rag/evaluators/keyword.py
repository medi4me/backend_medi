"""ground_truth_keywords 매칭 — 외부 API 없이 결정적, 비용 0."""


def score(item: dict, answer: str) -> dict:
    keywords = item.get("ground_truth_keywords", [])
    if not keywords:
        return {"keyword_recall": None, "keywords_matched": [], "keywords_missed": []}
    answer_lower = answer.lower()
    matched = [k for k in keywords if k.lower() in answer_lower]
    missed = [k for k in keywords if k.lower() not in answer_lower]
    return {
        "keyword_recall": round(len(matched) / len(keywords), 3),
        "keywords_matched": matched,
        "keywords_missed": missed,
    }