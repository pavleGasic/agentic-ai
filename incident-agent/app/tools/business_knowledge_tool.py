from pathlib import Path
from langchain_core.tools import tool

KNOWLEDGE_BASE_PATH = Path(__file__).parent.parent / "rag" / "knowledge" / "business_knowledge.txt"

@tool("business_knowledge_tool", return_direct=True)
def search_business_knowledge() -> str:
    """Returns business rules and domain knowledge about the invoice processing system."""
    return KNOWLEDGE_BASE_PATH.read_text()