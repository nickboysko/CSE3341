import java.util.Scanner;

class Procedure implements Node {
    private final ParserHelper P;
    private String name;
    private DeclSeq globals;
    private StmtSeq body;

    public Procedure(CoreScanner scanner) {
        this.P = new ParserHelper(scanner);
    }

    @Override
    public void parse() {
        P.expect(Core.PROCEDURE, "expected 'procedure'");
        if (P.token() != Core.ID) P.fail("expected procedure name");
        name = P.id(); P.advance();
        P.expect(Core.IS, "expected 'is'");

        if (P.token() != Core.BEGIN) {
            globals = new DeclSeq(P.scanner());
            globals.parse();
        }
        P.expect(Core.BEGIN, "expected 'begin'");

        body = new StmtSeq(P.scanner());
        body.parse();

        P.expect(Core.END, "expected 'end'");
    }


    @Override
    public void semanticCheck(SymbolTable st) {
        st.enterScope(); // global scope for the procedure
        if (globals != null) globals.semanticCheck(st);

        st.enterScope(); // procedure body 
        body.semanticCheck(st);
        st.exitScope();

        st.exitScope();
    }

    @Override
    public void print(int indent) {
        indent(indent); System.out.println("procedure " + name + " is");
        if (globals != null) globals.print(indent + 1);
        indent(indent); System.out.println("begin");
        body.print(indent + 1);
        indent(indent); System.out.println("end");
    }

    public void execute(Memory mem, Scanner dataScanner) {
        mem.enterScope();   
        if (globals != null) globals.execute(mem);
        if (body != null) body.execute(mem, dataScanner);
        mem.exitScope();
    }

}

