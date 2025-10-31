import java.util.Scanner;

class StmtSeq implements Node {
    private final ParserHelper P;
    private Stmt first;
    private StmtSeq rest; 

    StmtSeq(CoreScanner s) { this.P = new ParserHelper(s); }

    @Override
    public void parse() {
        first = new Stmt(P.scanner());
        first.parse();
        while (Stmt.startsStmt(P.token())) {
            StmtSeq tail = new StmtSeq(P.scanner());
            tail.first = new Stmt(P.scanner());
            tail.first.parse();
            if (rest == null) rest = tail;
            else {
                StmtSeq curr = rest;
                while (curr.rest != null) curr = curr.rest;
                curr.rest = tail;
            }
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

    public void execute(Memory mem, Scanner dataScanner) {
        first.execute(mem, dataScanner);
        if (rest != null) rest.execute(mem, dataScanner);
    }
}
