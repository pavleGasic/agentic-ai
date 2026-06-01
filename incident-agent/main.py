import uvicorn
from app.network.api import app
from app.rag.retriever import get_relevant_code

def main():
    query = "ProcessingService java.lang.IllegalArgumentException: Invoice issue date is too old for processing: 2028-06-15"
        
    code_chunks = get_relevant_code(query)
    code_context = "\n\n".join(code_chunks)
    
    print("Retrieved code context:")
    print(code_context)

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
    
# if __name__ == "__main__":
#     main()