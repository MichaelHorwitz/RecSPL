import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    public Node headNode;
    public ArrayList<Token> tokenList = new ArrayList<Token>();
    public int id;
    public Parser(ArrayList<Token> tokenList) {
        this.tokenList.addAll(tokenList);
        id = 0;
    }

    
    public boolean PROG() {  // PROG -> main GLOBVARS ALGO FUNCTIONS
        headNode = new Node("PROG");
        
        // Check for 'main'
        if (tokenList.size() > 0 && tokenList.get(0).name.equals("main")) {
            Node mainNode = new Node("main");
            mainNode.line = tokenList.get(0).line;
            headNode.childNodes.add(mainNode);
            tokenList.remove(0);  // Consume 'main'
            
            GLOBVARS(headNode);  // GLOBVARS is nullable, so it might do nothing
            ALGO(headNode);  // ALGO is also nullable, might do nothing
        
           FUNCTIONS(headNode);  // FUNCTIONS can also be nullable
    
            // After completing ALGO, check if there are more tokens (invalid case)
            if (tokenList.size() > 0) {
                System.err.println("Syntax Error: Unexpected token after program block termination: " + tokenList.get(0).name+ tokenList.get(0).line);
                System.exit(1);  // Terminate if there are extra tokens
            }
    
            return true;  // Successfully parsed the program
        } 
      
        return false;
    }
    
    public void ALGO(Node pNode) {  // ALGO ::= begin INSTRUC end
        if (tokenList.size() > 0 && tokenList.get(0).name.equals("begin")) {
            Node algoNode = new Node("ALGO");
            algoNode.line = tokenList.get(0).line;
            pNode.childNodes.add(algoNode);
            tokenList.remove(0);  // Consume 'begin'
    
            INSTRUC(algoNode);  // Parse the instructions, INSTRUC can be nullable
    
            if (tokenList.size() > 0 && tokenList.get(0).name.equals("end")) {
                Node endNode = new Node("end");
                endNode.line = tokenList.get(0).line;
                algoNode.childNodes.add(endNode);
                tokenList.remove(0);  // Consume 'end'
            } else {
                System.err.println("Syntax Error: 'end' expected.");
                System.exit(1);
            }
        } else {
            System.err.println("Syntax Error: 'begin' expected at line " + tokenList.get(0).line);
            System.exit(1);
        }
    }
    
            
    public void GLOBVARS(Node pNode) {  // GLOBVARS ::= VTYP VNAME , GLOBVARS | (nullable)
        // If tokenList is empty, treat GLOBVARS as nullable
        if (tokenList.size() == 0) {
            return;  // Allow GLOBVARS to be empty
        }
    
        // Check for a valid variable type: 'num' or 'text'
        if (tokenList.get(0).name.equals("num") || tokenList.get(0).name.equals("text")) {
            Node globVarsNode = new Node("GLOBVARS");
            pNode.childNodes.add(globVarsNode);
    
            VTYP(globVarsNode);  // Parse the type
            VNAME(globVarsNode); // Parse the variable name
    
            // Handle parentheses if present
            if (tokenList.size() > 0 && tokenList.get(0).name.equals("(")) {
                tokenList.remove(0);  // Consume '('
    
                // Skip over tokens inside the parentheses (e.g., arguments)
                while (tokenList.size() > 0 && !tokenList.get(0).name.equals(")")) {
                    tokenList.remove(0);  // Consume tokens inside the parentheses
                }
    
                // Ensure there's a closing parenthesis ')'
                if (tokenList.size() > 0 && tokenList.get(0).name.equals(")")) {
                    tokenList.remove(0);  // Consume ')'
                } else {
                    System.err.println("Syntax Error: Expected ')' after '(' in variable declaration.");
                    System.exit(1);
                }
            }
    
            // **Strictly Check for a Comma After Variable Declaration**
            if (tokenList.size() > 0 && tokenList.get(0).name.equals(",")) {
                Node commaNode = new Node(",");
                commaNode.line = tokenList.get(0).line;
                globVarsNode.childNodes.add(commaNode);
                tokenList.remove(0);  // Consume the comma
    
                // Recursively call GLOBVARS to parse the next variable declaration
                GLOBVARS(pNode);  // Call with the parent node to allow nesting
            } 
            // If 'begin' is found immediately after a variable declaration without a comma, it's an error
            else if (tokenList.size() > 0 && tokenList.get(0).name.equals("begin")) {
                System.err.println("Syntax Error: Expected ',' after global variable declaration before 'begin'.");
                System.exit(1);
            } 
            // If neither a comma nor 'begin' is found, it's an error
            else {
                System.err.println("Syntax Error: Expected ',' after global variable declaration, but found '" 
                                    + tokenList.get(0).name + "' at line " + tokenList.get(0).line);
                System.exit(1);
            }
        } 
        // If 'begin' is encountered without a preceding variable, it's valid (nullable GLOBVARS)
        else if (tokenList.get(0).name.equals("begin")) {
            return;  // GLOBVARS can be empty, so just return to move to ALGO
        } 
        // If an invalid token is encountered, throw a syntax error
        else {
            System.err.println("Syntax Error: Expected 'num' or 'text' as variable type, but found '" 
                                + tokenList.get(0).name + "' at line " + tokenList.get(0).line);
            System.exit(1);
        }
    }
    
    
    
    
    public void VNAME(Node pNode) {  // VNAME -> a token of Token-Class V
        if (tokenList.size() > 0 && tokenList.get(0).name.matches("V_[a-z]([a-z0-9])*")) {
            Node vnameNode = new Node("VNAME");
            vnameNode.id = id++;
            vnameNode.line = tokenList.get(0).line;
            vnameNode.childNodes.add(new Node(tokenList.get(0).name));
            pNode.childNodes.add(vnameNode);
            tokenList.remove(0);  // Consume the variable name
    
            // Now check if the next token is an opening parenthesis '('
            if (tokenList.size() > 0 && tokenList.get(0).name.equals("(")) {
                tokenList.remove(0);  // Consume '('
    
                // Skip over tokens inside the parentheses (e.g., variable arguments)
                while (tokenList.size() > 0 && !tokenList.get(0).name.equals(")")) {
                    tokenList.remove(0);  // Consume tokens inside parentheses
                }
    
                // Check and consume the closing parenthesis ')'
                if (tokenList.size() > 0 && tokenList.get(0).name.equals(")")) {
                    tokenList.remove(0);  // Consume ')'
                } else {
                    System.err.println("Syntax Error: Expected ')' after '(' in variable name.");
                    System.exit(1);
                }
            }
        } else {
            System.err.println("Syntax Error: Expected a valid variable name (VNAME), but found '" 
                                + tokenList.get(0).name + "' at line " + tokenList.get(0).line);
            System.exit(1);
        }
    }
    
    public void VTYP(Node pNode) {  // VTYP -> num | text
        if (tokenList.size() > 0 && (tokenList.get(0).name.equals("num") || tokenList.get(0).name.equals("text"))) {
            Node vtypNode = new Node("VTYP");
            vtypNode.line = tokenList.get(0).line;
            vtypNode.childNodes.add(new Node(tokenList.get(0).name));
            pNode.childNodes.add(vtypNode);
            tokenList.remove(0);  // Consume the type token
        } else {
            System.err.println("Syntax Error: Expected 'num' or 'text', but found '" + tokenList.get(0).name 
                                + "' at line " + tokenList.get(0).line);
            System.exit(1);
        }
    }
    
    public void INSTRUC(Node pNode) {  // INSTRUC ::= COMMAND ; INSTRUC | (nullable)
        if (tokenList.size() > 0 && !tokenList.get(0).name.equals("end")) {  // Don't continue past 'end'
            Node instrucNode = new Node("INSTRUC");
            pNode.childNodes.add(instrucNode);
    
            // Parse the command
            COMMAND(instrucNode);
    
            // After COMMAND, expect a semicolon
            if (tokenList.size() > 0 && tokenList.get(0).name.equals(";")) {
                Node semiColonNode = new Node(";");
                semiColonNode.line = tokenList.get(0).line;
                instrucNode.childNodes.add(semiColonNode);
                tokenList.remove(0);  // Consume the semicolon
    
                // Recursively call INSTRUC for further instructions
                INSTRUC(instrucNode);
            } else {
                System.err.println("Syntax Error: Expected ';' after command.");
                System.exit(1);
            }
        }
        // If no COMMAND is found, INSTRUC is nullable, so we return without adding anything
    }
    




   
   
    
    public void COMMAND(Node pNode) {
        if (tokenList.size() > 0) {
            String token = tokenList.get(0).name;
            Node commandNode = new Node("COMMAND");
            pNode.childNodes.add(commandNode);
    
            // Handle 'skip' and 'halt'
            if (token.equals("skip") || token.equals("halt")) {
                Node commandValueNode = new Node(token);
                commandValueNode.line = tokenList.get(0).line;
                commandNode.childNodes.add(commandValueNode);
                tokenList.remove(0);  // Consume 'skip' or 'halt'
            }
            // Handle 'print ATOMIC'
            else if (token.equals("print")) {
                Node printNode = new Node("print");
                printNode.line = tokenList.get(0).line;
                commandNode.childNodes.add(printNode);
                tokenList.remove(0);  // Consume 'print'
    
                ATOMIC(commandNode);  // Parse the ATOMIC value after 'print'
            }
            // Handle 'return ATOMIC'
            else if (token.equals("return")) {
                Node returnNode = new Node("return");
                returnNode.line = tokenList.get(0).line;
                commandNode.childNodes.add(returnNode);
                tokenList.remove(0);  // Consume 'return'
    
                ATOMIC(commandNode);  // Parse the ATOMIC value after 'return'
            }
            // Handle 'if COND then ALGO else ALGO' (BRANCH)
            else if (token.equals("if")) {
                BRANCH(commandNode);  // Parse the branching structure (if-else)
            }
            // Handle function call (CALL)
            else if (token.matches("F_[a-z]([a-z0-9])*")) {
                CALL(commandNode);  // Parse the function call
            }
            // Handle assignment (ASSIGN)
            else {
                ASSIGN(commandNode);  // Parse the assignment if it's not a call
            }
        }
    }
    
    public void BRANCH(Node pNode) {  // BRANCH ::= if COND then ALGO else ALGO
        if (tokenList.size() > 0 && tokenList.get(0).name.equals("if")) {
            Node branchNode = new Node("BRANCH");
            pNode.childNodes.add(branchNode);
            tokenList.remove(0);  // Consume 'if'
    
            // Parse the condition (COND)
            COND(branchNode);
    
            // Check for 'then'
            if (tokenList.size() > 0 && tokenList.get(0).name.equals("then")) {
                Node thenNode = new Node("then");
                thenNode.line = tokenList.get(0).line;
                branchNode.childNodes.add(thenNode);
                tokenList.remove(0);  // Consume 'then'
    
                // Parse the first algorithm (ALGO)
                ALGO(branchNode);
    
                // Check for 'else'
                if (tokenList.size() > 0 && tokenList.get(0).name.equals("else")) {
                    Node elseNode = new Node("else");
                    elseNode.line = tokenList.get(0).line;
                    branchNode.childNodes.add(elseNode);
                    tokenList.remove(0);  // Consume 'else'
    
                    // Parse the second algorithm (ALGO)
                    ALGO(branchNode);
                } else {
                    System.err.println("Syntax Error: 'else' expected after 'then'.");
                    System.exit(1);
                }
            } else {
                System.err.println("Syntax Error: 'then' expected after condition.");
                System.exit(1);
            }
        }
    }
    
    public void CALL(Node pNode) {  // CALL ::= FNAME( ATOMIC , ATOMIC , ATOMIC )
        if (tokenList.size() > 0) {
            // Parse the function name (everything before the '(')
            String token = tokenList.get(0).name;
            
            // Ensure it's a valid function name (without parentheses)
            if (token.matches("F_[a-z]([a-z0-9])*")) {
                Node callNode = new Node("CALL");
                pNode.childNodes.add(callNode);
        
                Node fnameNode = new Node("FNAME");
                fnameNode.childNodes.add(new Node(token));  // Add function name
                callNode.childNodes.add(fnameNode);
                tokenList.remove(0);  // Consume the function name
        
                // Expect an opening parenthesis '('
                if (tokenList.size() > 0 && tokenList.get(0).name.equals("(")) {
                    tokenList.remove(0);  // Consume '('
        
                    // Parse the first ATOMIC (must be un-nested)
                    if (isAtomic(tokenList.get(0).name)) {
                        Node atomicNode = new Node("ATOMIC");
                        atomicNode.childNodes.add(new Node(tokenList.get(0).name));  // Add the ATOMIC value
                        callNode.childNodes.add(atomicNode);
                        tokenList.remove(0);  // Consume the ATOMIC value
                    } else {
                        System.err.println("Syntax Error: Expected un-nested ATOMIC as first argument.");
                        System.exit(1);
                    }
        
                    // Expect a comma
                    if (tokenList.size() > 0 && tokenList.get(0).name.equals(",")) {
                        tokenList.remove(0);  // Consume the first comma
                    } else {
                        System.err.println("Syntax Error: Expected ',' after the first argument in function call.");
                        System.exit(1);
                    }
        
                    // Parse the second ATOMIC (must be un-nested)
                    if (isAtomic(tokenList.get(0).name)) {
                        Node atomicNode = new Node("ATOMIC");
                        atomicNode.childNodes.add(new Node(tokenList.get(0).name));  // Add the ATOMIC value
                        callNode.childNodes.add(atomicNode);
                        tokenList.remove(0);  // Consume the ATOMIC value
                    } else {
                        System.err.println("Syntax Error: Expected un-nested ATOMIC as second argument.");
                        System.exit(1);
                    }
        
                    // Expect a second comma
                    if (tokenList.size() > 0 && tokenList.get(0).name.equals(",")) {
                        tokenList.remove(0);  // Consume the second comma
                    } else {
                        System.err.println("Syntax Error: Expected ',' after the second argument in function call.");
                        System.exit(1);
                    }
        
                    // Parse the third ATOMIC (must be un-nested)
                    if (isAtomic(tokenList.get(0).name)) {
                        Node atomicNode = new Node("ATOMIC");
                        atomicNode.childNodes.add(new Node(tokenList.get(0).name));  // Add the ATOMIC value
                        callNode.childNodes.add(atomicNode);
                        tokenList.remove(0);  // Consume the ATOMIC value
                    } else {
                        System.err.println("Syntax Error: Expected un-nested ATOMIC as third argument.");
                        System.exit(1);
                    }
        
                    // Ensure there's a closing parenthesis ')'
                    if (tokenList.size() > 0 && tokenList.get(0).name.equals(")")) {
                        tokenList.remove(0);  // Consume ')'
                    } else {
                        System.err.println("Syntax Error: Expected ')' after third argument in function call.");
                        System.exit(1);
                    }
                } else {
                    System.err.println("Syntax Error: Expected '(' after function name.");
                    System.exit(1);
                }
            }
        }
    }
    
    // Helper method to validate un-nested ATOMIC expressions
    public boolean isAtomic(String token) {
        // Define the regex patterns
        String variablePattern = "V_[a-z]([a-z0-9]*)";       // Matches variable names
        String numberPattern = "-?[0-9]+(\\.[0-9]+)?";       // Matches integers and decimals
        String stringPattern = "\"[A-Z][a-z]{0,7}\"";         // Matches strings enclosed in double quotes
    
        // Check if the token matches any of the atomic patterns
        return token.matches(variablePattern) || 
               token.matches(numberPattern) || 
               token.matches(stringPattern);
    }
    

    // Helper method to validate un-nested ATOMIC expressions
    public boolean isValidAtomic() {
        String token = tokenList.get(0).name;
    
        // Define the regex patterns
        String variablePattern = "V_[a-z]([a-z0-9]*)";       // Matches variable names
        String numberPattern = "-?[0-9]+(\\.[0-9]+)?";       // Matches integers and decimals
        String stringPattern = "\"[A-Z][a-z]{0,7}\"";         // Matches strings enclosed in double quotes
    
        // Check if the token matches any of the atomic patterns
        return token.matches(variablePattern) || 
               token.matches(numberPattern) || 
               token.matches(stringPattern);
    }
    

    private void parseUnNestedAtomic(Node pNode) {
        if (tokenList.size() > 0) {
            String token = tokenList.get(0).name;
    
            // Handle VNAME (variable names starting with V_)
            if (token.matches("V_[a-z]([a-z0-9])*")) {
                Node varNode = new Node("VNAME");
                varNode.childNodes.add(new Node(token));  // Add the variable name as a child node
                pNode.childNodes.add(varNode);
                tokenList.remove(0);  // Consume the VNAME
            } 
            // Handle CONST (numeric constants or string literals)
            else if (token.matches("-?[0-9]+(\\.[0-9]+)?") || token.matches("\"[A-Z][a-z]{0,7}\"")) {
                Node constNode = new Node("CONST");
                constNode.childNodes.add(new Node(token));  // Add the constant value as a child node
                pNode.childNodes.add(constNode);
                tokenList.remove(0);  // Consume the constant
            } 
            // If it's neither a valid VNAME nor a CONST, throw a syntax error
            else {
                System.err.println("Syntax Error: Invalid ATOMIC value.");
                System.exit(1);
            }
        } else {
            System.err.println("Syntax Error: Unexpected end of input while parsing ATOMIC.");
            System.exit(1);
        }
    }
        
    public void ASSIGN(Node pNode) {  // ASSIGN ::= VNAME < input | VNAME = TERM
        if (tokenList.size() > 0) {
            Node assignNode = new Node("ASSIGN");  // Create a parent node for the assignment
            pNode.childNodes.add(assignNode);  // Add the assignment node to the parent node
    
            // Parse the variable name (VNAME)
            VNAME(assignNode);  // Parse the variable name and add it to the assignNode (single nesting)
    
            if (tokenList.size() > 0) {
                String token = tokenList.get(0).name;
    
                // Handle assignment with input: VNAME < input
                if (token.equals("<")) {
                    tokenList.remove(0);  // Consume '<'
    
                    Node inputNode = new Node("input");
                    inputNode.line = tokenList.get(0).line;
    
                    if (tokenList.size() > 0 && tokenList.get(0).name.equals("input")) {
                        assignNode.childNodes.add(inputNode);  // Add input node to the assignment
                        tokenList.remove(0);  // Consume 'input'
                    } else {
                        System.err.println("Syntax Error: Expected 'input' after '<'.");
                        System.exit(1);
                    }
                } 
                // Handle assignment with a term: VNAME = TERM
                else if (token.equals("=")) {
                    tokenList.remove(0);  // Consume '='
    
                    // Create a node for the '=' operator
                    Node equalNode = new Node("=");
                    assignNode.childNodes.add(equalNode);  // Add '=' node to the assignment
    
                    // Parse the term after '='
                    Node termNode = new Node("TERM");
                    assignNode.childNodes.add(termNode);  // Add the TERM node to the assignment
    
                    TERM(termNode);  // Parse the term
                } 
                else {
                    System.err.println("Syntax Error: Expected '< input' or '=' after VNAME.");
                    System.exit(1);
                }
            }
        }
    }
    
                    // Consume '='
    
                    // Create a node for the '=' operator
    
public void FNAME(Node pNode) {  // FNAME ::= a token of Token-Class F
  // Parse FNAME (function name)
if (tokenList.size() > 0 && tokenList.get(0).name.matches("F_[a-z]([a-z0-9])*")) {
    Node fnameNode = new Node("FNAME");
    
    // Create a child node for the actual function name (token name)
    Node functionName = new Node(tokenList.remove(0).name);  // Consume FNAME
    fnameNode.childNodes.add(functionName);  // Add the token name as a child node
    
    pNode.childNodes.add(fnameNode);  // Add the FNAME node to the parent node
} else {
    System.err.println("Syntax Error: Expected function name.");
    System.exit(1);
}
System.exit(1);
    }







public void printParseTree(Node head,FileWriter writer) throws IOException{
    //FileWriter writer=new FileWriter("SyntaxTree.xml");
    writer.write('<'+head.NodeName+'>');
    
    for(Node node:head.childNodes){
        if(node.childNodes.size()==0){
            writer.write(node.NodeName);
            writer.write("\n");
        }
        else{
            printParseTree(node, writer);
        }
    }
    

    writer.write("</"+head.NodeName+'>');
}
    
    public void ATOMIC(Node pNode) {  // ATOMIC ::= VNAME | CONST
        if (tokenList.size() > 0) {
            String token = tokenList.get(0).name;
    
            // Check if it's a valid VNAME (variable name)
            if (token.matches("V_[a-z]([a-z0-9])*")) {
                VNAME(pNode);  // Parse the VNAME
            } 
            // If it's not a VNAME, treat it as a CONST
            else {
                CONST(pNode);  // Parse the constant value
            }
        }
    }
     
    public void TERM(Node pNode) {  // TERM ::= ATOMIC | CALL | OP
        if (tokenList.size() > 0) {
            String token = tokenList.get(0).name;
    
            // Check if it's a valid ATOMIC value
            if (token.matches("V_[a-z]([a-z0-9])*") || token.matches("[0-9]+") || token.matches("\"[A-Za-z]+\"")) {
                ATOMIC(pNode);  // Parse ATOMIC value
            } 
            // Check if it's a function call (CALL)
            else if (token.matches("F_[a-z]([a-z0-9])*")) {
                CALL(pNode);  // Parse CALL
            } 
            // Otherwise, it must be an operation (OP)
            else {
                OP(pNode);  // Parse OP
            }
        }
    }
    
   
    
    
    public void OP(Node pNode) {  // OP ::= UNOP( ARG ) | BINOP( ARG , ARG )
        if (tokenList.size() > 0) {
            String token = tokenList.get(0).name;
    
            // Check if it's a unary operation (UNOP)
            if (token.equals("not") || token.equals("sqrt")) {
                UNOP(pNode);  // Call UNOP method
            } 
            // Otherwise, it must be a binary operation (BINOP)
            else {
                BINOP(pNode);  // Call BINOP method
            }
        }
    }
    
    public void UNOP(Node pNode) {  // UNOP ::= not | sqrt
        if (tokenList.size() > 0) {
            String token = tokenList.get(0).name;
    
            // Check for unary operations
            if (token.equals("not") || token.equals("sqrt")) {
                Node unopNode = new Node("UNOP");
                unopNode.line = tokenList.get(0).line;
                unopNode.childNodes.add(new Node(token));  // Add the operator (e.g., "not" or "sqrt")
                pNode.childNodes.add(unopNode);
                tokenList.remove(0);  // Consume the unary operator
    
                // Check for opening parenthesis
                if (tokenList.size() > 0 && tokenList.get(0).name.equals("(")) {
                    tokenList.remove(0);  // Consume '('
    
                    ARG(unopNode);  // Parse the argument (which could be ATOMIC or OP)
    
                    // Expect closing parenthesis
                    if (tokenList.size() > 0 && tokenList.get(0).name.equals(")")) {
                        tokenList.remove(0);  // Consume ')'
                    } else {
                        System.err.println("Syntax Error: Expected ')' after argument in UNOP.");
                        System.exit(1);
                    }
                } else {
                    System.err.println("Syntax Error: Expected '(' after unary operator.");
                    System.exit(1);
                }
            }
        }
    }
    
    
    
    public void BINOP(Node pNode) {  // BINOP ::= or | and | eq | grt | add | sub | mul | div
        if (tokenList.size() > 0) {
            String token = tokenList.get(0).name;
    
            // Check for binary operations
            if (token.equals("or") || token.equals("and") || token.equals("eq") || token.equals("grt") ||
                token.equals("add") || token.equals("sub") || token.equals("mul") || token.equals("div")) {
                Node binopNode = new Node("BINOP");
                binopNode.line = tokenList.get(0).line;
                binopNode.childNodes.add(new Node(token));  // Add the binary operator (e.g., "add", "sub")
                pNode.childNodes.add(binopNode);
                tokenList.remove(0);  // Consume the binary operator
    
                // Check for opening parenthesis
                if (tokenList.size() > 0 && tokenList.get(0).name.equals("(")) {
                    tokenList.remove(0);  // Consume '('
    
                    ARG(binopNode);  // Parse the first argument (which could be ATOMIC or OP)
    
                    // Expect comma between arguments
                    if (tokenList.size() > 0 && tokenList.get(0).name.equals(",")) {
                        tokenList.remove(0);  // Consume ','
    
                        ARG(binopNode);  // Parse the second argument (which could be ATOMIC or OP)
    
                        // Expect closing parenthesis
                        if (tokenList.size() > 0 && tokenList.get(0).name.equals(")")) {
                            tokenList.remove(0);  // Consume ')'
                        } else {
                            System.err.println("Syntax Error: Expected ')' after second argument in BINOP.");
                            System.exit(1);
                        }
                    } else {
                        System.err.println("Syntax Error: Expected ',' between arguments in BINOP.");
                        System.exit(1);
                    }
                } else {
                    System.err.println("Syntax Error: Expected '(' after binary operator.");
                    System.exit(1);
                }
            } else {
                System.err.println("Syntax Error: Unknown binary operator '" + token + "'");
                System.exit(1);
            }
        }
    }
    
    
   
    
    
    
    private boolean isBinaryOp(String token) {
        return token.equals("or") || token.equals("and") || token.equals("eq") || token.equals("grt") ||
               token.equals("add") || token.equals("sub") || token.equals("mul") || token.equals("div");
    }
    
    
    public void ARG(Node pNode) {  // ARG ::= ATOMIC | OP
        if (tokenList.size() > 0) {
            String token = tokenList.get(0).name;
    
            // Check if it's an ATOMIC value
            if (token.matches("V_[a-z]([a-z0-9])*") || token.matches("[0-9]+") || token.matches("\"[A-Za-z]+\"")) {
                ATOMIC(pNode);  // Parse ATOMIC value
            } 
            // If it's not an ATOMIC, it must be an OP (either UNOP or BINOP)
            else if (token.equals("not") || token.equals("sqrt")) {
                UNOP(pNode);  // Parse a unary operation
            } else if (token.equals("or") || token.equals("and") || token.equals("eq") || token.equals("grt") ||
                       token.equals("add") || token.equals("sub") || token.equals("mul") || token.equals("div")) {
                BINOP(pNode);  // Parse a binary operation
            } else {
                System.err.println("Syntax Error: Invalid argument '" + token + "'");
                System.exit(1);
            }
        }
    }
    
    
    
    public void COND(Node pNode) {  // COND ::= SIMPLE | COMPOSIT
        if (tokenList.size() > 0) {
            // Determine if the condition is SIMPLE or COMPOSIT
            if (isSimple()) {
                SIMPLE(pNode);  // Parse SIMPLE condition
            } else {
                COMPOSIT(pNode);  // Parse COMPOSIT condition
            }
        }
    }
    
    public void SIMPLE(Node pNode) {  // SIMPLE ::= BINOP(ATOMIC, ATOMIC)
        Node simpleNode = new Node("SIMPLE");
        pNode.childNodes.add(simpleNode);
    
        BINOP(simpleNode);  // Parse the binary operator
    }
    
    public void COMPOSIT(Node pNode) {  // COMPOSIT ::= BINOP(SIMPLE, SIMPLE) | UNOP(SIMPLE)
        if (tokenList.size() > 0) {
            String token = tokenList.get(0).name;
    
            // Check for binary operations
            if (isBinaryOperator(token)) {
                Node compositNode = new Node("COMPOSIT");
                pNode.childNodes.add(compositNode);
    
                // Consume the binary operator
                tokenList.remove(0);  // Remove the binary operator
    
                // Expect opening parenthesis for the BINOP
                if (tokenList.size() > 0 && tokenList.get(0).name.equals("(")) {
                    tokenList.remove(0);  // Consume '('
    
                    // Parse the first SIMPLE
                    SIMPLE(compositNode);  // Parse the first SIMPLE
    
                    // Expect a comma between SIMPLE arguments
                    if (tokenList.size() > 0 && tokenList.get(0).name.equals(",")) {
                        tokenList.remove(0);  // Consume ','
    
                        // Parse the second SIMPLE
                        SIMPLE(compositNode);  // Parse the second SIMPLE
    
                        // Expect closing parenthesis
                        if (tokenList.size() > 0 && tokenList.get(0).name.equals(")")) {
                            tokenList.remove(0);  // Consume ')'
                        } else {
                            System.err.println("Syntax Error: Expected ')' after second SIMPLE in COMPOSIT.");
                            System.exit(1);
                        }
                    } else {
                        System.err.println("Syntax Error: Expected ',' between SIMPLE arguments in COMPOSIT.");
                        System.exit(1);
                    }
                } else {
                    System.err.println("Syntax Error: Expected '(' after binary operator in COMPOSIT.");
                    System.exit(1);
                }
            } else {
                // If it's not a binary operator, check for UNOP
                if (token.equals("not") || token.equals("sqrt")) {
                    UNOP(pNode);  // If it's a unary operator, parse it
                } else {
                    System.err.println("Syntax Error: Invalid operator for COMPOSIT: '" + token + "'");
                    System.exit(1);
                }
            }
        }
    }
    
    // Helper method to check for valid binary operators
    private boolean isBinaryOperator(String token) {
        return token.equals("or") || token.equals("and") || token.equals("eq") || token.equals("grt") ||
               token.equals("add") || token.equals("sub") || token.equals("mul") || token.equals("div");
    }
    
    
    
    private boolean isSimple() {
        // Check if there are enough tokens to form a SIMPLE structure: BINOP(atomic, atomic)
        if (tokenList.size() >= 6) {
            String binOp = tokenList.get(0).name;            // Binary operator
            String openingParen = tokenList.get(1).name;     // Opening parenthesis '('
            String firstToken = tokenList.get(2).name;       // First atomic value
            String comma = tokenList.get(3).name;            // Comma separator
            String secondToken = tokenList.get(4).name;      // Second atomic value
            String closingParen = tokenList.get(5).name;     // Closing parenthesis ')'
    
            // Check if the first and second tokens are atomic (variable or number)
            boolean firstIsAtomic = firstToken.matches("V_[a-z]([a-z0-9])*") || firstToken.matches("[0-9]+") || firstToken.matches("\"[A-Za-z]+\"");
            boolean secondIsAtomic = secondToken.matches("V_[a-z]([a-z0-9])*") || secondToken.matches("[0-9]+") || secondToken.matches("\"[A-Za-z]+\"");
    
            // Check if the operator is a valid binary operator
            boolean isBinaryOperator = binOp.equals("or") || binOp.equals("and") || binOp.equals("eq") || binOp.equals("grt") ||
                                       binOp.equals("add") || binOp.equals("sub") || binOp.equals("mul") || binOp.equals("div");
    
            // Ensure the structure is BINOP(ATOMIC, ATOMIC) and parentheses are present
            return isBinaryOperator && openingParen.equals("(") && firstIsAtomic && comma.equals(",") &&
                   secondIsAtomic && closingParen.equals(")");
        }
        return false;
    }
    
    
    
    public void CONST(Node pNode) {  // CONST ::= a token of Token-Class N (numbers) or T (text)
        if (tokenList.size() > 0) {
            String token = tokenList.get(0).name;
    
            // Check if it's a valid number (Token-Class N)
            if (token.matches("-?[0-9]+(\\.[0-9]+)?")) {  // Token-Class N (numbers)
                Node constNode = new Node("CONST");
                constNode.line = tokenList.get(0).line;
                constNode.childNodes.add(new Node(token));
                pNode.childNodes.add(constNode);
                tokenList.remove(0);  // Consume the number
            } 
            // Check if it's a valid text (Token-Class T)
            else if (token.matches("\"[A-Z][a-z]{0,7}\"")) {  // Token-Class T (text)
                Node constNode = new Node("CONST");
                constNode.line = tokenList.get(0).line;
                constNode.childNodes.add(new Node(token));
                pNode.childNodes.add(constNode);
                tokenList.remove(0);  // Consume the text
            } 
            else {
                System.err.println("Syntax Error: Expected a number (Token-Class N) or text (Token-Class T), but found '" + token + "'");
                System.exit(1);
            }
        }
    }
     
    public void FUNCTIONS(Node pNode) {
        
      
      
        if (tokenList.size() > 0 && (tokenList.get(0).name.equals("num") || tokenList.get(0).name.equals("void"))) {
            Node functionsNode = new Node("FUNCTIONS");
            pNode.childNodes.add(functionsNode);
            DECL(functionsNode);  // Parse the declaration
            FUNCTIONS(functionsNode);  // Recursive call to parse more functions
        }
        // Nullable, so no else block is needed
    }
    public void DECL(Node pNode) {
       
        Node declNode = new Node("DECL");
       
        pNode.childNodes.add(declNode);
        HEADER(declNode);  // Parse the header
        BODY(declNode);    // Parse the body
    }
    public void HEADER(Node pNode) {
        Node headerNode = new Node("HEADER");
        pNode.childNodes.add(headerNode);
    
        // Parse FTYP (num or void)
        FTYP(headerNode);  // Add return type directly as child of HEADER
    
        // Parse FNAME (function name)
        if (tokenList.size() > 0 && tokenList.get(0).name.matches("F_[a-z]([a-z0-9])*")) {
            Node fnameNode = new Node("FNAME");
            Node functionName = new Node(tokenList.remove(0).name);  // Consume FNAME
            fnameNode.childNodes.add(functionName);  // Add the function name as a child of FNAME
            headerNode.childNodes.add(fnameNode);    // Add FNAME as a child of HEADER
        } else {
            System.err.println("Syntax Error: Expected function name.");
            System.exit(1);
        }
    
        // Parse parameters (VNAME1, VNAME2, VNAME3)
        if (tokenList.size() > 0 && tokenList.get(0).name.equals("(")) {
            tokenList.remove(0);  // Consume '('
    
            VNAME(headerNode);  // 1st parameter
            expectComma();
            VNAME(headerNode);  // 2nd parameter
            expectComma();
            VNAME(headerNode);  // 3rd parameter
    
            if (tokenList.size() > 0 && tokenList.get(0).name.equals(")")) {
                tokenList.remove(0);  // Consume ')'
            } else {
                System.err.println("Syntax Error: Expected ')'.");
                System.exit(1);
            }
        } else {
            System.err.println("Syntax Error: Expected '('.");
            System.exit(1);
        }
    }
    
    private boolean isUnNestedAtomic() {
        String token = tokenList.size() > 0 ? tokenList.get(0).name : "";
        // ATOMIC can only be a VNAME (Variable) or a CONST (Constant number or string)
        return token.matches("V_[a-z]([a-z0-9])*") || token.matches("[0-9]+") || token.matches("\"[^\"]*\"");
    }
    
    private void expectComma() {
        if (tokenList.size() > 0 && tokenList.get(0).name.equals(",")) {
            tokenList.remove(0);  // Consume ','
        } else {
            System.err.println("Syntax Error: Expected ','.");
            System.exit(1);
        }
    }
    public void FTYP(Node pNode) {
       // Parse FTYP (function return type)
if (tokenList.size() > 0 && (tokenList.get(0).name.equals("num") || tokenList.get(0).name.equals("void"))) {
    Node ftypNode = new Node("FTYP");
    
    // Create a child node for the actual type (token name 'num' or 'void')
    Node returnType = new Node(tokenList.remove(0).name);  // Consume 'num' or 'void'
    ftypNode.childNodes.add(returnType);  // Add the return type as a child node
    
    pNode.childNodes.add(ftypNode);  // Add the FTYP node to the parent node
} else {
    System.err.println("Syntax Error: Expected function return type ('num' or 'void').");
    System.exit(1);
}

    }
    public void PROLOG(Node pNode) {
        if (tokenList.size() > 0 && tokenList.get(0).name.equals("{")) {
            Node prologNode = new Node("PROLOG");
            prologNode.line = tokenList.get(0).line;
            pNode.childNodes.add(prologNode);
            tokenList.remove(0);  // Consume '{'
        } else {
            System.err.println("Syntax Error: Expected '{' at the start of the body.");
            System.exit(1);
        }
    }
    public void LOCVARS(Node pNode) {
        Node locvarsNode = new Node("LOCVARS");
        pNode.childNodes.add(locvarsNode);
    
        // Assuming we have three local variables like VTYP VNAME, VTYP VNAME, VTYP VNAME
        for (int i = 0; i < 3; i++) {
            VTYP(locvarsNode);  // Parse the variable type
            VNAME(locvarsNode);  // Parse the variable name
            if (i < 3) {
                expectComma();
            }
        }
    }
    public void EPILOG(Node pNode) {
        if (tokenList.size() > 0 && tokenList.get(0).name.equals("}")) {
            Node epilogNode = new Node("EPILOG");
            epilogNode.line = tokenList.get(0).line;
            pNode.childNodes.add(epilogNode);
            tokenList.remove(0);  // Consume '}'
        } else {
            System.err.println("Syntax Error: Expected '}' at the end of the body.");
            System.exit(1);
        }
    }
    public void SUBFUNCS(Node pNode) {
        Node subfuncsNode = new Node("SUBFUNCS");
        pNode.childNodes.add(subfuncsNode);
        FUNCTIONS(subfuncsNode);  // Call the recursive FUNCTIONS rule
    }
    
    public void BODY(Node pNode) {
        Node bodyNode = new Node("BODY");
        pNode.childNodes.add(bodyNode);
    
        PROLOG(bodyNode);  // Parse '{'
    
        LOCVARS(bodyNode);  // Parse local variables (nullable)
    
        ALGO(bodyNode);  // Parse the algorithm (this should include 'begin' and 'end')
    
        EPILOG(bodyNode);  // Parse '}'
        
        SUBFUNCS(bodyNode);  // Parse sub-functions if any
    
        // Ensure 'end' is present after SUBFUNCS
        if (tokenList.size() > 0 && tokenList.get(0).name.equals("end")) {
            Node endNode = new Node("end");
            endNode.line = tokenList.get(0).line;
            bodyNode.childNodes.add(endNode);
            tokenList.remove(0);  // Consume 'end'
        } else {
            System.err.println("Syntax Error: 'end' expected at end of body of function" ); //tokenList.get(0).line);
            System.exit(1);
        }
    }
    
                




}
    

   