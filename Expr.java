class Expr implements Node {
    private final ParserHelper P;
    private Term left;
    private Core op;
    private Expr right;

    Expr(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        left = new Term(P.scanner());
        left.parse();
        if (P.token() == Core.ADD || P.token() == Core.SUBTRACT) {
            op = P.token(); P.advance();
            right = new Expr(P.scanner());
            right.parse();
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
        indent(indent);
        System.out.println(toSource());
    }

    public void print() {
        System.out.print(toSource());
    }

    String toSource() {
        String s = left.toSource();
        if (right != null) {
            s += (op == Core.ADD ? " + " : " - ") + right.toSource();
        }
        return s;
    }

    public int execute(Memory mem) {
        int leftVal = left.execute(mem);
        if (right == null) {
            return leftVal;
        }

        int rightVal = right.execute(mem);

        if (op == Core.ADD) {
            return leftVal + rightVal;
        } else {
            return leftVal - rightVal;
        }
    }

}
