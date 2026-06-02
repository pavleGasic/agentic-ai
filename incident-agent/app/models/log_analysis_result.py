from typing import Optional, Literal
from pydantic import BaseModel

class LogAnalysisResult(BaseModel):
    resolution_type: Literal["USER", "DEVELOPER"]
    confidence: float
    suggested_user_actions: Optional[str] = None
    responsible_component: Optional[str] = None
    responsible_method: Optional[str] = None