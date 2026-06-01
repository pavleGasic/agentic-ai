from pathlib import Path
from datetime import datetime, timezone

from langchain_core.messages import HumanMessage, SystemMessage
from langchain_groq import ChatGroq

from app.config import GROQ_API_KEY, MODEL_NAME, TEMPERATURE
from app.models.incident_analysis_result import IncidentAnalysisResult


class IncidentAnalysisAgent:

    def __init__(self):
        llm = ChatGroq(api_key=GROQ_API_KEY, model=MODEL_NAME, temperature=TEMPERATURE)
        self.structured_llm = llm.with_structured_output(IncidentAnalysisResult)

        prompt_path = (
            Path(__file__).parent.parent
            / "prompts"
            / "incident_analysis_prompt.txt"
        )
        self.system_prompt = prompt_path.read_text()

    def analyze_incident(self, state) -> IncidentAnalysisResult:
        now = datetime.now(timezone.utc)
        time_context = (
            f"Current time (UTC): {now.isoformat()}, "
            f"date: {now.date().isoformat()}, "
            f"weekday: {now.weekday()}"
        )

        system_message = SystemMessage(content=f"{self.system_prompt}\n\n{time_context}")
        user_message = HumanMessage(content=(
            f"Incident title:\n{state.get('incident_title', '')}\n\n"
            f"Incident description:\n{state.get('incident_description', '')}"
        ))

        return self.structured_llm.invoke([system_message, user_message])
