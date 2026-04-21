"""LLM-as-judge — OpenAI gpt-4o 로 정확도/완전성/유용성 1-5 점.

생성기(gpt-3.5-turbo) 보다 상위 모델을 써서 self-preference bias 를 간접적으로 완화.
동일 모델 패밀리라는 한계는 있으나, A/B/C 상대 비교 목적에는 충분.
"""
import json
import re

from config import JUDGE_MODEL, JUDGE_TIMEOUT, OPENAI_API_KEY

_SYSTEM = """\
당신은 한국 의약품 정보에 대한 챗봇 답변을 평가하는 심사관입니다.
기준은 FDA/MFDS 라벨 수준의 공식 근거입니다.
환자 안전과 관련되므로 부정확·누락된 안전 정보에 엄격히 감점하세요.

세 축으로 1-5 점을 매깁니다:
- accuracy: 답변이 expected_answer/ground_truth_keywords 에 사실적으로 부합하는가 (5=완전 부합, 1=사실 오류)
- completeness: expected_answer 의 핵심 요소를 빠짐없이 다루는가 (5=모두, 1=거의 누락)
- helpfulness: 환자가 행동 결정을 내릴 수 있게 명확한가 (5=매우 명확, 1=모호)

각 점수와 함께 근거 1~2문장을 'rationale' 에 적고, hallucination(근거 없는 주장) 이 있으면 'hallucination_flag': true 로 표시합니다.

반드시 아래 JSON 스키마만 반환하세요. 다른 텍스트 금지:
{
  "accuracy": 1-5,
  "completeness": 1-5,
  "helpfulness": 1-5,
  "hallucination_flag": true|false,
  "rationale": "..."
}
"""


def judge(item: dict, answer: str) -> dict:
    if not OPENAI_API_KEY:
        raise RuntimeError("OPENAI_API_KEY not set — set env var or use --dry-run")
    from openai import OpenAI  # lazy import
    client = OpenAI(api_key=OPENAI_API_KEY, timeout=JUDGE_TIMEOUT)
    resp = client.chat.completions.create(
        model=JUDGE_MODEL,
        messages=[
            {"role": "system", "content": _SYSTEM},
            {"role": "user", "content": _build_prompt(item, answer)},
        ],
        response_format={"type": "json_object"},
    )
    raw = resp.choices[0].message.content or ""
    return _parse(raw)


def _build_prompt(item: dict, answer: str) -> str:
    return (
        f"약물: {item['drug']['name_ko']} ({item['drug']['ingredient_ko']})\n"
        f"카테고리: {item['category']}\n"
        f"질문: {item['question']}\n\n"
        f"expected_answer (정답):\n{item['expected_answer']}\n\n"
        f"ground_truth_keywords: {item.get('ground_truth_keywords', [])}\n\n"
        f"--- 평가 대상 답변 ---\n{answer}\n"
    )


def _parse(raw: str) -> dict:
    match = re.search(r"\{.*\}", raw, re.DOTALL)
    if not match:
        return {"accuracy": None, "completeness": None, "helpfulness": None,
                "hallucination_flag": None, "rationale": f"[parse failure] {raw[:200]}"}
    try:
        return json.loads(match.group(0))
    except json.JSONDecodeError as e:
        return {"accuracy": None, "completeness": None, "helpfulness": None,
                "hallucination_flag": None, "rationale": f"[json error] {e}: {raw[:200]}"}


def dry_judge(item: dict, answer: str) -> dict:
    return {"accuracy": 3, "completeness": 3, "helpfulness": 3,
            "hallucination_flag": False, "rationale": "[DRY-RUN]"}
