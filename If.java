import java.util.*;

class If implements Node {
    private final ParserHelper P;
    private Cond cond;
    private StmtSeq thenPart;
    private StmtSeq elsePart;

    If(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        P.expect(Core.IF, "expected 'if'");
        cond = new Cond(P.scanner()); cond.parse();
        P.expect(Core.THEN, "expected 'then'");
        thenPart = new StmtSeq(P.scanner()); thenPart.parse();
        if (P.token() == Core.ELSE) {
            P.advance();
            elsePart = new StmtSeq(P.scanner()); elsePart.parse();
        }
        P.expect(Core.END, "expected 'end' after if");
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        st.enterScope();
        thenPart.semanticCheck(st);
        st.exitScope();
        if (elsePart != null) {
            st.enterScope();
            elsePart.semanticCheck(st);
            st.exitScope();
        }
    }

    @Override
    public void print(int indent) {
        indent(indent); System.out.println("if " + cond.toSource() + " then");
        thenPart.print(indent + 1);
        if (elsePart != null) {
            indent(indent); System.out.println("else");
            elsePart.print(indent + 1);
        }
        indent(indent); System.out.println("end");
    }

    public void execute(Memory mem, Scanner dataScanner) {
        execute(mem, dataScanner, new HashMap<>());
    }

    public void execute(Memory mem, Scanner dataScanner, Map<String, Function> procedures) {
        if (cond.evaluate(mem)) {
            mem.enterScope();
            thenPart.execute(mem, dataScanner, procedures);
            mem.exitScope(); 
        } else if (elsePart != null) {
            mem.enterScope();
            elsePart.execute(mem, dataScanner, procedures);
            mem.exitScope();
        }
    }
}