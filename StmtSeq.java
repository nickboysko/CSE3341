import java.util.*;

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
        execute(mem, dataScanner, new HashMap<>());
    }

    public void execute(Memory mem, Scanner dataScanner, Map<String, Function> procedures) {
        first.execute(mem, dataScanner, procedures);
        if (rest != null) rest.execute(mem, dataScanner, procedures);
    }

    public List<Call> extractCalls() {
        List<Call> calls = new ArrayList<>();
        if (first.isCall()) {
            calls.add(first.getCall());
        }
        if (rest != null) {
            calls.addAll(rest.extractCalls());
        }
        return calls;
    }
}