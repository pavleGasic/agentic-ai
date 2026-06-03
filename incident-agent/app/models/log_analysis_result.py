from typing import Optional, Literal
from pydantic import BaseModel

class LogAnalysisResult(BaseModel):
    resolution_type: Literal["USER", "DEVELOPER"]
    confidence: float
    suggested_user_actions: Optional[str] = None
    responsible_components: Optional[list[str]] = None
    responsible_methods: Optional[list[str]] = None