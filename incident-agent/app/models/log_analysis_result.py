from typing import Optional
from pydantic import BaseModel

class LogAnalysisResult(BaseModel):
    log_errors: list[str]
    affected_invoice_ids: list[str]
    incident_type: str
    resolution_type: str
    confidence: float
    log_summary: str
    suggested_user_actions: Optional[str] = None
    responsible_component: Optional[str] = None