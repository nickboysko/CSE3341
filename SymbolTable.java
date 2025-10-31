import java.util.*;

enum VarType { INTEGER, OBJECT }

class SymbolTable {
    private final Deque<Map<String, VarType>> scopes = new ArrayDeque<>();

    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    public void exitScope() {
        scopes.pop();
    }

    public void declare(String id, VarType type) {
        Map<String, VarType> top = scopes.peek();
        if (top == null) {
            throw new IllegalStateException("No scope to declare in");
        }
        if (top.containsKey(id)) {
            error("duplicate declaration of " + id);
        }
        top.put(id, type);
    }

    public boolean isDeclared(String id) {
        return lookup(id) != null;
    }

    public VarType lookup(String id) {
        for (Map<String, VarType> m : scopes) {
            if (m.containsKey(id)) return m.get(id);
        }
        return null;
    }

    public static void requireDeclared(SymbolTable st, String id) {
        if (!st.isDeclared(id)) error("undeclared identifier " + id);
    }

    public static void requireType(SymbolTable st, String id, VarType want) {
        requireDeclared(st, id);
        VarType got = st.lookup(id);
        if (got != want) error("type mismatch for " + id + " (expected " + want + ")");
    }

    static void error(String msg) {
        System.out.println("ERROR: " + msg);
        System.exit(1);
    }

    public VarType lookupType(String id) {
        VarType t = lookup(id);
        if (t == null) {
            error("undeclared identifier " + id);
        }
        return t;
    }
}
