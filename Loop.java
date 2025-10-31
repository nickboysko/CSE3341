import java.util.*;

class Loop implements Node {
    private final ParserHelper P;
    private String loopVar;
    private Expr initExpr;
    private Cond condition;
    private Expr stepExpr;
    private StmtSeq body;

    Loop(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        P.expect(Core.FOR, "expected 'for'");
        P.expect(Core.LPAREN, "expected '(' after 'for'");
        if (P.token() != Core.ID) P.fail("expected loop variable");
        loopVar = P.id(); P.advance();
        P.expect(Core.ASSIGN, "expected '=' after loop variable");
        initExpr = new Expr(P.scanner()); initExpr.parse();
        P.expect(Core.SEMICOLON, "expected ';' after init expr");
        condition = new Cond(P.scanner()); condition.parse();
        P.expect(Core.SEMICOLON, "expected ';' after condition");
        stepExpr = new Expr(P.scanner()); stepExpr.parse();
        P.expect(Core.RPAREN, "expected ')'");
        P.expect(Core.DO, "expected 'do'");
        body = new StmtSeq(P.scanner());
        body.parse();
        P.expect(Core.END, "expected 'end' after loop");
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        VarType t = st.lookupType(loopVar);
        if (t != VarType.INTEGER && t != VarType.OBJECT) {
            SymbolTable.error("invalid loop variable type for " + loopVar);
        }

        initExpr.requireInt(st);
        condition.semanticCheck(st);
        stepExpr.requireInt(st);

        st.enterScope();
        body.semanticCheck(st);
        st.exitScope();
    }


    @Override
    public void print(int indent) {
        indent(indent);
        System.out.print("for (" + loopVar + " = ");
        initExpr.print();
        System.out.print("; " + condition.toSource() + "; ");
        stepExpr.print();
        System.out.println(") do");
        body.print(indent + 1);
        indent(indent); System.out.println("end");
    }

    public void execute(Memory mem, Scanner dataScanner) {
        execute(mem, dataScanner, new HashMap<>());
    }

    public void execute(Memory mem, Scanner dataScanner, Map<String, Function> procedures) {
        int initVal = initExpr.execute(mem);
        if (mem.hasIntVar(loopVar)) {
            mem.setInt(loopVar, initVal);
        } else if (mem.hasObjVar(loopVar)) {
            Map<String,Integer> obj = mem.getObj(loopVar);
            String def = mem.getDefaultKey(loopVar);
            obj.put(def, initVal);
        }

        while (condition.evaluate(mem)) {
            mem.enterScope();
            body.execute(mem, dataScanner, procedures);
            mem.exitScope();

            int stepVal = stepExpr.execute(mem);
            if (mem.hasIntVar(loopVar)) {
                mem.setInt(loopVar, stepVal);
            } else {
                Map<String,Integer> obj = mem.getObj(loopVar);
                String def = mem.getDefaultKey(loopVar);
                obj.put(def, stepVal);
            }
        }
    }

}