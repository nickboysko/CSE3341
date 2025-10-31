import java.util.*;

class Function implements Node {
    private final ParserHelper P;
    private String name;
    private List<String> parameters;
    private StmtSeq body;

    Function(CoreScanner s) { 
        this.P = new ParserHelper(s); 
        this.parameters = new ArrayList<>();
    }

    @Override
    public void parse() {
        P.expect(Core.PROCEDURE, "expected 'procedure'");
        if (P.token() != Core.ID) P.fail("expected procedure name");
        name = P.id(); 
        P.advance();
        
        P.expect(Core.LPAREN, "expected '(' after procedure name");
        P.expect(Core.OBJECT, "expected 'object' keyword");
        
        // Parse parameters
        parseParameters();
        
        P.expect(Core.RPAREN, "expected ')' after parameters");
        P.expect(Core.IS, "expected 'is'");
        
        body = new StmtSeq(P.scanner());
        body.parse();
        
        P.expect(Core.END, "expected 'end'");
    }

    private void parseParameters() {
        if (P.token() != Core.ID) P.fail("expected parameter name");
        String param = P.id();
        parameters.add(param);
        P.advance();
        
        while (P.token() == Core.COMMA) {
            P.advance();
            if (P.token() != Core.ID) P.fail("expected parameter name after comma");
            param = P.id();
            parameters.add(param);
            P.advance();
        }
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        // Check for duplicate parameters
        Set<String> seen = new HashSet<>();
        for (String param : parameters) {
            if (seen.contains(param)) {
                SymbolTable.error("duplicate formal parameter: " + param);
            }
            seen.add(param);
        }
        
        // Check procedure body with parameters in scope
        st.enterScope();
        for (String param : parameters) {
            st.declare(param, VarType.OBJECT);
        }
        body.semanticCheck(st);
        st.exitScope();
    }

    @Override
    public void print(int indent) {
        indent(indent);
        System.out.print("procedure " + name + "(object ");
        for (int i = 0; i < parameters.size(); i++) {
            System.out.print(parameters.get(i));
            if (i < parameters.size() - 1) System.out.print(", ");
        }
        System.out.println(") is");
        body.print(indent + 1);
        indent(indent);
        System.out.println("end");
    }

    public String getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public StmtSeq getBody() {
        return body;
    }
}