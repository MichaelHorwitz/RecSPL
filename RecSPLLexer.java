import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecSPLLexer {

    public ArrayList<Token> tokens = new ArrayList<Token>();   // List of tokens
    public ArrayList<String> kw = new ArrayList<String>();     // Keywords
    public ArrayList<Character> sTerminals = new ArrayList<Character>();  // Character terminals  

    // Constructor to initialize keywords and terminals
    public RecSPLLexer() {
        // Define keywords from the RecSPL grammar
        kw.add("main");
        kw.add("begin");
        kw.add("end");
        kw.add("if");
        kw.add("then");
        kw.add("else");
        kw.add("print");
        kw.add("halt");
        kw.add("skip");
        kw.add("input");
        kw.add("num");
        kw.add("text");
        kw.add("void");
        kw.add("def");
        kw.add("add");
        kw.add("sub");
        kw.add("mul");
        kw.add("div");
        kw.add("and");
        kw.add("or");
        kw.add("eq");
        kw.add("grt");
        kw.add("not");
        kw.add("sqrt");
        kw.add("return");
        // Define character terminals (symbols)
        sTerminals.add(';');
        sTerminals.add('=');
        sTerminals.add('(');  // Added to handle parentheses
        sTerminals.add(')');
        sTerminals.add('{');
        sTerminals.add('}');
        sTerminals.add(',');
        sTerminals.add('<');
        sTerminals.add('>');
    }

    // Method to read and tokenize the input
    public boolean readFile(String filename) throws Exception {
        File file = new File(filename);
        Scanner scan = new Scanner(file);

        int r = 1;  // Line number
        String ls = "";  // Whole line string

        // Patterns for Token-Class F (Function Names), Token-Class V (Variable Names), Token-Class T (Text Strings), and Token-Class N (Numbers)
        Pattern functionPattern = Pattern.compile("F_[a-z]([a-z]|[0-9])*");
        Pattern variablePattern = Pattern.compile("V_[a-z]([a-z]|[0-9])*");
        Pattern textPattern = Pattern.compile("\"[A-Z][a-z]{0,7}\"");
        Pattern numberPattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");  // Updated for Token-Class N

        // Process each line from the file
        while (scan.hasNextLine()) {
            ls = scan.nextLine();
            int c = 0;
            String buf = "";  // String buffer

            while (c < ls.length()) {
                buf = "";

                // Handle string literals (Token-Class T)
                if (ls.charAt(c) == '"') {
                    int start = c;
                    c++;
                    while (c < ls.length() && ls.charAt(c) != '"') {
                        buf += ls.charAt(c++);
                    }
                    if (c < ls.length() && ls.charAt(c) == '"') {
                        buf = '"' + buf + '"';
                        Matcher matcher = textPattern.matcher(buf);
                        if (matcher.matches()) {
                            tokens.add(new Token(buf, r));
                            c++;
                        } else {
                            System.out.println("Lexical Error on line " + r + ": Invalid string format " + buf);
                            scan.close();
                            return false;
                        }
                    } else {
                        System.out.println("Lexical Error on line " + r + ": Unterminated string");
                        scan.close();
                        return false;
                    }
                } 
                // Handle function names (Token-Class F)
                else if (ls.startsWith("F_", c)) {
                    buf = "F_";  // Set buf to "F_" directly
                    c += 2;  // Skip over "F_"
                    
                    // Append the rest of the function name (allowing letters and digits)
                    while (c < ls.length() && Character.isLetterOrDigit(ls.charAt(c))) {
                        buf += ls.charAt(c++);
                    }
                
                    // Match against the function pattern
                    Matcher matcher = functionPattern.matcher(buf);
                    if (matcher.matches()) {
                        tokens.add(new Token(buf, r));  // Add valid function name as a token
                    } else {
                        System.out.println("Lexical Error on line " + r + ": Invalid function name " + buf);
                        scan.close();
                        return false;
                    }
                
                    // Ignore any white spaces after the function name
                    while (c < ls.length() && Character.isWhitespace(ls.charAt(c))) {
                        c++;  // Skip over white spaces
                    }
                
                    // Now handle the opening parenthesis '(' and the arguments
                    if (c < ls.length() && ls.charAt(c) == '(') {
                        tokens.add(new Token("(", r));  // Add '(' as a token
                        c++;
                    } else {
                        System.out.println("Lexical Error on line " + r + ": Missing opening parenthesis after function " + buf);
                        scan.close();
                        return false;
                    }
                    
                    // You can now proceed with parsing the arguments if needed
                    // For example, loop over the characters to handle function arguments here
                }
                
                // Handle variable names (Token-Class V)
                else if (ls.startsWith("V_", c)) {
                    buf = "V_";  // Set buf to "V_" directly
                    c += 2;  // Skip over "V_"
                    while (c < ls.length() && (Character.isLetterOrDigit(ls.charAt(c)))) {
                        buf += ls.charAt(c++);  // Append remaining characters to buf
                    }
                    
                    Matcher matcher = variablePattern.matcher(buf);
                    if (matcher.matches()) {
                        tokens.add(new Token(buf, r));
                    } else {
                        System.out.println("Lexical Error on line " + r + ": Invalid variable name " + buf);
                        scan.close();
                        return false;
                    }
                }
                // Handle identifiers (such as 'banda' in F_bad(banda))
                else if (Character.isLetter(ls.charAt(c))) {
                    while (c < ls.length() && Character.isLetterOrDigit(ls.charAt(c))) {
                        buf += ls.charAt(c++);
                    }
                    tokens.add(new Token(buf, r));
                }
                // Handle closing parenthesis ')'
                else if (ls.charAt(c) == ')') {
                    tokens.add(new Token(")", r));
                    c++;
                }
                // Handle numbers (Token-Class N)
                else if (Character.isDigit(ls.charAt(c)) || ls.charAt(c) == '-') {
                    buf += ls.charAt(c++);
                    while (c < ls.length() && (Character.isDigit(ls.charAt(c)) || ls.charAt(c) == '.')) {
                        buf += ls.charAt(c++);
                    }
                    Matcher matcher = numberPattern.matcher(buf);
                    if (matcher.matches()) {
                        tokens.add(new Token(buf, r));
                    } else {
                        System.out.println("Lexical Error on line " + r + ": Invalid number format " + buf);
                        scan.close();
                        return false;
                    }
                } 
                // Handle character terminals
                else if (sTerminals.contains(ls.charAt(c))) {
                    tokens.add(new Token(ls.charAt(c++) + "", r));
                } 
                // Handle whitespace and tabs
                else if (ls.charAt(c) == ' ' || ls.charAt(c) == '\t') {
                    c++;
                } 
                // Handle unknown characters
                else {
                    System.out.println("Lexical Error on line " + r + ": Unknown token " + ls.charAt(c));
                    scan.close();
                    return false;
                }
            }
            r++;
        }
        scan.close();
        return true;
    }

    // Method to write tokens to a file
    public void Tokenize() {
        try {
            FileWriter myWriter = new FileWriter("LexerOutput.txt");
            for (Token token : tokens) {
                myWriter.write(token.name + "\n");
            }
            myWriter.close();
            System.out.println("Lexer output written to LexerOutput.txt");
        } catch (IOException e) {
            System.out.println("A Lexical Error occurred.");
            e.printStackTrace();
        }
    }

    // Main method for testing the lexer
    public static void main(String[] args) throws Exception {
        RecSPLLexer lexer = new RecSPLLexer();
        if (lexer.readFile("input.txt")) {  // Use the input.txt file for testing
            lexer.Tokenize();
        }
    }
}

// Token class definition
class Token {
    String name;
    int line;

    public Token(String name, int line) {
        this.name = name;
        this.line = line;
    }
}
