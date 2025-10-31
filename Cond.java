class Cond implements Node {
    private final ParserHelper P;

    private enum Kind { CMPR, NOT, BRACKET, OR, AND }
    private Kind kind;

    private Cmpr cmprLeft;
    private Cond condRight;
    private Cond inner;

    Cond(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        if (P.token() == Core.NOT) {
            kind = Kind.NOT;
            P.advance();
            inner = new Cond(P.scanner());
            inner.parse();
            return;
        } else if (P.token() == Core.LSQUARE) {
            kind = Kind.BRACKET;
            P.advance();
            inner = new Cond(P.scanner()); inner.parse();
            P.expect(Core.RSQUARE, "expected ']' in condition");
        } else {
            cmprLeft = new Cmpr(P.scanner()); cmprLeft.parse();
            if (P.token() == Core.OR) {
                kind = Kind.OR; P.advance();
                condRight = new Cond(P.scanner()); condRight.parse();
            } else if (P.token() == Core.AND) {
                kind = Kind.AND; P.advance();
                condRight = new Cond(P.scanner()); condRight.parse();
            } else {
                kind = Kind.CMPR;
            }
        }
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        switch (kind) {
            case CMPR: cmprLeft.semanticCheck(st); break;
            case NOT: inner.semanticCheck(st); break;
            case BRACKET: inner.semanticCheck(st); break;
            case OR: case AND:
                cmprLeft.semanticCheck(st);
                condRight.semanticCheck(st);
                break;
        }
    }

    @Override
    public void print(int indent) {
        indent(indent); System.out.println(toSource());
    }

    String toSource() {
        switch (kind) {
            case CMPR: return cmprLeft.toSource();
            case NOT: return "not " + inner.toSource();
            case BRACKET: return "[" + inner.toSource() + "]";
            case OR: return cmprLeft.toSource() + " or " + condRight.toSource();
            case AND: return cmprLeft.toSource() + " and " + condRight.toSource();
        }
        return "";
    }

    public boolean evaluate(Memory mem) {
        return switch (kind) {
            case CMPR -> cmprLeft.evaluate(mem);
            case NOT -> !inner.evaluate(mem);
            case BRACKET -> inner.evaluate(mem);
            case OR -> cmprLeft.evaluate(mem) || condRight.evaluate(mem);
            case AND -> cmprLeft.evaluate(mem) && condRight.evaluate(mem);
        };
    }


}
