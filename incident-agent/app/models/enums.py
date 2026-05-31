from enum import Enum

class IncidentType(str, Enum):
    USER_ERROR = "USER_ERROR"
    BACKEND_ERROR = "BACKEND_ERROR"
    UNKNOWN_ERROR = "UNKNOWN_ERROR"
    
class ResolutionType(str, Enum):
    USER = "USER"
    DEVELOPER = "DEVELOPER"