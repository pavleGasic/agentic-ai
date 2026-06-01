from fastapi import FastAPI
from pydantic import BaseModel
import asyncio
import app.config  # ensures LangSmith env vars are set before graph runs
from app.network.incident_client import post_comment
from app.graph.state import IncidentState
from app.graph.graph import build_graph

app = FastAPI()

class IncidentRequest(BaseModel):
    incident_id: str
    title: str
    description: str
    batch_id: str = None
    
@app.post("/agent/process")
async def process_incident(request: IncidentRequest):
    asyncio.create_task(_run_agent(request))
    return {"status": "accepted", "incident_id": request.incident_id}

async def _run_agent(request: IncidentRequest):
    graph = build_graph()
    result = graph.invoke({
        "incident_title": request.title,
        "incident_description": request.description,
        "batch_id": request.batch_id,
    })
    visibility = result.get("final_report_visibility", "PUBLIC")
    post_comment(request.incident_id, result["final_report"], visibility)
