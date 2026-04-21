import os
from pathlib import Path

try:
    from dotenv import load_dotenv
except ImportError:
    def load_dotenv(*args, **kwargs):
        return False

ROOT = Path(__file__).resolve().parent
load_dotenv(ROOT / ".env")
load_dotenv(ROOT.parent.parent / ".env")

GOLDENSET_FILES = [
    ROOT / "goldenset-v0.1.json",
    ROOT / "goldenset-v0.2-modern-foreign.json",
]
RESULTS_DIR = ROOT / "results"

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

CHATBOT_MODEL = os.getenv("CHATBOT_MODEL", "gpt-3.5-turbo")
JUDGE_MODEL = os.getenv("JUDGE_MODEL", "gpt-4o")

CHATBOT_TIMEOUT = 30
JUDGE_TIMEOUT = 60
