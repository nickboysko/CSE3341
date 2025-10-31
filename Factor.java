import java.util.Map;

class Factor implements Node {
    private final ParserHelper P;

    private enum Kind { ID, OBJ_INDEX, CONST, PAREN }
    private Kind kind;

    private String id;
    private String keyStr;
    private int constVal;
    private Expr parenExpr;

    Factor(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        Core t = P.token();
        if (t == Core.ID) {
            id = P.id(); P.advance();
            if (P.token() == Core.LSQUARE) {
                P.advance();
                if (P.token() != Core.STRING) P.fail("expected string inside []");
                keyStr = P.str(); P.advance();
                P.expect(Core.RSQUARE, "expected ']' after string");
                kind = Kind.OBJ_INDEX;
            } else {
                kind = Kind.ID;
            }
        } else if (t == Core.CONST) {
            constVal = P.cnst(); P.advance();
            kind = Kind.CONST;
        } else if (t == Core.LPAREN) {
            P.advance();
            parenExpr = new Expr(P.scanner()); parenExpr.parse();
            P.expect(Core.RPAREN, "expected ')'");
            kind = Kind.PAREN;
        } else {
            P.fail("expected factor");
        }
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        requireInt(st);
    }

    public void requireInt(SymbolTable st) {
        switch (kind) {
            case ID:
                SymbolTable.requireDeclared(st, id);
                break;
            case OBJ_INDEX:
                SymbolTable.requireType(st, id, VarType.OBJECT);
                break; 
            case CONST:
                break;
            case PAREN:
                parenExpr.requireInt(st);
                break;
        }
    }

    @Override
    public void print(int indent) {
        indent(indent); System.out.println(toSource());
    }

    String toSource() {
        switch (kind) {
            case ID: return id;
            case OBJ_INDEX: return id + "['" + keyStr + "']";
            case CONST: return Integer.toString(constVal);
            case PAREN: return "(" + parenExpr.toSource() + ")";
        }
        return "";
    }

        public int execute(Memory mem) {
        return switch (kind) {
            case ID -> {
                if (mem.hasIntVar(id)) {
                    yield mem.getInt(id);
                } else if (mem.hasObjVar(id)) {
                    Map<String, Integer> obj = mem.getObj(id);
                    if (obj == null)
                        throw new RuntimeException("Runtime error: object '" + id + "' not initialized");
                    String def = mem.getDefaultKey(id);
                    if (def == null || !obj.containsKey(def))
                        throw new RuntimeException("Runtime error: key '" + def + "' not found in object '" + id + "'");
                    yield obj.get(def);
                } else {
                    throw new RuntimeException("Undefined variable: " + id);
                }
            }
            case OBJ_INDEX -> {
                Map<String, Integer> obj = mem.getObj(id);
                if (obj == null)
                    throw new RuntimeException("Runtime error: object '" + id + "' not initialized");
                if (!obj.containsKey(keyStr))
                    throw new RuntimeException("Runtime error: key '" + keyStr + "' not found in object '" + id + "'");
                yield obj.get(keyStr);
            }

            case CONST -> constVal;

            case PAREN -> parenExpr.execute(mem);
        };
    }
}
