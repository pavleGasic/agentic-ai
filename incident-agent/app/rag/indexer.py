import glob
import os
from langchain_core.documents import Document
from langchain_community.vectorstores import Chroma
from langchain_community.embeddings import HuggingFaceEmbeddings

from app.rag.parser import parser, get_class_name, extract_methods, detect_layer

JAVA_SRC = "../invoice-management-server/src/main/java/com/master/invoicemanagementserver"
CHROMA_DIR = os.path.join(os.path.dirname(__file__), "chroma_db")

def to_document(method: dict, file_path: str, layer: str) -> Document:
    content = (
        f"Class: {method['class_name']}\n"
        f"Method: {method['method_name']}\n"
        f"Layer: {layer}\n"
        f"{method['source']}"
    )
    
    return Document(
        page_content=content,
        metadata={
            "class_name": method["class_name"],
            "method_name": method["method_name"],
            "file_path": file_path,
            "layer": layer
        }
    )
    
def build_index():
    java_files = glob.glob(f"{JAVA_SRC}/**/*.java", recursive=True)
    docs = []
    
    print(f"Starting to build the index... {JAVA_SRC}")
    
    print(f"Found {len(java_files)} Java files. Extracting methods...")
    
    for path in java_files:
        source = open(path, "rb").read()
        tree = parser.parse(source)
        root = tree.root_node
        class_name = get_class_name(root, source)
        layer = detect_layer(path)
        methods = extract_methods(root, source, class_name)
        
        for method in methods:
            docs.append(to_document(method, path, layer))
            
    print(f"Extracted {len(docs)} methods from {len(java_files)} files.")
            
    embeddings = HuggingFaceEmbeddings(model_name="all-MiniLM-L6-v2")
    Chroma.from_documents(docs, embeddings, persist_directory=CHROMA_DIR)
    
if __name__ == "__main__":
    build_index()