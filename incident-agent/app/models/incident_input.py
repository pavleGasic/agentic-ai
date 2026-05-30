from pydantic import BaseModel

class IncidentInput(BaseModel):
    user_message: str
    logs: str