import requests
from app.config import INCIDENT_API_URL, INCIDENT_API_USER, INCIDENT_API_PASSWORD

def _get_token() -> str:
    response = requests.post(
        f"{INCIDENT_API_URL}/api/auth/login",
        json={"username": INCIDENT_API_USER, "password": INCIDENT_API_PASSWORD},
        timeout=10
    )
    response.raise_for_status()
    return response.json().get("token")

def post_comment(incident_id: str, content: str, visibility: str = "PUBLIC") -> None:
    token = _get_token()
    requests.post(
        f"{INCIDENT_API_URL}/api/incidents/{incident_id}/messages",
        json={"content": content, "visibility": visibility},
        headers={"Authorization": f"Bearer {token}"},
        timeout=10
    )
    
