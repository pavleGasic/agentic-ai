from typing import Optional, Literal
from pydantic import BaseModel

class LogAnalysisResult(BaseModel):
    log_errors: list[str]
    affected_invoice_ids: list[str]
    incident_type: Literal["USER_ERROR", "DEVELOPER_ERROR", "UNKNOWN"]
    resolution_type: Literal["USER", "DEVELOPER"]
    confidence: float
    log_summary: str
    suggested_user_actions: Optional[str] = None
    responsible_component: Optional[str] = None
    responsible_method: Optional[str] = None