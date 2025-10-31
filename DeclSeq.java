import java.util.*;

class DeclSeq implements Node {
    private final ParserHelper P;
    private List<Node> items; 

    DeclSeq(CoreScanner s) { 
        this.P = new ParserHelper(s); 
        this.items = new ArrayList<>();
    }

    @Override
    public void parse() {
        while (P.token() == Core.INTEGER || P.token() == Core.OBJECT || P.token() == Core.PROCEDURE) {
            if (P.token() == Core.PROCEDURE) {
                Function func = new Function(P.scanner());
                func.parse();
                items.add(func);
            } else {
                Decl decl = new Decl(P.scanner());
                decl.parse();
                items.add(decl);
            }
        }
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        for (Node item : items) {
            item.semanticCheck(st);
        }
    }

    @Override
    public void print(int indent) {
        for (Node item : items) {
            item.print(indent);
        }
    }

    public void execute(Memory mem) {
        for (Node item : items) {
            if (item instanceof Decl) {
                ((Decl) item).execute(mem);
            }
            // Functions are not executed during declaration
        }
    }

    public Map<String, Function> extractFunctions() {
        Map<String, Function> functions = new HashMap<>();
        for (Node item : items) {
            if (item instanceof Function) {
                Function func = (Function) item;
                if (functions.containsKey(func.getName())) {
                    SymbolTable.error("duplicate procedure name: " + func.getName());
                }
                functions.put(func.getName(), func);
            }
        }
        return functions;
    }
}