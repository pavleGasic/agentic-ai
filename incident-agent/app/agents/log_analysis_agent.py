from pathlib import Path

from langchain_core.prompts import ChatPromptTemplate
from langchain_groq import ChatGroq

from app.config import (GROQ_API_KEY, MODEL_NAME, TEMPERATURE)
from app.graph.state import IncidentState

class LogAnalysisAgent:
    
    def __init__(self):
        self.llm = ChatGroq(api_key=GROQ_API_KEY, model=MODEL_NAME, temperature=TEMPERATURE)
        self.structured_llm = (self.llm.with_structured_output(IncidentState))
        
        prompt_path = (
            Path(__file__)
            .parent.parent
            / "prompts"
            / "log_analysis_prompt.txt"
        )
        
        self.prompt = ChatPromptTemplate.from_template(prompt_path.read_text())
    
    def analyze_logs(self, logs: list[dict]) -> IncidentState:
        
        return self.structured_llm.invoke(
            self.prompt.format(logs=logs)
        )