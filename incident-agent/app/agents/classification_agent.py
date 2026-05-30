from pathlib import Path
from langchain_core.prompts import ChatPromptTemplate
from langchain_groq import ChatGroq
from app.config import (GROQ_API_KEY, MODEL_NAME, TEMPERATURE)
from app.models.incident_input import IncidentInput
from app.models.classification_result import ClassificationResult

class ClassificationAgent:
    
    def __init__(self):
        self.llm = ChatGroq(
            api_key=GROQ_API_KEY,
            model=MODEL_NAME,
            temperature=TEMPERATURE
        )
        
        self.structured_llm = (
            self.llm.with_structured_output(ClassificationResult)
        )
        
        prompt_path = (
            Path(__file__)
            .parent.parent
            / "prompts"
            / "classification_prompt.txt"
        )
        
        self.prompt = ChatPromptTemplate.from_template(
            prompt_path.read_text()
        )
    
    def classify_incident(self, incident: IncidentInput) -> ClassificationResult:
        incident_text = f"""
                User Message: {incident.user_message}
                Logs: {incident.logs}
            """
            
        chain = self.prompt | self.structured_llm
        
        response = chain.invoke({"incident": incident_text})
        return response