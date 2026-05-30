from typing import TypedDict

class IncidentState(TypedDict):
    user_message: str
    logs: str
    classification: str
    resolution_type: str
    confidence: float
    root_cause: str