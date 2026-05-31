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
    resolution_type: str
    confidence: float
    classification_reasoning: Optional[str]
    suggested_user_action: Optional[str]
    
    responsible_component: Optional[str]
    error_type: Optional[str]
    code_analysis_reasoning: Optional[str]
    
    similar_incidents: Optional[list[dict]]
    history_search_reasoning: Optional[str]
    
    final_report: Optional[str]
