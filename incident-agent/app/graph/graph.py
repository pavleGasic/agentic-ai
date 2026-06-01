from langgraph.graph import StateGraph, END

from app.graph.state import IncidentState
from app.models.code_analysis_result import CodeAnalysisResult
from app.tools.logs_tool import fetch_logs
from app.agents.log_analysis_agent import LogAnalysisAgent
import logging
from langgraph.graph import StateGraph, END

from app.graph.state import IncidentState
from app.models.code_analysis_result import CodeAnalysisResult
from app.tools.logs_tool import fetch_logs
from app.agents.log_analysis_agent import LogAnalysisAgent
from app.agents.code_agent import CodeAgent
from app.models.log_analysis_result import LogAnalysisResult


# Logger setup: only configure basic logging if no handlers are present
logger = logging.getLogger(__name__)
if not logging.getLogger().handlers:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s [%(name)s] %(message)s"
    )

log_agent = LogAnalysisAgent()
code_agent = CodeAgent()


def fetch_logs_node(state: IncidentState):
    invoice_id = state.get("invoice_id", "")
    batch_id = state.get("batch_id", "")
    logger.info("fetch_logs_node start: invoice_id=%s batch_id=%s", invoice_id, batch_id)

    try:
        logs = fetch_logs.invoke({
            "invoice_id": invoice_id,
            "batch_upload_id": batch_id
        })
    except Exception as e:
        logger.exception("fetch_logs failed: %s", e)
        logs = []

    if not logs:
        logger.info("fetch_logs_node: no logs found")
        return {
            "logs": [],
            "resolution_type": "DEVELOPER",
            "incident_type": "UNKNOWN"
        }

    logger.info("fetch_logs_node: retrieved %d logs", len(logs))
    return {"logs": logs}


def log_analysis_node(state: IncidentState):
    logger.info("log_analysis_node start: logs_count=%s", len(state.get("logs") or []))
    try:
        result: LogAnalysisResult = log_agent.analyze_logs(state.get("logs", []))
    except Exception as e:
        logger.exception("log_analysis failed: %s", e)
        # return a conservative default
        return {
            "log_errors": [],
            "affected_invoice_ids": [],
            "log_summary": "",
            "incident_type": "UNKNOWN",
            "resolution_type": "DEVELOPER",
            "confidence": 0.0,
            "suggested_user_actions": None,
            "responsible_component": None,
            "responsible_method": None,
        }

    out = {
        "log_errors": result.log_errors,
        "affected_invoice_ids": result.affected_invoice_ids,
        "log_summary": result.log_summary,
        "incident_type": result.incident_type,
        "resolution_type": result.resolution_type,
        "confidence": result.confidence,
        "suggested_user_actions": result.suggested_user_actions,
        "responsible_component": result.responsible_component,
        "responsible_method": result.responsible_method
    }

    logger.info(
        "log_analysis_node result: incident_type=%s resolution=%s confidence=%.2f errors=%s",
        out.get("incident_type"), out.get("resolution_type"), out.get("confidence") or 0.0, bool(out.get("log_errors"))
    )
    return out


def route_node(state: IncidentState):
    resolution = (state.get("resolution_type") or "DEVELOPER").upper()
    logger.info("route_node: resolution_type=%s", resolution)
    if resolution == "USER":
        logger.info("route_node -> USER")
        return "USER"
    else:
        logger.info("route_node -> DEVELOPER")
        return "DEVELOPER"


def user_resolution_node(state: IncidentState):
    suggested = state.get('suggested_user_actions', 'N/A')
    logger.info("user_resolution_node: suggested_actions=%s", suggested)
    return {
        "final_report": f"User resolution recommended.\nSuggested action:\n{suggested}"
    }


def analyze_code_node(state: IncidentState):
    logger.info("analyze_code_node start: affected_invoices=%s", state.get("affected_invoice_ids"))
    try:
        result: CodeAnalysisResult = code_agent.analyze_code(state)
    except Exception as e:
        logger.exception("analyze_code failed: %s", e)
        return {
            "responsible_component": None,
            "responsible_method": None,
            "error_type": None,
            "code_analysis_reasoning": "",
            "fix_suggestion": None
        }

    out = {
        "responsible_component": result.responsible_component,
        "responsible_method": result.responsible_method,
        "error_type": result.error_type,
        "code_analysis_reasoning": result.reasoning,
        "fix_suggestion": result.fix_suggestion
    }

    logger.info(
        "analyze_code_node result: component=%s method=%s error_type=%s",
        out.get("responsible_component"), out.get("responsible_method"), out.get("error_type")
    )
    return out


def developer_node(state: IncidentState):
    logger.info("developer_node: confidence=%s component=%s error_type=%s", state.get('confidence'), state.get('responsible_component'), state.get('error_type'))
    return {
        "final_report": f"Developer investigation recommended with confidence {state.get('confidence', 0.0):.2f}. Responsible component: {state.get('responsible_component', 'N/A')}, Error type: {state.get('error_type', 'N/A')}"
    }


def build_graph():
    logger.info("build_graph: constructing StateGraph")
    graph = StateGraph(IncidentState)

    graph.add_node("fetch_logs", fetch_logs_node)
    graph.add_node("log_analysis", log_analysis_node)
    graph.add_node("analyze_code", analyze_code_node)
    graph.add_node("user_resolution", user_resolution_node)
    graph.add_node("developer_investigation", developer_node)

    graph.set_entry_point("fetch_logs")

    graph.add_edge("fetch_logs", "log_analysis")

    graph.add_conditional_edges(
        "log_analysis",
        route_node,
        {
            "USER": "user_resolution",
            "DEVELOPER": "analyze_code"
        }
    )

    graph.add_edge("user_resolution", END)
    graph.add_edge("analyze_code", "developer_investigation")
    graph.add_edge("developer_investigation", END)

    logger.info("build_graph: compiling graph")
    compiled = graph.compile()
    logger.info("build_graph: graph compiled successfully")
    return compiled