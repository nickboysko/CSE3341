CSE 3341 Project 3 
Nicholas Boysko

Project 3 Additions:
    For project 3, execute methods were added for every parse tree class to interpret the Core program
    A new Memory.java class was added that manages interger and object values at runtime using scope and stack logic in SymbolTable 
    Objects store key --> Integer mappings and supports new object, aliasing with id1 : id2 and default key access when an object us not  used
Files Included:
Core.java
    Enum of all the tokens for the core language

CoreScanner.java
    Same file from project 1 which reads characters from the input file and converts them to a stream of tokens

Main.java
    Modified from Project 1 to create scanner, build the parse tree using the parser, and the pretty-prints the Core program

Parser.java
    Driver for the recursive decent parser and coordinates the parsing of the top-level rule

ParserHelper.java  
    Wrapper over CoreScanner to simplify parsing

SymbolTable.java
    Tracks variable declarations and scopes. This is used for semantic checks to enforce declaration-before-us, prevent duplicates in the same scope, and make sure of type correctness

Node.java 
    Interface for all the parse tree nodes

Procedure.java
DeclSeq.java
Decl.java
StmtSeq.java
Stmt.java
Assign.java
If.java
Loop.java
PrintStmt.java
ReadStmt.java
Cond.java
Cmpr.java
Expr.java
Term.java
Factor.java

    Classes for all the nonterminals in the grammer which parse its nonterminal using recursive decent, store the relevant children, print itself and perform semantic checks using the SymbolTable

Project Design
    The parser is implemented as a recursive decent parser where each nonterminal in the grammer is represented by a corresponding Java class. Each nonterminal class has the methods parse, semanticCheck, and print. The parse tree uis built directly by these classes as the input is being parsed. The tree is traversed and used to produce the original format

    Scopes are managed by a SymbolTable which uses a stack of hasmaps. Entering a block pushes a new scope and leaving a block pops the scope. This ensures that duplicates in the same scope are caught

    Testing was performed using the provided tester.sh script allong with the Correct and Error directories. All test cases passed and I got correct error messages for all the errors
