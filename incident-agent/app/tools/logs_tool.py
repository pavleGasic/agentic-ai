import requests
from langchain_core.tools import tool
from app.config import INVOICE_API_URL

@tool("fetch_logs")
def fetch_logs(invoice_id: str = None, batch_upload_id: str = None) -> list[dict]:
    """
    Fetch raw processing logs for analysis agent.
    """
    logs = []
    
    if batch_upload_id:
        url = f"{INVOICE_API_URL}/logs/batch/{batch_upload_id}"
        response = requests.get(url, timeout=10)
        if response.ok:
            logs.extend(response.json())
            
    if invoice_id:
        url = f"{INVOICE_API_URL}/logs/invoice/{invoice_id}"
        response = requests.get(url, timeout=10)
        if response.ok:
            logs.extend(response.json())
    
    return logs
