class DeclSeq implements Node {
    private final ParserHelper P;
    private Decl first;
    private DeclSeq rest; 

    DeclSeq(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        first = new Decl(P.scanner());
        first.parse();
        if (P.token() == Core.INTEGER || P.token() == Core.OBJECT) {
            rest = new DeclSeq(P.scanner());
            rest.parse();
        }
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        first.semanticCheck(st);
        if (rest != null) rest.semanticCheck(st);
    }

    @Override
    public void print(int indent) {
        first.print(indent);
        if (rest != null) rest.print(indent);
    }

    public void execute(Memory mem) {
        first.execute(mem);
        if (rest != null) rest.execute(mem);
    }
}
