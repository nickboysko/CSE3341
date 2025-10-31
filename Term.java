class Term implements Node {
    private final ParserHelper P;
    private Factor left;
    private Core op;
    private Term right;

    Term(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        left = new Factor(P.scanner()); left.parse();
        if (P.token() == Core.MULTIPLY || P.token() == Core.DIVIDE) {
            op = P.token(); P.advance();
            right = new Term(P.scanner()); right.parse();
        }
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        left.requireInt(st);
        if (right != null) right.requireInt(st);
    }

    public void requireInt(SymbolTable st) {
        semanticCheck(st);
    }

    @Override
    public void print(int indent) {
        indent(indent); System.out.println(toSource());
    }

    String toSource() {
        String s = left.toSource();
        if (right != null) {
            s += (op == Core.MULTIPLY ? " * " : " / ") + right.toSource();
        }
        return s;
    }

public int execute(Memory mem) {
    int leftVal = left.execute(mem);
    if (right == null) {
        return leftVal;
    }

    int rightVal = right.execute(mem);

    if (op == Core.MULTIPLY) {
        return leftVal * rightVal;
    } else { // division
        if (rightVal == 0)
            throw new RuntimeException("Runtime error: division by zero");
        return leftVal / rightVal;
    }
}
}
