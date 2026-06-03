from typing import TypedDict, List, Optional, Literal
from datetime import datetime 

class IncidentState(TypedDict):
    incident_title: str
    incident_description: str
    
    incident_analysis_status: Literal["SUCCESS", "FAILURE", "INSUFFICIENT_DATA"]
    business_context: Optional[str]
    start_time: Optional[datetime]
    end_time: Optional[datetime]
    
    logs: Optional[List[dict]]
    resolution_type: Literal["USER", "DEVELOPER"]
    confidence: Optional[float]
    classification_reasoning: Optional[str]
    responsible_components: Optional[list[str]]
    responsible_methods: Optional[list[str]]
    suggested_user_actions: Optional[str] = None

    error_type: Optional[str]
    fix_suggestion: Optional[str]
    code_analysis_reasoning: Optional[str]
    
    similar_incidents: Optional[list[dict]]
    history_search_reasoning: Optional[str]
    
    final_report: Optional[str]
    final_report_visibility: Optional[str]
