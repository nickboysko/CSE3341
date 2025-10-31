import java.io.*;
import java.util.*;

class CoreScanner {

    private BufferedReader reader;
    private int currentChar;        // current character 
    private Core currentToken;      // current token
    private String idValue;         // value for ID
    private int constValue;         // value for CONST
    private String stringValue;     // value for STRING

    private boolean eofReached = false;

    // Keyword table
    private static final Map<String, Core> keywords = new HashMap<>();
    static {
        keywords.put("and", Core.AND);
        keywords.put("begin", Core.BEGIN);
        keywords.put("case", Core.CASE);
        keywords.put("do", Core.DO);
        keywords.put("else", Core.ELSE);
        keywords.put("end", Core.END);
        keywords.put("for", Core.FOR);
        keywords.put("if", Core.IF);
        keywords.put("in", Core.IN);
        keywords.put("integer", Core.INTEGER);
        keywords.put("is", Core.IS);
        keywords.put("new", Core.NEW);
        keywords.put("not", Core.NOT);
        keywords.put("object", Core.OBJECT);
        keywords.put("or", Core.OR);
        keywords.put("print", Core.PRINT);
        keywords.put("procedure", Core.PROCEDURE);
        keywords.put("read", Core.READ);
        keywords.put("return", Core.RETURN);
        keywords.put("then", Core.THEN);
    }

    // Initialize the scanner
    CoreScanner(String filename) {
        try {
            reader = new BufferedReader(new FileReader(filename));
            advance();     // load first character into currentChar
            nextToken();   // set the first token into currentToken
        } catch (FileNotFoundException e) {
            System.out.println("Cannot open file " + filename);
            currentToken = Core.ERROR;
            eofReached = true;
        } catch (IOException e) { // unexpected from advance()
            System.out.println("I/O error while opening file");
            currentToken = Core.ERROR;
            eofReached = true;
        }
    }

    // Advance reader by one character, set currentChar, set eofReached if -1
    private void advance() throws IOException {
        currentChar = reader.read();
        if (currentChar == -1) {
            eofReached = true;
        }
    }

    // Skip whitespace (space, tab, newline, carriage return)
    private void skipWhitespace() {
        try {
            while (!eofReached && Character.isWhitespace(currentChar)) {
                advance();
            }
        } catch (IOException e) {
            System.out.println("I/O error while skipping whitespace");
            currentToken = Core.ERROR;
            eofReached = true;
        }
    }

    // Advance to the next token
    public void nextToken() {
        if (currentToken == Core.ERROR || currentToken == Core.EOS) {
            return;
        }

        // Reset token values
        idValue = null;
        stringValue = null;
        constValue = 0;

        try {
            skipWhitespace();

            if (eofReached) {
                currentToken = Core.EOS;
                closeReader();
                return;
            }

            //Identifier or Keyword
            if (Character.isLetter(currentChar)) {
                StringBuilder sb = new StringBuilder();
                while (!eofReached && Character.isLetterOrDigit(currentChar)) {
                    sb.append((char) currentChar);
                    advance();
                }
                String word = sb.toString();
                Core kw = keywords.get(word);
                if (kw != null) {
                    currentToken = kw;
                } else {
                    currentToken = Core.ID;
                    idValue = word;
                }
                return;
            }

            //Constant
            if (Character.isDigit(currentChar)) {
                StringBuilder sb = new StringBuilder();
                while (!eofReached && Character.isDigit(currentChar)) {
                    sb.append((char) currentChar);
                    advance();
                }
                String digits = sb.toString();

                // Leading zero check
                if (digits.length() > 1 && digits.charAt(0) == '0') {
                    System.out.println("Invalid constant (leading zero): " + digits);
                    currentToken = Core.ERROR;
                    return;
                }

                // Parse integer and check range 0-8191
                try {
                    int val = Integer.parseInt(digits);
                    if (val < 0 || val > 8191) {
                        System.out.println("Constant out of range: " + digits);
                        currentToken = Core.ERROR;
                        return;
                    } else {
                        currentToken = Core.CONST;
                        constValue = val;
                        return;
                    }
                } catch (NumberFormatException ex) {
                    // number too large to parse or invalid format
                    System.out.println("Invalid constant: " + digits);
                    currentToken = Core.ERROR;
                    return;
                }
            }

            //String
            if (currentChar == '\'') {
                // consume opening quote
                advance();
                StringBuilder sb = new StringBuilder();
                while (!eofReached && currentChar != '\'') {
                    sb.append((char) currentChar);
                    advance();
                }
                if (eofReached) {
                    System.out.println("Unterminated string");
                    currentToken = Core.ERROR;
                    return;
                } else {
                    advance(); // consume closing '
                    currentToken = Core.STRING;
                    stringValue = sb.toString();
                    return;
                }
            }

            //Symbols
            switch (currentChar) {
                case '+':
                    currentToken = Core.ADD; advance(); return;
                case '-':
                    currentToken = Core.SUBTRACT; advance(); return;
                case '*':
                    currentToken = Core.MULTIPLY; advance(); return;
                case '/':
                    currentToken = Core.DIVIDE; advance(); return;
                case '=':
                    advance();
                    if (!eofReached && currentChar == '=') {
                        currentToken = Core.EQUAL;
                        advance();
                    } else {
                        currentToken = Core.ASSIGN;
                    }
                    return;
                case '<':
                    currentToken = Core.LESS; advance(); return;
                case ':':
                    currentToken = Core.COLON; advance(); return;
                case ';':
                    currentToken = Core.SEMICOLON; advance(); return;
                case '.':
                    currentToken = Core.PERIOD; advance(); return;
                case ',':
                    currentToken = Core.COMMA; advance(); return;
                case '(':
                    currentToken = Core.LPAREN; advance(); return;
                case ')':
                    currentToken = Core.RPAREN; advance(); return;
                case '[':
                    currentToken = Core.LSQUARE; advance(); return;
                case ']':
                    currentToken = Core.RSQUARE; advance(); return;
                case '{':
                    currentToken = Core.LCURL; advance(); return;
                case '}':
                    currentToken = Core.RCURL; advance(); return;
                default:
                    char bad = (char) currentChar;
                    System.out.println("Invalid character '" + bad + "'");
                    currentToken = Core.ERROR;
                    advance(); 
                    return;
            }
        } catch (IOException e) {
            System.out.println("I/O error while scanning");
            currentToken = Core.ERROR;
            try { closeReader(); } catch (Exception ignored) {}
            return;
        }
    }

    // Return the current token
    public Core currentToken() {
        return currentToken;
    }

	// Return the identifier string
    public String getId() {
        return idValue;
    }

	// Return the constant value
    public int getConst() {
        return constValue;
    }
	
	// Return the character string
    public String getString() {
        return stringValue;
    }

    // Close the reader if open
    private void closeReader() {
        if (reader != null) {
            try { reader.close(); } catch (IOException ignored) {}
            reader = null;
        }
    }

}
