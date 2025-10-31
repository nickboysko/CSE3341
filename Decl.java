class Decl implements Node {
    private final ParserHelper P;
    private boolean isInteger;
    private String id;

    Decl(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        if (P.token() == Core.INTEGER) {
            isInteger = true; P.advance();
        } else if (P.token() == Core.OBJECT) {
            isInteger = false; P.advance();
        } else {
            P.fail("expected 'integer' or 'object' in declaration");
            return;
        }
        if (P.token() != Core.ID) P.fail("expected identifier in declaration");
        id = P.id(); P.advance();
        P.expect(Core.SEMICOLON, "expected ';'");
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        st.declare(id, isInteger ? VarType.INTEGER : VarType.OBJECT);
    }

    @Override
    public void print(int indent) {
        indent(indent);
        System.out.println((isInteger ? "integer " : "object ") + id + ";");
    }

    public void execute(Memory mem) {
        if (isInteger) mem.declareInt(id);
        else mem.declareObj(id);
    }
}
