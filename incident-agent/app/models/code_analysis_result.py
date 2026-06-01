from typing import Optional
from pydantic import BaseModel

class CodeAnalysisResult(BaseModel):
    responsible_component: str
    responsible_method: str
    error_type: str
    fix_suggestion: str
    reasoning: str