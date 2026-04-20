"""Version B — 약 메타데이터 앵커링 (RAG 아직 없음).

Version A 와 같은 모델(gpt-3.5-turbo)을 쓰되, 골든셋의 drug 필드를 system 메시지로 주입.
순수 "앵커링 효과" 를 분리해 측정 — 이후 Version C (RAG) 와 대비해
앵커링만으로 얼마나 개선되는지 / RAG 가 추가로 얼마나 끌어올리는지 구분.
"""
import time
from typing import Any

from config import CHATBOT_MODEL, CHATBOT_TIMEOUT, OPENAI_API_KEY

VERSION_NAME = "B-anchoring"

_SYSTEM_TEMPLATE = (
    "당신은 한국 사용자에게 의약품 정보를 설명하는 보조 챗봇입니다.\n"
    "현재 대화는 아래 약에 대한 질문이므로, 이 약의 범위를 벗어난 일반론으로 빗나가지 마세요.\n"
    "불확실하거나 안전에 영향이 있는 사안은 반드시 '의사·약사와 상담'을 안내하고, "
    "처방 변경을 단독으로 지시하지 마세요.\n\n"
    "약 컨텍스트:\n"
    "- 제품명: {name_ko}\n"
    "- 성분: {ingredient_ko}\n"
    "- 구분: {rx_otc}"
)


def run(items: list[dict], dry_run: bool = False) -> list[dict]:
    if dry_run:
        return [_dry_answer(it) for it in items]
    if not OPENAI_API_KEY:
        raise RuntimeError("OPENAI_API_KEY not set — set env var or use --dry-run")
    from openai import OpenAI  # lazy import
    client = OpenAI(api_key=OPENAI_API_KEY, timeout=CHATBOT_TIMEOUT)
    return [_ask(client, it) for it in items]


def _ask(client, item: dict) -> dict:
    system = _build_system(item["drug"])
    started = time.perf_counter()
    resp = client.chat.completions.create(
        model=CHATBOT_MODEL,
        messages=[
            {"role": "system", "content": system},
            {"role": "user", "content": item["question"]},
        ],
    )
    latency_ms = (time.perf_counter() - started) * 1000
    answer = resp.choices[0].message.content or ""
    usage: Any = resp.usage
    return {
        "id": item["id"],
        "answer": answer,
        "latency_ms": round(latency_ms, 1),
        "tokens_in": getattr(usage, "prompt_tokens", None),
        "tokens_out": getattr(usage, "completion_tokens", None),
        "model": CHATBOT_MODEL,
    }


def _build_system(drug: dict) -> str:
    return _SYSTEM_TEMPLATE.format(
        name_ko=drug.get("name_ko", "(미상)"),
        ingredient_ko=drug.get("ingredient_ko", "(미상)"),
        rx_otc=drug.get("rx_otc", "(미상)"),
    )


def _dry_answer(item: dict) -> dict:
    return {
        "id": item["id"],
        "answer": f"[DRY-RUN anchored stub for {item['id']} — drug={item['drug']['name_ko']}]",
        "latency_ms": 0.0,
        "tokens_in": 0,
        "tokens_out": 0,
        "model": "dry-run",
    }
