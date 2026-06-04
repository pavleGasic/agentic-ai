import requests
from typing import Optional
from datetime import datetime
from langchain_core.tools import tool
from app.config import INVOICE_API_URL

@tool("fetch_logs")
def fetch_logs(keyword: str = None, startDate: Optional[datetime] = None, endDate: Optional[datetime] = None) -> list[dict]:
    """
    Fetch raw processing logs for log analysis agent.
    """
    logs = []
    
    response = requests.get(
        f"{INVOICE_API_URL}/logs",
        params={
            "keyword": keyword,
            "startDate": startDate.isoformat() if startDate else None,
            "endDate": endDate.isoformat() if endDate else None,
        },
        timeout=10
    )
    
    logs.extend(response.json())
    
    return logs
