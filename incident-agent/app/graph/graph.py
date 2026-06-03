from langgraph import graph
from langgraph.graph import StateGraph, END

import logging
from app.graph.state import IncidentState
from app.agents.incident_analysis_agent import IncidentAnalysisAgent
from app.models.incident_analysis_result import IncidentAnalysisResult
from app.agents.log_analysis_agent import LogAnalysisAgent
from app.models.log_analysis_result import LogAnalysisResult
from app.tools.logs_tool import fetch_logs
from app.agents.code_agent import CodeAgent
from app.models.code_analysis_result import CodeAnalysisResult


logger = logging.getLogger(__name__)
if not logging.getLogger().handlers:
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s [%(name)s] %(message)s"
    )

incident_agent = IncidentAnalysisAgent()
log_agent = LogAnalysisAgent()
code_agent = CodeAgent()

def incident_analysis_node(state: IncidentState):
    logger.info("incident_analysis_node start: incident_title=%s", state.get("incident_title"))
    result: IncidentAnalysisResult = incident_agent.analyze_incident(state)
    
    out = {
        "business_context": result.business_context,
        "start_time": result.start_time,
        "end_time": result.end_time,
        "incident_analysis_status": result.incident_analysis_status
    }
    
    logger.info(
        "incident_analysis_node result: business_context=%s start_time=%s end_time=%s incident_analysis_status=%s",
        out["business_context"],
        out["start_time"],
        out["end_time"],
        out["incident_analysis_status"]
    )
    
    return out

def route_after_incident_analysis_node(state: IncidentState):
    status = (state.get("incident_analysis_status") or "INSUFFICIENT_DATA").upper()
    logger.info("route_after_incident_analysis_node: incident_analysis_status=%s", status)
    if status == "SUCCESS":
        logger.info("route_after_incident_analysis_node -> SUCCESS")
        return "SUCCESS"
    elif status == "INSUFFICIENT_DATA":
        logger.info("route_after_incident_analysis_node -> INSUFFICIENT_DATA")
        return "INSUFFICIENT_DATA"
    else:   
        logger.info("route_after_incident_analysis_node -> FAILURE")
        return "FAILURE"
    
    
def incident_analysis_failure_node(state: IncidentState):
    logger.info("incident_analysis_failure_node: incident_analysis_status=%s", state.get("incident_analysis_status"))
    if state.get("incident_analysis_status") == "FAILURE":
        return {
            "final_report": "Incident analysis failed. The incident analysis agent was not able to analyze the incident successfully.",
            "final_report_visibility": "PUBLIC"
        }
    return {
        "final_report": "Incident analysis failed. Insufficient data to analyze the incident.",
        "final_report_visibility": "PUBLIC"
    }

def fetch_logs_node(state: IncidentState):
    logger.info("fetch_logs_node start: incident_title=%s with filters: keyword=%s, from=%s, to=%s", state.get("incident_title"), state.get("keyword"), state.get("startDate"), state.get("endDate"))
    logs = fetch_logs.invoke({
            "keyword": state.get("business_context"),
            "startDate": state.get("start_time"),
            "endDate": state.get("end_time"),
        })
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
    result: LogAnalysisResult = log_agent.analyze_logs(state.get("logs", []))

    out = {
        "resolution_type": result.resolution_type,
        "confidence": result.confidence,
        "responsible_components": result.responsible_components,
        "responsible_methods": result.responsible_methods,
        "suggested_user_actions": result.suggested_user_actions,
    }

    logger.info(
        "log_analysis_node result: incident_type=%s resolution=%s confidence=%.2f errors=%s",
        out.get("incident_type"), out.get("resolution_type"), out.get("confidence") or 0.0, bool(out.get("log_errors"))
    )
    return out


def route_after_log_analysis_node(state: IncidentState):
    resolution = (state.get("resolution_type") or "DEVELOPER").upper()
    logger.info("route_after_log_analysis_node: resolution_type=%s", resolution)
    if resolution == "USER":
        logger.info("route_after_log_analysis_node -> USER")
        return "USER"
    else:
        logger.info("route_after_log_analysis_node -> DEVELOPER")
        return "DEVELOPER"


def user_resolution_node(state: IncidentState):
    suggested = state.get('suggested_user_actions', 'N/A')
    logger.info("user_resolution_node: suggested_actions=%s", suggested)
    return {
        "final_report": f"Suggested action:\n{suggested}",
        "final_report_visibility": "PUBLIC"
    }


def analyze_code_node(state: IncidentState):
    logger.info("analyze_code_node start: suspected_components=%s suspected_methods=%s logs_count=%s",
                state.get("suspected_components"), state.get("suspected_methods"), len(state.get("logs") or [])) 
    
    result: CodeAnalysisResult = code_agent.analyze_code(state)
    
    out = {
        "responsible_components": result.responsible_components,
        "responsible_methods": result.responsible_methods,
        "error_types": result.error_types,
        "code_analysis_reasoning": result.reasoning,
        "fix_suggestion": result.fix_suggestion
    }

    logger.info(
        "analyze_code_node result: components=%s methods=%s error_types=%s",
        out.get("responsible_components"), out.get("responsible_methods"), out.get("error_types")
    )
    return out


def developer_node(state: IncidentState):
    logger.info("developer_node: confidence=%s components=%s error_types=%s", state.get('confidence'), state.get('responsible_components'), state.get('error_types'))
    return {
        "final_report": f"Developer resolution recommended.\nResponsible components: {state.get('responsible_components')}\nResponsible methods: {state.get('responsible_methods')}\nError types: {state.get('error_types')}\nFix suggestion: {state.get('fix_suggestion')}\nReasoning: {state.get('code_analysis_reasoning')}",
        "final_report_visibility": "DEVELOPER_ONLY"
    }


def build_graph():
    logger.info("build_graph: constructing StateGraph")
    graph = StateGraph(IncidentState)
    
    graph.add_node("incident_analysis", incident_analysis_node)
    graph.add_node("incident_analysis_failure_node", incident_analysis_failure_node)
    graph.add_node("fetch_logs", fetch_logs_node)
    graph.add_node("log_analysis", log_analysis_node)
    graph.add_node("user_resolution_node", user_resolution_node)
    graph.add_node("analyze_code_node", analyze_code_node)
    graph.add_node("developer_node", developer_node)
    
    graph.set_entry_point("incident_analysis")
    graph.add_conditional_edges(
        "incident_analysis",
        route_after_incident_analysis_node,
        {
            "SUCCESS": "fetch_logs",
            "INSUFFICIENT_DATA": "incident_analysis_failure_node",
            "FAILURE": "incident_analysis_failure_node"
        }
    )    

    graph.add_edge("incident_analysis_failure_node", END)
    graph.add_edge("fetch_logs", "log_analysis")
    graph.add_conditional_edges(
        "log_analysis",
        route_after_log_analysis_node,
        {
            "USER": "user_resolution_node",
            "DEVELOPER": "analyze_code_node"
        }
    )
    graph.add_edge("user_resolution_node", END)
    graph.add_edge("analyze_code_node", "developer_node")
    graph.add_edge("developer_node", END)

    logger.info("build_graph: compiling graph")
    compiled = graph.compile()
    logger.info("build_graph: graph compiled successfully")
    return compiled