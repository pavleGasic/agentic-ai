from typing import TypedDict
from typing import List, Optional

class IncidentState(TypedDict):
    incident_title: str
    incident_description: str
    batch_id: str | None
    
    logs: Optional[List[dict]]
    log_errors: Optional[List[str]]
    affected_invoice_ids: Optional[List[str]]
    logs_summary: Optional[str]
    incident_type: Optional[str]
    resolution_type: Optional[str]
    confidence: Optional[float]
    classification_reasoning: Optional[str]
    responsible_component: Optional[str]
    responsible_method: Optional[str]
    suggested_user_actions: Optional[str] = None

    
    error_type: Optional[str]
    fix_suggestion: Optional[str]
    code_analysis_reasoning: Optional[str]
    
    similar_incidents: Optional[list[dict]]
    history_search_reasoning: Optional[str]
    
    final_report: Optional[str]
    final_report_visibility: Optional[str]
