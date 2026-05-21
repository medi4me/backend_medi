"""Version C — RAG. 검색 서비스(/retrieve)로 라벨 청크를 받아 컨텍스트로 주입.

Version B(앵커링)와 같은 모델·시스템 프롬프트 골격을 쓰되, 약 메타데이터(이름·성분)만
넣던 자리에 mediforme-chatbot-rag 의 /retrieve 가 돌려준 실제 라벨 청크를 넣는다.
B 대비 "검색된 근거를 주면 hallucination 이 얼마나 더 줄어드나" 를 측정.

drug_id 는 골든셋의 ingredient_en 을 쓰며, 검색 서비스의 brand·generic alias 필터로
해당 약 청크만 좁혀 검색한다. 복합제(a/b)는 첫 성분으로 필터링한다.
"""
import json
import time
import urllib.error
import urllib.request
from typing import Any

from config import (
    CHATBOT_MODEL,
    CHATBOT_TIMEOUT,
    OPENAI_API_KEY,
    RAG_SERVICE_URL,
    RAG_TIMEOUT,
    RAG_TOP_K,
)

VERSION_NAME = "C-rag"

# 생성 모델(gpt-3.5-turbo, 16385 토큰) 한도를 넘지 않도록 컨텍스트 토큰 예산을 둔다.
# 표·화학명 많은 라벨은 토큰 밀도가 높아 top_k 청크가 한도를 넘길 수 있다.
_CONTEXT_TOKEN_BUDGET = 12000

_SYSTEM_TEMPLATE = (
    "당신은 한국 사용자에게 의약품 정보를 설명하는 보조 챗봇입니다.\n"
    "현재 대화는 아래 약에 대한 질문이므로, 이 약의 범위를 벗어난 일반론으로 빗나가지 마세요.\n"
    "불확실하거나 안전에 영향이 있는 사안은 반드시 '의사·약사와 상담'을 안내하고, "
    "처방 변경을 단독으로 지시하지 마세요.\n\n"
    "아래 [검색된 라벨 컨텍스트] 에 근거해 답하고, 컨텍스트에 없는 내용은 추측하지 마세요.\n\n"
    "약: {name_ko} ({ingredient_ko})\n\n"
    "[검색된 라벨 컨텍스트]\n{context}"
)


def run(items: list[dict], dry_run: bool = False) -> list[dict]:
    if dry_run:
        return [_dry_answer(it) for it in items]
    if not OPENAI_API_KEY:
        raise RuntimeError("OPENAI_API_KEY not set — set env var or use --dry-run")
    from openai import OpenAI  # lazy import: 드라이런엔 openai 패키지 없어도 됨

    client = OpenAI(api_key=OPENAI_API_KEY, timeout=CHATBOT_TIMEOUT)
    return [_ask(client, it) for it in items]


def _ask(client, item: dict) -> dict:
    drug = item["drug"]
    drug_id = _drug_id(drug)
    chunks = _retrieve(item["question"], drug_id)
    system = _SYSTEM_TEMPLATE.format(
        name_ko=drug.get("name_ko", "(미상)"),
        ingredient_ko=drug.get("ingredient_ko", "(미상)"),
        context=_format_context(chunks),
    )
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
        "retrieved_n": len(chunks),
        "retrieved_drugs": sorted({c["drug_name"] for c in chunks}),
    }


def _drug_id(drug: dict) -> str:
    ingredient = (drug.get("ingredient_en") or "").strip()
    return ingredient.split("/")[0].strip()


def _retrieve(question: str, drug_id: str) -> list[dict]:
    payload = json.dumps(
        {"query": question, "drug_id": drug_id or None, "top_k": RAG_TOP_K}
    ).encode("utf-8")
    req = urllib.request.Request(
        f"{RAG_SERVICE_URL}/retrieve",
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=RAG_TIMEOUT) as resp:
            data = json.loads(resp.read().decode("utf-8"))
    except urllib.error.URLError as e:
        raise RuntimeError(
            f"/retrieve 호출 실패 ({RAG_SERVICE_URL}): {e}. 검색 서비스가 떠 있는지 확인하세요"
        ) from e
    return data.get("chunks", [])


def _format_context(chunks: list[dict]) -> str:
    if not chunks:
        return "(검색 결과 없음 — 컨텍스트 없이 답변)"
    encoder = _get_encoder()
    parts: list[str] = []
    used = 0
    for i, c in enumerate(chunks, 1):
        block = f"{i}. [{c['drug_name']} / {c['section']}]\n{c['text']}"
        cost = _count_tokens(encoder, block)
        # 유사도 상위 청크부터 채우고, 토큰 예산을 넘기는 이후 청크는 생략
        if parts and used + cost > _CONTEXT_TOKEN_BUDGET:
            break
        parts.append(block)
        used += cost
    return "\n\n".join(parts)


def _get_encoder() -> Any:
    try:
        import tiktoken

        return tiktoken.get_encoding("cl100k_base")
    except Exception:
        return None


def _count_tokens(encoder: Any, text: str) -> int:
    if encoder is None:
        return len(text) // 2  # tiktoken 없을 때 보수적 추정 (2 chars/token)
    return len(encoder.encode(text))


def _dry_answer(item: dict) -> dict:
    return {
        "id": item["id"],
        "answer": f"[DRY-RUN rag stub for {item['id']} — drug_id={_drug_id(item['drug'])}]",
        "latency_ms": 0.0,
        "tokens_in": 0,
        "tokens_out": 0,
        "model": "dry-run",
        "retrieved_n": 0,
        "retrieved_drugs": [],
    }
