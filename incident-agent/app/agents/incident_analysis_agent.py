from pathlib import Path

from langchain_groq import ChatGroq
from langchain.agents import create_agent

from app.config import GROQ_API_KEY, MODEL_NAME, TEMPERATURE
from app.tools.datetime_tool import get_time_context
from app.models.incident_analysis_result import IncidentAnalysisResult

class IncidentAnalysisAgent:

    def __init__(self):

        self.llm = ChatGroq(
            api_key=GROQ_API_KEY,
            model=MODEL_NAME,
            temperature=TEMPERATURE
        )

        self.tools = [get_time_context]

        prompt_path = (
            Path(__file__).parent.parent
            / "prompts"
            / "incident_analysis_prompt.txt"
        )

        self.system_prompt = prompt_path.read_text()

        self.agent = create_agent(
            model=self.llm,
            tools=self.tools,
            system_prompt=self.system_prompt,
            response_format=IncidentAnalysisResult
        )

    def analyze_incident(self, state) -> IncidentAnalysisResult:

        result = self.agent.invoke({
            "messages": [
                (
                    "user",
                    f"""
                    Incident title:
                    {state.get("incident_title","")}

                    Incident description:
                    {state.get("incident_description","")}
                    """
                )
            ]
        })

        return result