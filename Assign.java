import java.util.HashMap;
import java.util.Map;

class Assign implements Node {
    private final ParserHelper P;

    private enum Kind { ID_EQ_EXPR, OBJ_INDEX_EQ_EXPR, NEW_OBJECT, COLON_ASSIGN }
    private Kind kind;

    private String id1;    
    private String keyStr;
    private Expr expr;
    private String id2;
    private String newType;

    Assign(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        if (P.token() != Core.ID) P.fail("expected identifier at start of assignment");
        id1 = P.id(); P.advance();

        if (P.token() == Core.ASSIGN) {
            P.advance();
            if (P.token() == Core.NEW) {
                P.advance();
                P.expect(Core.OBJECT, "expected 'object' after 'new'");
                P.expect(Core.LPAREN, "expected '(' after 'object'");
                if (P.token() != Core.STRING) P.fail("expected string in new object");
                newType = P.str(); P.advance();
                P.expect(Core.COMMA, "expected ',' in new object(...)");
                expr = new Expr(P.scanner()); expr.parse();
                P.expect(Core.RPAREN, "expected ')' after new object args");
                P.expect(Core.SEMICOLON, "expected ';' after assignment");
                kind = Kind.NEW_OBJECT;
            } else {
                expr = new Expr(P.scanner()); expr.parse();
                P.expect(Core.SEMICOLON, "expected ';' after assignment");
                kind = Kind.ID_EQ_EXPR;
            }
        } else if (P.token() == Core.LSQUARE) {
            P.advance();
            if (P.token() != Core.STRING) P.fail("expected string inside []");
            keyStr = P.str(); P.advance();
            P.expect(Core.RSQUARE, "expected ']' after string");
            P.expect(Core.ASSIGN, "expected '=' after ]");
            expr = new Expr(P.scanner()); expr.parse();
            P.expect(Core.SEMICOLON, "expected ';' after assignment");
            kind = Kind.OBJ_INDEX_EQ_EXPR;
        } else if (P.token() == Core.COLON) {
            P.advance();
            if (P.token() != Core.ID) P.fail("expected identifier after ':'");
            id2 = P.id(); P.advance();
            P.expect(Core.SEMICOLON, "expected ';' after ':' assignment");
            kind = Kind.COLON_ASSIGN;
        } else {
            P.fail("malformed assignment");
        }
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        switch (kind) {
            case ID_EQ_EXPR:
                VarType t = st.lookupType(id1);
                if (t != VarType.INTEGER && t != VarType.OBJECT) {
                    P.fail("undefined or invalid variable type for " + id1);
                }
                expr.requireInt(st);
                break;
            case OBJ_INDEX_EQ_EXPR:
                SymbolTable.requireType(st, id1, VarType.OBJECT);
                expr.requireInt(st);
                break;
            case NEW_OBJECT:
                SymbolTable.requireType(st, id1, VarType.OBJECT);
                expr.requireInt(st);
                break;
            case COLON_ASSIGN:
                SymbolTable.requireType(st, id1, VarType.OBJECT);
                SymbolTable.requireType(st, id2, VarType.OBJECT);
                break;
        }
    }

    @Override
    public void print(int indent) {
        indent(indent);
        switch (kind) {
            case ID_EQ_EXPR:
                System.out.print(id1 + " = ");
                expr.print();
                System.out.println(";");
                break;
            case OBJ_INDEX_EQ_EXPR:
                System.out.print(id1 + "['" + keyStr + "'] = ");
                expr.print();
                System.out.println(";");
                break;
            case NEW_OBJECT:
                System.out.print(id1 + " = new object('" + newType + "', ");
                expr.print();
                System.out.println(");");
                break;
            case COLON_ASSIGN:
                System.out.println(id1 + " : " + id2 + ";");
                break;
        }
    }

    public void execute(Memory mem) {
    switch (kind) {
        case ID_EQ_EXPR -> {
            int val = expr.execute(mem);
            if (mem.hasIntVar(id1)) {
                mem.setInt(id1, val);
            } else if (mem.hasObjVar(id1)) {
                Map<String, Integer> obj = mem.getObj(id1);
                if (obj == null)
                    throw new RuntimeException("Runtime error: object '" + id1 + "' not initialized");
                String def = mem.getDefaultKey(id1);
                if (def == null)
                    throw new RuntimeException("Runtime error: object '" + id1 + "' not initialized"); // no default yet
                obj.put(def, val);
            } else {
                throw new RuntimeException("Undefined variable: " + id1);
            }
        }

        case OBJ_INDEX_EQ_EXPR -> {
            Map<String, Integer> obj = mem.getObj(id1);
            if (obj == null)
                throw new RuntimeException("Runtime error: object '" + id1 + "' not initialized");
            obj.put(keyStr, expr.execute(mem));
        }

        case NEW_OBJECT -> {
            Map<String, Integer> newObj = new HashMap<>();
            newObj.put(newType, expr.execute(mem));
            mem.updateObjRefAndKeepDefault(id1, newObj);
            mem.setDefaultKey(id1, newType);
        }

        case COLON_ASSIGN -> {
             Map<String, Integer> ref = mem.getObj(id2);
            if (ref == null)
                throw new RuntimeException("Runtime error: object '" + id2 + "' not initialized");
            mem.setObjRef(id1, ref);
            String def = mem.getDefaultKey(id2);
            mem.setDefaultKey(id1, def);
        }
    }
}

}
