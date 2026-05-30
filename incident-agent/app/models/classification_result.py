from pydantic import BaseModel
from app.models.enums import ( 
    IncidentType,
    ResolutionType)

class ClassificationResult(BaseModel):
    incident_type: IncidentType
    resolution_type: ResolutionType
    confidence: float
    reasoning: str