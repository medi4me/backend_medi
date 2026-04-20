"""Version A — 현재 프로덕션 챗봇 재현.

ChatbotServiceImpl 기준: gpt-3.5-turbo, 시스템 프롬프트 없음, 순수 사용자 메시지만 전달.
RAG/앵커링 비교의 baseline.
"""
import time
from typing import Any

from config import CHATBOT_MODEL, CHATBOT_TIMEOUT, OPENAI_API_KEY

VERSION_NAME = "A-baseline"


def run(items: list[dict], dry_run: bool = False) -> list[dict]:
    if dry_run:
        return [_dry_answer(it) for it in items]
    if not OPENAI_API_KEY:
        raise RuntimeError("OPENAI_API_KEY not set — set env var or use --dry-run")
    from openai import OpenAI  # lazy import: 드라이런엔 openai 패키지 없어도 됨
    client = OpenAI(api_key=OPENAI_API_KEY, timeout=CHATBOT_TIMEOUT)
    return [_ask(client, it) for it in items]


def _ask(client, item: dict) -> dict:
    started = time.perf_counter()
    resp = client.chat.completions.create(
        model=CHATBOT_MODEL,
        messages=[{"role": "user", "content": item["question"]}],
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


def _dry_answer(item: dict) -> dict:
    return {
        "id": item["id"],
        "answer": f"[DRY-RUN stub answer for {item['id']}]",
        "latency_ms": 0.0,
        "tokens_in": 0,
        "tokens_out": 0,
        "model": "dry-run",
    }