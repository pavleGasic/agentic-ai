import json
import re
from pathlib import Path
import logging

from langchain_groq import ChatGroq
from langchain_core.messages import HumanMessage, SystemMessage
from langgraph.prebuilt import create_react_agent

from app.config import (GROQ_API_KEY, MODEL_NAME, TEMPERATURE)
from app.models.code_analysis_result import CodeAnalysisResult
from app.graph.state import IncidentState
from app.tools.code_tool import search_code
from app.tools.business_knowledge_tool import search_business_knowledge

MAX_ITERATIONS = 5


logger = logging.getLogger(__name__)
if not logging.getLogger().handlers:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s [%(name)s] %(message)s"
    )
    
class CodeAgent:
    def __init__(self):
        llm = ChatGroq(api_key=GROQ_API_KEY, model=MODEL_NAME, temperature=TEMPERATURE)
        prompt_path = (
            Path(__file__).parent.parent
            / "prompts"
            / "code_analysis_prompt.txt"
        )
        self.system_prompt = prompt_path.read_text()
        
        self.agent = create_react_agent(llm, tools=[search_code, search_business_knowledge])
        
    def analyze_code(self, state: IncidentState) -> CodeAnalysisResult:
        logs = state.get("logs") or []
        logs_text = "\n".join(
            f"[{log.get('level','')}] {log.get('module','')} - {log.get('message','')} {log.get('stack_trace','')}"
            for log in logs
        )
        
        human_message = (
            f"Logs:\n{logs_text}\n\n"
            f"Incident title: {state.get('incident_title', '')}\n"
            f"Incident description: {state.get('incident_description', '')}\n"
            f"Suspected components: {state.get('suspected_components', [])}\n"
            f"Suspected methods: {state.get('suspected_methods', [])}\n\n"
            "Use the available tools iteratively to gather enough context, "
            "then produce the final JSON answer."
        )
        
        try:
            result = self.agent.invoke(
                {
                    "messages": [
                        SystemMessage(content=self.system_prompt),
                        HumanMessage(content=human_message)
                    ],
                },
                config={"recursion_limit": MAX_ITERATIONS}
            )
            
            logger.info("CodeAgent raw result: %s", result)
            
            last_content = result["messages"][-1].content
            
            logger.info("CodeAgent analyze_code result: %s", last_content)
            
            data = json.loads(last_content)
            return CodeAnalysisResult(**data)
        
        except Exception as e:
            return CodeAnalysisResult(
                error_types=[str(e)],
                responsible_components=state.get("responsible_components", []),
                responsible_methods=state.get("responsible_methods", []),
                fix_suggestion="Failed to analyze code. Please check the logs and suspected components/methods for more details.",
                reasoning=f"An error occurred during code analysis: {str(e)}"
            )