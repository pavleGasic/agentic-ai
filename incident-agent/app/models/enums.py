from enum import Enum

class IncidentType(str, Enum):
    USER_ERROR = "user_error"
    BACKEND_ERROR = "backend_error"
    UNKNOWN_ERROR = "unknown_error"
    
class ResolutionType(str, Enum):
    USER = "user"
    DEVELOPER = "developer"