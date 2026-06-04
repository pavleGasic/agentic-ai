from app.rag.retriever import get_relevant_code

def test_retriever():
    query = "error occurred during earnings calculation"
    results = get_relevant_code(query)
    
    for r in results:
        print(r)
        
if __name__ == "__main__":
    test_retriever()