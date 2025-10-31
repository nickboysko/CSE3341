import java.util.*;

class Stmt implements Node {
    private final ParserHelper P;
    private Node impl;

    Stmt(CoreScanner s) { this.P = new ParserHelper(s); }

    static boolean startsStmt(Core t) {
        return t == Core.ID || t == Core.IF || t == Core.FOR || t == Core.PRINT
                || t == Core.READ || t == Core.INTEGER || t == Core.OBJECT || t == Core.BEGIN;
    }

    @Override
    public void parse() {
        Core t = P.token();
        switch (t) {
            case ID: impl = new Assign(P.scanner()); break;
            case IF: impl = new If(P.scanner()); break;
            case FOR: impl = new Loop(P.scanner()); break;
            case PRINT: impl = new PrintStmt(P.scanner()); break;
            case READ: impl = new ReadStmt(P.scanner()); break;
            case INTEGER: case OBJECT: impl = new Decl(P.scanner()); break;
            case BEGIN: impl = new Call(P.scanner()); break;
            default: P.fail("expected a statement"); return;
        }
        impl.parse();
    }

    @Override
    public void semanticCheck(SymbolTable st) {
        impl.semanticCheck(st);
    }

    @Override
    public void print(int indent) {
        impl.print(indent);
    }

    public void execute(Memory mem, Scanner dataScanner, Map<String, Function> procedures) {
        if (impl instanceof Assign) {
            ((Assign) impl).execute(mem);
        } else if (impl instanceof If) {
            ((If) impl).execute(mem, dataScanner, procedures);
        } else if (impl instanceof Loop) {
            ((Loop) impl).execute(mem, dataScanner, procedures);
        } else if (impl instanceof PrintStmt) {
            ((PrintStmt) impl).execute(mem);
        } else if (impl instanceof ReadStmt) {
            ((ReadStmt) impl).execute(mem, dataScanner);
        } else if (impl instanceof Decl) {
            ((Decl) impl).execute(mem);
        } else if (impl instanceof Call) {
            ((Call) impl).execute(mem, dataScanner, procedures);
        } else {
            throw new RuntimeException("Unknown statement type during execution");
        }
    }

    public boolean isCall() {
        return impl instanceof Call;
    }

    public Call getCall() {
        return (Call) impl;
    }
}