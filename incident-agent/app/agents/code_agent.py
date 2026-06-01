from pathlib import Path

from langchain_groq import ChatGroq
from app.config import (GROQ_API_KEY, MODEL_NAME, TEMPERATURE)
from app.models.code_analysis_result import CodeAnalysisResult
from app.rag.retriever import get_relevant_code
from app.graph.state import IncidentState

class CodeAgent:
    def __init__(self):
        llm = ChatGroq(api_key=GROQ_API_KEY, model=MODEL_NAME, temperature=TEMPERATURE)
        self.structured_llm = (llm.with_structured_output(CodeAnalysisResult))
        prompt_path = (
            Path(__file__)
            .parent.parent
            / "prompts"
            / "code_analysis_prompt.txt"
        )
        self.prompt = prompt_path.read_text()
        
    def analyze_code(self, state: IncidentState) -> CodeAnalysisResult:
        query = ""
        code_chunks = get_relevant_code(query)
        code_context = "\n\n".join(code_chunks)
        
        prompt = self.prompt.format(
            logs_summary=state.get("logs_summary", ""),
            log_errors=state.get("log_errors", []),
            responsible_component=state.get("responsible_component", "unknown"),
            responsible_method=state.get("responsible_method", "unknown"),
            code_context=code_context
        )
        return self.structured_llm.invoke(prompt)
