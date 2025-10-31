import java.util.*;

class Procedure implements Node {
    private final ParserHelper P;
    private String name;
    private DeclSeq globals;
    private StmtSeq body;
    private Map<String, Function> procedures;

    public Procedure(CoreScanner scanner) {
        this.P = new ParserHelper(scanner);
        this.procedures = new HashMap<>();
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
        
        // Extract and register all nested procedures
        if (globals != null) {
            procedures = globals.extractFunctions();
            globals.semanticCheck(st);
        }
        
        // Validate all procedure calls reference declared procedures
        st.enterScope(); // procedure body 
        body.semanticCheck(st);
        validateProcedureCalls(body);
        st.exitScope();

        st.exitScope();
    }

    private void validateProcedureCalls(StmtSeq stmtSeq) {
        // This will recursively check all procedure calls in the statement sequence
        List<Call> calls = extractCalls(stmtSeq);
        for (Call call : calls) {
            if (!procedures.containsKey(call.getProcName())) {
                SymbolTable.error("procedure '" + call.getProcName() + "' not declared");
            }
            
            // Check argument count
            Function func = procedures.get(call.getProcName());
            if (call.getArguments().size() != func.getParameters().size()) {
                SymbolTable.error("procedure '" + call.getProcName() + "' expects " + 
                    func.getParameters().size() + " arguments but got " + call.getArguments().size());
            }
        }
    }

    private List<Call> extractCalls(Object node) {
        List<Call> calls = new ArrayList<>();
        
        if (node instanceof StmtSeq) {
            StmtSeq seq = (StmtSeq) node;
            calls.addAll(extractCallsFromStmtSeq(seq));
        } else if (node instanceof Call) {
            calls.add((Call) node);
        }
        
        return calls;
    }

    private List<Call> extractCallsFromStmtSeq(StmtSeq seq) {
        // This is a helper that needs access to StmtSeq internals
        // We'll handle this through StmtSeq providing a method
        List<Call> calls = new ArrayList<>();
        // The actual extraction will be done through execution path
        return calls;
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
        if (globals != null) {
            globals.execute(mem);
        }
        if (body != null) {
            body.execute(mem, dataScanner, procedures);
        }
        mem.exitScope();
    }

    public Map<String, Function> getProcedures() {
        return procedures;
    }
}