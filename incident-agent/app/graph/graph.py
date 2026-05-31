from langgraph.graph import StateGraph, END

from app.graph.state import IncidentState
from app.tools.logs_tool import fetch_logs
from app.agents.log_analysis_agent import LogAnalysisAgent

log_agent = LogAnalysisAgent()

def fetch_logs_node(state: IncidentState):
    logs = fetch_logs.invoke({
        "invoice_id": state.get("invoice_id", ""),
        "batch_upload_id": state.get("batch_id", "")
    })
    
    return {"logs": logs}

def log_analysis_node(state: IncidentState):
    return log_agent.analyze_logs(state["logs"])
    
def route_node(state: IncidentState):
    if state["resolution_type"].upper() == "USER":
        return "USER"
    else:
        return "DEVELOPER"
    
def user_resolution_node(state: IncidentState):
    return {
        "final_report": f"User resolution recommended with confidence {state['confidence']:.2f}. Suggested action: {state.get('suggested_user_action', 'N/A')}"
    }
    
def developer_node(state: IncidentState):
    return {
        "final_report": f"Developer investigation recommended with confidence {state['confidence']:.2f}. Responsible component: {state.get('responsible_component', 'N/A')}, Error type: {state.get('error_type', 'N/A')}"
    }
    
def build_graph():
    graph = StateGraph(IncidentState)
    
    graph.add_node("fetch_logs", fetch_logs_node)
    graph.add_node("log_analysis", log_analysis_node)
    graph.add_node("user_resolution", user_resolution_node)
    graph.add_node("developer_investigation", developer_node)
    
    graph.set_entry_point("fetch_logs")
    
    graph.add_edge("fetch_logs", "log_analysis")
    
    graph.add_conditional_edges(
        "log_analysis",
        route_node,
        {
            "USER": "user_resolution",
            "DEVELOPER": "developer_investigation"
        }
    )

    graph.add_edge("user_resolution", END)
    graph.add_edge("developer_investigation", END)
    
    return graph.compile()