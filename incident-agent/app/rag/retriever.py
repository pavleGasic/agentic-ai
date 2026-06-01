from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma
import os

CHROMA_DIR = os.path.join(os.path.dirname(__file__), "chroma_db")

def get_relevant_code(query: str, k: int = 5):
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
    vectorstore = Chroma(persist_directory=CHROMA_DIR, embedding_function=embeddings)
    results = vectorstore.similarity_search(query, k=k)
    return [f"{doc.metadata['filename']}: {doc.page_content}" for doc in results]