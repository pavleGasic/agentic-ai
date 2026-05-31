import uvicorn
from app.network.api import app
from app.tools.logs_tool import fetch_logs
from app.agents.log_analysis_agent import LogAnalysisAgent
        
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)