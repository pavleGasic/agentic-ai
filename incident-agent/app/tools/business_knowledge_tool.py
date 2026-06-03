from pathlib import Path
from langchain_core.tools import tool

KNOWLEDGE_BASE_PATH = Path(__file__).parent.parent / "rag" / "knowledge_base" / "business_knowledge.txt"

@tool("business_knowledge_tool", return_direct=True)
def business_knowledge_tool() -> str:
    """Returns business rules and domain knowledge about the invoice processing system."""
    return KNOWLEDGE_BASE_PATH.read_text()