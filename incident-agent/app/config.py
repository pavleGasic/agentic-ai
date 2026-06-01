import os
from dotenv import load_dotenv

load_dotenv()

GROQ_API_KEY = os.getenv("GROQ_API_KEY")
MODEL_NAME = "llama-3.3-70b-versatile"
TEMPERATURE = 0.1
INVOICE_API_URL = os.getenv("INVOICE_API_URL", "http://localhost:8080")
INCIDENT_API_URL = os.getenv("INCIDENT_API_URL", "http://localhost:8081")
INCIDENT_API_USER = os.getenv("INCIDENT_API_USER", "admin")
INCIDENT_API_PASSWORD = os.getenv("INCIDENT_API_PASSWORD")

os.environ.setdefault("LANGSMITH_TRACING", os.getenv("LANGCHAIN_TRACING_V2", "false"))
os.environ.setdefault("LANGSMITH_ENDPOINT", os.getenv("LANGCHAIN_ENDPOINT", "https://api.smith.langchain.com"))
os.environ.setdefault("LANGSMITH_API_KEY", os.getenv("LANGCHAIN_API_KEY", ""))
os.environ.setdefault("LANGSMITH_PROJECT", os.getenv("LANGCHAIN_PROJECT", "incident-agent"))