import json
from pathlib import Path
from typing import Iterable


def load_goldenset(paths: Iterable[Path]) -> list[dict]:
    items: list[dict] = []
    seen_ids: set[str] = set()
    for path in paths:
        data = json.loads(path.read_text(encoding="utf-8"))
        for item in data["items"]:
            if item["id"] in seen_ids:
                raise ValueError(f"duplicate id across golden sets: {item['id']}")
            seen_ids.add(item["id"])
            item["_source_file"] = path.name
            items.append(item)
    return items
