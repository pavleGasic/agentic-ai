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