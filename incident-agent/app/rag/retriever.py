import os
from langchain_community.vectorstores import Chroma
from langchain_community.embeddings import HuggingFaceEmbeddings

CHROMA_DIR = os.path.join(os.path.dirname(__file__), "chroma_db")
def get_relevant_code(query: str, class_name: str = None, k: int = 5) -> list[str]:
    where = {"class_name": class_name} if class_name else None
    
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
    vectorstore = Chroma(persist_directory=CHROMA_DIR, embedding_function=embeddings)
    
    results = vectorstore.similarity_search(query, k=k, filter=where)
    return [doc.page_content for doc in results]