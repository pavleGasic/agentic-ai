from langchain_core.tools import tool

from app.rag.retriever import get_relevant_code

@tool("search_code", return_direct=True)
def search_code(query: str, class_name: str = None) -> str:
    """Search invoice management source code for relevant classes and methods."""
    results = get_relevant_code(query, class_name)
    return "\n\n---\n\n".join(results)