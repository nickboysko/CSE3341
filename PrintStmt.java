class PrintStmt implements Node {
    private final ParserHelper P;
    private Expr expr;

    PrintStmt(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        P.expect(Core.PRINT, "expected 'print'");
        P.expect(Core.LPAREN, "expected '(' after print");
        expr = new Expr(P.scanner()); expr.parse();
        P.expect(Core.RPAREN, "expected ')'");
        P.expect(Core.SEMICOLON, "expected ';' after print");
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        expr.requireInt(st);
    }

    @Override
    public void print(int indent) {
        indent(indent); System.out.print("print(");
        expr.print();
        System.out.println(");");
    }

    public void execute(Memory mem) {
        int value = expr.execute(mem);
        System.out.print(value);
        System.out.print('\n');
    }
}
