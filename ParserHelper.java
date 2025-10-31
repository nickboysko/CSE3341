class ParserHelper {
    private final CoreScanner scanner;
    ParserHelper(CoreScanner s) { this.scanner = s; }
    CoreScanner scanner() { return scanner; }

    Core token() { return scanner.currentToken(); }
    void advance() { scanner.nextToken(); }
    String id() { return scanner.getId(); }
    int cnst() { return scanner.getConst(); }
    String str() { return scanner.getString(); }

    void expect(Core t, String msg) {
        if (scanner.currentToken() != t) fail(msg + " (found " + scanner.currentToken() + ")");
        scanner.nextToken();
    }
    void fail(String msg) {
        System.out.println("ERROR: " + msg);
        System.exit(1);
    }
}

