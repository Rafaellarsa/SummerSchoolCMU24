import os
import ast
import math
from collections import Counter, defaultdict
import numpy as np

MATCH_SCORE=3
GAP_COST=2
import javalang
from anytree import Node, RenderTree, PreOrderIter
from anytree.search import findall_by_attr
from anytree.walker import Walker

# List the functions we want to have in our parsing
function_string_list = ["public", "int", "=", "+", "-", "*", "/", "//", "&&", "||", "==", ">=", "<=", "<", ">",
                       "return", "true", "false", "else", "String", "!=", "+=", "-=", "*=", "/=",
                       "length", "substring", "indexOf", "startsWith", "endsWith", "StringBuilder", "contains",
                       "append", "for", "++", "chatAt", "equals", "toLowerCase", "lastIndexOf", ":",
                       "int[]", "new", "[]"]

# This code takes a node of parsed Java into token
def get_token(node):
    token = ''
    if isinstance(node, str):
        if not node.isnumeric() and (node not in function_string_list):
            token = "node"
        else:
            token = node
    elif isinstance(node, set):
        token = 'Modifier'  # node.pop()
    elif isinstance(node, javalang.ast.Node):
        token = node.__class__.__name__

    return token

# This code expands the java tree into
def get_children(root):
    if isinstance(root, javalang.ast.Node):
        children = root.children
    elif isinstance(root, set):
        children = list(root)
    else:
        children = []

    def expand(nested_list):
        for item in nested_list:
            if isinstance(item, list):
                for sub_item in expand(item):
                    yield sub_item
            elif item:
                yield item

    return list(expand(children))

# Recursively build "anytree" structure with the node.
def get_trees(current_node, parent_node, order):

    token, children = get_token(current_node), get_children(current_node)
    node = Node([order,token], parent=parent_node, order=order)

    for child_order in range(len(children)):
        get_trees(children[child_order], node, order+str(int(child_order)+1))

# Java parser of program
def program_parser(func):
    tokens = javalang.tokenizer.tokenize(func)
    parser = javalang.parser.Parser(tokens)
    tree = parser.parse_member_declaration()
    return tree

def parse_java(raw_code):
    java_code = program_parser(raw_code)
    # Initialize head node of the code.
    head = Node(["1",get_token(java_code)])
    # Recursively construct AST tree.
    for child_order in range(len(get_children(java_code))):
        get_trees(get_children(java_code)[child_order], head, "1"+str(int(child_order)+1))
    return head


#### OLD CODE
def compute_tf(ast_tree, all_nodes):
    """Compute term frequency for an AST given a list of all node types."""
    # nodes = [type(node).__name__ for node in ast.walk(ast_tree)]
    nodes = [node.name[1] for node in PreOrderIter(ast_tree)]
    node_count = Counter(nodes)
    total_nodes = sum(node_count.values())
    # Return a list of term frequencies in the same order as all_nodes
    return [node_count.get(node, 0) / total_nodes for node in all_nodes]


def compute_df(ast_trees):
    """Compute document frequency for all nodes given a group of ASTs."""
    df_counter = Counter()
    for ast_tree in ast_trees:
        nodes = [node.name[1] for node in PreOrderIter(ast_tree)]
        df_counter.update(nodes)
    return df_counter

def compute_idf(df, total_documents):
    return {node: math.log((total_documents + 1) / (frequency + 1)) + 1 for node, frequency in df.items()}


def compute_tfidf(ast_trees):
    """Compute TF-IDF for a collection of ASTs and return aligned vectors."""
    df = compute_df(ast_trees)
    idf = compute_idf(df, len(ast_trees))
    all_nodes = list(df.keys())
    tfidf_trees = []
    
    for ast_tree in ast_trees:
        tf = compute_tf(ast_tree, all_nodes)
        # Use the index to align tf with idf values
        tfidf = np.array([tf_val * idf[node] for node, tf_val in zip(all_nodes, tf)])
        tfidf_trees.append(tfidf)
    
    return tfidf_trees, all_nodes, idf

def compute_tfidf_ood(new_tree, all_nodes, existing_idf):
    """Compute TF-IDF for a new AST using existing IDF values."""
    tf_new_tree = compute_tf(new_tree, all_nodes)
    tfidf = np.array([tf_val * existing_idf[node] for node, tf_val in zip(all_nodes, tf_new_tree)])
    return tfidf

def euclidean_distance(vec1, vec2):
    return np.linalg.norm(vec1 - vec2)

# def dfs_traversal(tree):
#     """Perform a DFS traversal on an AST and return a list of node types."""
#     nodes = []
#     def visit(node):
#         nodes.append(type(node).__name__)
#         for child in ast.iter_child_nodes(node):
#             visit(child)
#     visit(tree)
#     return nodes
def dfs_traversal(tree):
    """Perform a DFS traversal on an anytree tree and return a list of node types."""
    nodes = []
    def visit(node):
        nodes.append(node.name[1])
        for child in node.children:
            visit(child)
    visit(tree)
    return nodes

# def set_of_children(node, **kwargs): 
#     """Helper function for getting all the nodes in a subtree"""
#     return set((type(node).__name__, )).union(set().union(*[set_of_children(child) for child in ast.iter_child_nodes(node)]))
def set_of_children(node):
    """Helper function for getting all the nodes in a subtree"""
    return set((node.name[1], )).union(set().union(*[set_of_children(child) for child in node.children]))

# def tree_edit_distance_with_operations(node1, node2): 
#     # Base cases
#     if not node1 and not node2:
#         return set()
#     if not node1:
#         return set_of_children(node2, annotate_fields=False)
#     if not node2:
#         return set_of_children(node1, annotate_fields=False)

#     # Check if nodes are of same type
#     if type(node1) != type(node2):
#         return set_of_children(node2, annotate_fields=False).union(tree_edit_distance_with_operations(node1, None)) # delete faulty subtree, insert correct one

#     else:
#         children1 = list(ast.iter_child_nodes(node1))
#         children2 = list(ast.iter_child_nodes(node2))

#         # Get the cost and operations for matching children of both nodes
#         operations = set()
#         for c1, c2 in zip(children1, children2):
#             ops = tree_edit_distance_with_operations(c1, c2)
#             operations = operations.union(ops)

#         # Extra children in either of the trees
#         for extra_child in children1[len(children2):]:
#             ops = tree_edit_distance_with_operations(extra_child, None)
#             operations = operations.union(ops)
#         for extra_child in children2[len(children1):]:
#             ops = tree_edit_distance_with_operations(None, extra_child)
#             operations = operations.union(ops)

#         return operations
def tree_edit_distance_with_operations(node1, node2):
    # Base cases
    if not node1 and not node2:
        return set()
    if not node1:
        return set_of_children(node2)
    if not node2:
        return set_of_children(node1)

    # Check if nodes are of same type
    if node1.name[1] != node2.name[1]:
        return set_of_children(node2).union(tree_edit_distance_with_operations(node1, None))  # delete faulty subtree, insert correct one
    else:
        children1 = list(node1.children)
        children2 = list(node2.children)

        # Get the cost and operations for matching children of both nodes
        operations = set()
        for c1, c2 in zip(children1, children2):
            ops = tree_edit_distance_with_operations(c1, c2)
            operations = operations.union(ops)

        # Extra children in either of the trees
        for extra_child in children1[len(children2):]:
            ops = tree_edit_distance_with_operations(extra_child, None)
            operations = operations.union(ops)
        for extra_child in children2[len(children1):]:
            ops = tree_edit_distance_with_operations(None, extra_child)
            operations = operations.union(ops)

        return operations


def print_ast(node, indent=0):
    """
    Recursively print an AST node.
    """
    # Print the node type and any additional information (e.g., its name if it's a function or variable)
    if isinstance(node, ast.FunctionDef):
        print('  ' * indent + f'FunctionDef(name={node.name})')
    elif isinstance(node, ast.Name):
        print('  ' * indent + f'Name(id={node.id})')
    else:
        print('  ' * indent + str(node.name[1]))

    # For each field (like 'body' for functions, 'value' for assignments, etc.)
    for field in node._fields:
        value = getattr(node, field, None)
        if isinstance(value, list):
            for item in value:
                print_ast(item, indent+1)
        elif isinstance(value, ast.AST):
            print_ast(value, indent+1)

