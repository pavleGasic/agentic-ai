from typing import Optional
from pydantic import BaseModel
from datetime import datetime

class IncidentAnalysisResult(BaseModel):
    business_context: Optional[str]
    start_time: Optional[datetime]
    end_time: Optional[datetime]