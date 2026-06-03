from typing import Optional
from pydantic import BaseModel

class CodeAnalysisResult(BaseModel):
    responsible_components: Optional[list[str]] = None
    responsible_methods: Optional[list[str]] = None
    error_types: Optional[list[str]] = None
    fix_suggestion: str
    reasoning: str