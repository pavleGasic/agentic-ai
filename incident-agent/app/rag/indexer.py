from langchain_community.document_loaders import TextLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.vectorstores import Chroma
import os, glob

JAVA_SRC = os.path.join(os.path.dirname(__file__), "../../../invoice-management-server/src/main/java")
CHROMA_DIR = os.path.join(os.path.dirname(__file__), "chroma_db")

def build_index():
    java_files = glob.glob(f"{JAVA_SRC}/**/*.java", recursive=True)
    docs = []
    for path in java_files:
        loader = TextLoader(path)
        loaded = loader.load()
        for doc in loaded:
            doc.metadata["filename"] = os.path.basename(path)
        docs.extend(loaded)
        
    text_splitter = RecursiveCharacterTextSplitter(chunk_size=1000, chunk_overlap=200)
    chunks = text_splitter.split_documents(docs)
    
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
    Chroma.from_documents(chunks, embeddings, persist_directory=CHROMA_DIR)
    print(f"Indexed {len(chunks)} chunks from {len(java_files)} Java files.")
    
if __name__ == "__main__":
    build_index()