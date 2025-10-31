class Cmpr implements Node {
    private final ParserHelper P;
    private Expr left, right;
    private Core op;

    Cmpr(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        left = new Expr(P.scanner());
        left.parse();

        if (P.token() == Core.EQUAL || P.token() == Core.LESS) {
            op = P.token();
            P.advance();

            right = new Expr(P.scanner());
            right.parse();
        } else {
            P.fail("expected '==' or '<' in comparison");
        }
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        left.requireInt(st);
        right.requireInt(st);
    }

    @Override
    public void print(int indent) {
        indent(indent); System.out.println(toSource());
    }

    String toSource() {
        String o = (op == Core.EQUAL ? "==" : "<");
        return left.toSource() + " " + o + " " + right.toSource();
    }

    public boolean evaluate(Memory mem) {
        int l = left.execute(mem);
        int r = right.execute(mem);
        if (op == Core.EQUAL) return l == r;
        return l < r;
    }
}
