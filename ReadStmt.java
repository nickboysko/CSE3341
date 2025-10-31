import java.util.Scanner;

class ReadStmt implements Node {
    private final ParserHelper P;
    private String id;

    ReadStmt(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        P.expect(Core.READ, "expected 'read'");
        P.expect(Core.LPAREN, "expected '(' after read");
        if (P.token() != Core.ID) P.fail("expected identifier in read");
        id = P.id(); P.advance();
        P.expect(Core.RPAREN, "expected ')'");
        P.expect(Core.SEMICOLON, "expected ';' after read");
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        // read targets must be integers
        SymbolTable.requireType(st, id, VarType.INTEGER);
    }

    @Override
    public void print(int indent) {
        indent(indent); System.out.println("read(" + id + ");");
    }

    public void execute(Memory mem, Scanner dataScanner) {
        if (!dataScanner.hasNextInt()) {
            throw new RuntimeException("Runtime error: no more input data available for read()");
        }

        int nextVal = dataScanner.nextInt();
        mem.setInt(id, nextVal);
    }
}
