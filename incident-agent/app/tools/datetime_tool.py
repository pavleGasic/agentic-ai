from datetime import datetime, timezone
from langchain_core.tools import tool

@tool
def get_time_context() -> dict:
    """
    Returns current time context for incident analysis.
    """
    now = datetime.now(timezone.utc)

    return {
        "now": now.isoformat(),
        "date": now.date().isoformat(),
        "year": now.year,
        "month": now.month,
        "day": now.day,
        "weekday": now.weekday()
    }