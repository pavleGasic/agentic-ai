from pathlib import Path

from langchain_core.prompts import ChatPromptTemplate
from langchain_core.messages import HumanMessage, SystemMessage
from langchain_groq import ChatGroq

from app.config import (GROQ_API_KEY, MODEL_NAME, TEMPERATURE)
from app.models.log_analysis_result import LogAnalysisResult

class LogAnalysisAgent:
    
    def __init__(self):
        self.llm = ChatGroq(api_key=GROQ_API_KEY, model=MODEL_NAME, temperature=TEMPERATURE)
        self.structured_llm = (self.llm.with_structured_output(LogAnalysisResult))
        
        prompt_path = (
            Path(__file__)
            .parent.parent
            / "prompts"
            / "log_analysis_prompt.txt"
        )
        
        self.prompt = prompt_path.read_text()
    
    def analyze_logs(self, logs: list[dict]) -> LogAnalysisResult:
        logs_message = HumanMessage(content=f"Logs for analysis: {logs}")
        system_message = SystemMessage(content=self.prompt)
        
        return self.structured_llm.invoke(
            [system_message, logs_message]
        )