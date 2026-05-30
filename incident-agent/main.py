from app.agents.classification_agent import ClassificationAgent
from app.models.incident_input import IncidentInput

def main():
    
    agent = ClassificationAgent()
    
    incident = IncidentInput(
        user_message="The application crashes when I try to upload a invoice file.",
        logs="2024-06-01 12:00:00 ERROR UploadService - java.lang.NullPointerException at InvoiceMapper.map(InvoiceMapper.java:42)"
    )
    
    result = agent.classify_incident(incident)
    
    print()
    print(f"Incident Type: {result.incident_type}")
    print(f"Resolution Type: {result.resolution_type}")
    print(f"Confidence: {result.confidence}")
    print(f"Reasoning: {result.reasoning}")
        
if __name__ == "__main__":
    main()