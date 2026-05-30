from app.agents.classification_agent import ClassificationAgent
from app.models.incident_input import IncidentInput

def test_validation_error():
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
    
    assert result.incident_type is not None
    assert result.resolution_type is not None
    assert result.confidence is not None
    assert result.reasoning is not None
    
def test_missing_fields():
    agent = ClassificationAgent()
    
    incident = IncidentInput(
        user_message="The application crashes when I try to upload a invoice file.",
        logs="2024-06-01 12:00:00 ERROR UploadService - Missing column 'invoice_date' in invoices.csv file"
    )
    
    result = agent.classify_incident(incident)
    
    print()
    print(f"Incident Type: {result.incident_type}")
    print(f"Resolution Type: {result.resolution_type}")
    print(f"Confidence: {result.confidence}")
    print(f"Reasoning: {result.reasoning}")
    
    assert result.incident_type is not None
    assert result.resolution_type is not None
    assert result.confidence is not None
    assert result.reasoning is not None