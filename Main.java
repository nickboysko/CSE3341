import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Main <program.code> <program.data>");
            return;
        }

        String codeFile = args[0];
        String dataFile = args[1];

        // Initialize the scanner for the Core program
        CoreScanner scanner = new CoreScanner(codeFile);
        if (scanner.currentToken() == Core.ERROR) {
            return;
        }

        // Parse the procedure
        Parser parser = new Parser(scanner);
        Procedure proc = parser.parseProcedure();

        SymbolTable st = new SymbolTable();
        proc.semanticCheck(st);

        Memory mem = new Memory();

        try (Scanner dataScanner = new Scanner(new File(dataFile))) {
            proc.execute(mem, dataScanner);
        } catch (FileNotFoundException e) {
            System.out.println("Runtime error: could not open data file " + dataFile);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
