import java.util.*;

class Call implements Node {
    private final ParserHelper P;
    private String procName;
    private List<String> arguments;

    Call(CoreScanner s) { 
        this.P = new ParserHelper(s); 
        this.arguments = new ArrayList<>();
    }

    @Override
    public void parse() {
        P.expect(Core.BEGIN, "expected 'begin'");
        if (P.token() != Core.ID) P.fail("expected procedure name");
        procName = P.id();
        P.advance();
        
        P.expect(Core.LPAREN, "expected '(' after procedure name");
        
        // Parse arguments
        if (P.token() == Core.ID) {
            arguments.add(P.id());
            P.advance();
            
            while (P.token() == Core.COMMA) {
                P.advance();
                if (P.token() != Core.ID) P.fail("expected argument name after comma");
                arguments.add(P.id());
                P.advance();
            }
        }
        
        P.expect(Core.RPAREN, "expected ')' after arguments");
        P.expect(Core.SEMICOLON, "expected ';' after procedure call");
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        // Check that all arguments are declared
        for (String arg : arguments) {
            SymbolTable.requireDeclared(st, arg);
        }
        
        // Check procedure exists
    }

    @Override
    public void print(int indent) {
        indent(indent);
        System.out.print("begin " + procName + "(");
        for (int i = 0; i < arguments.size(); i++) {
            System.out.print(arguments.get(i));
            if (i < arguments.size() - 1) System.out.print(", ");
        }
        System.out.println(");");
    }

    public void execute(Memory mem, Scanner dataScanner, Map<String, Function> procedures) {
        // Look up the procedure
        Function func = procedures.get(procName);
        if (func == null) {
            throw new RuntimeException("Runtime error: procedure '" + procName + "' not found");
        }
        
        // Check argument count matches parameter count
        if (arguments.size() != func.getParameters().size()) {
            throw new RuntimeException("Runtime error: procedure '" + procName + 
                "' expects " + func.getParameters().size() + " arguments but got " + arguments.size());
        }
        
        // Evaluate all arguments BEFORE entering new scope
        List<String> params = func.getParameters();
        List<Map<String, Integer>> argObjects = new ArrayList<>();
        List<String> argDefaultKeys = new ArrayList<>();
        
        for (int i = 0; i < arguments.size(); i++) {
            String arg = arguments.get(i);
            Map<String, Integer> argObj = mem.getObj(arg);
            String argDefaultKey = mem.getDefaultKey(arg);
            argObjects.add(argObj);
            argDefaultKeys.add(argDefaultKey);
        }
        
        // Create new frame
        mem.enterScope();
        
        // Bind formal parameters to actual arguments 
        for (int i = 0; i < params.size(); i++) {
            String param = params.get(i);
            
            // Declare the parameter as an object in the new scope
            mem.declareObj(param);
            
            // Set the parameter to point to the argument's object
            mem.setObjRef(param, argObjects.get(i));
            mem.setDefaultKey(param, argDefaultKeys.get(i));
        }
        
        // Execute the procedure body
        func.getBody().execute(mem, dataScanner, procedures);
        
        // Pop the frame
        mem.exitScope();
    }

    public String getProcName() {
        return procName;
    }

    public List<String> getArguments() {
        return arguments;
    }
}