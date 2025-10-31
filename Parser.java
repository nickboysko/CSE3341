public class Parser {
    private final CoreScanner scanner;

    public Parser(CoreScanner scanner) {
        this.scanner = scanner;
    }

    private void expect(Core t, String msg) {
        if (scanner.currentToken() != t) {
            error(msg + " (found " + scanner.currentToken() + ")");
        }
        scanner.nextToken();
    }

    private void error(String msg) {
        System.out.println("ERROR: " + msg);
        System.exit(1);
    }

    public Procedure parseProcedure() {
        Procedure p = new Procedure(scanner);
        p.parse();
        // after parsing should be at EOS
        if (scanner.currentToken() != Core.EOS) {
            error("extra tokens after program end");
        }
        return p;
    }

    // Expose helpers to node classes
    Core token() { return scanner.currentToken(); }
    void advance() { scanner.nextToken(); }
    String id() { return scanner.getId(); }
    int cnst() { return scanner.getConst(); }
    String str() { return scanner.getString(); }
    void expectToken(Core t, String msg) { expect(t, msg); }
    void fail(String msg) { error(msg); }
}