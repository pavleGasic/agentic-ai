from pathlib import Path
import tree_sitter_java as tsjava
from tree_sitter import Language, Parser

JAVA_LANGUAGE = Language(tsjava.language())
parser = Parser(JAVA_LANGUAGE)

def get_class_name(root, source: bytes) -> str:
    for node in root.children:
        if node.type == "class_declaration":
            for child in node.children:
                if child.type == "identifier":
                    return source[child.start_byte:child.end_byte].decode()
    return "Unknown"

def extract_methods(root, source: bytes, class_name: str) -> list[dict]:
    methods = []
    
    for node in root.children:
        if node.type == "class_declaration":
            for child in node.children:
                if child.type == "class_body":
                    for member in child.children:
                        if member.type == "method_declaration":
                            method = extract_method(member, source, class_name)
                            methods.append(method)
    return methods

def extract_method(node, source: bytes, class_name: str) -> dict:
    method_name = ""
    parameters = ""
    
    for child in node.children:
        if child.type == "identifier":
            method_name = source[child.start_byte:child.end_byte].decode()
        elif child.type == "formal_parameters":
            parameters = source[child.start_byte:child.end_byte].decode()
            
    method_source = source[node.start_byte:node.end_byte].decode()
    
    return {
        "class_name": class_name,
        "method_name": method_name,
        "parameters": parameters,
        "source": method_source
    }
    
def detect_layer(file_path: str) -> str:
    parts = Path(file_path).parts
    for layer in ("service", "controller", "repository", "entity", "util", "dto", "config", "exception"):
        if layer in parts:
            return layer
    return "other"