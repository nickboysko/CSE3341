interface Node {
    void parse();
    void print(int indent);
    void semanticCheck(SymbolTable st);
    default void indent(int n) {
        for (int i = 0; i < n; i++) System.out.print("  ");
    }
}

