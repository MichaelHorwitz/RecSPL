import java.util.HashMap;
import java.util.Stack;
import java.util.ArrayList;

class TypeChecker {
    SymbolTable symbolTable; // Your existing symbol table

    // Entry point for type checking a program
    public boolean typeCheckProg(Node prog) {
        return typeCheck(prog) && typeCheckAlgo(prog) && typeCheckFunctions(prog);
    }

    // Type check global variables
    public boolean typeCheck(Node root) {
        if (root == null) {
            return false; // Handle null case
        }
        
        // Ensure we're processing the PROG node
        if (root.NodeName.equals("PROG")) {
            System.out.println("Type checking PROG");
    
            // Loop through the children of PROG
            for (Node child : root.childNodes) {
                if (child.NodeName.equals("GLOBVARS")) {
                    // Type check each GLOBVARS node
                    if (!typeCheckGLOVARS1(child)) {
                        return false; // Return false if any GLOBVARS fails
                    }
                }
            }
            return true; // All GLOBVARS passed type checking
        }
        
        System.out.println("Error: Expected PROG node, found: " + root.NodeName);
        return false;
    }
    
    private boolean typeCheckGLOVARS1(Node globVarsNode) {
        String varName = null;
        String varType = null;
    
        // Ensure we are processing only a GLOBVARS node
        if (!globVarsNode.NodeName.equals("GLOBVARS")) {
            System.out.println("Error: Expected GLOBVARS node, found: " + globVarsNode.NodeName);
            return false;
        }
    
        // Loop through the child nodes of the GLOBVARS node
        for (Node child : globVarsNode.childNodes) {
            if (child.NodeName.equals("VTYP")) {
                varType = getChildValue(child).trim();
                while (varType.endsWith(":")) {
                    varType = varType.substring(0, varType.length() - 1);
                }
                System.out.println("Variable TYPE: " + varType);
            } 
            else if (child.NodeName.equals("VNAME")) {
                varName = getChildValue(child).trim();
                while (varName.endsWith(":")) {
                    varName = varName.substring(0, varName.length() - 1);
                }
                System.out.println("Variable NAME: " + varName);
                
                // Retrieve the variable properties from the symbol table
                VariableProps varProps = getVariableProps(varName);
                
                // Check if varProps is not null and compare types
                if (varProps != null) {
                    System.out.println("Found Variable: " + varProps.oldName + " with Type: " + varProps.varType);
                    if (!varProps.varType.equals(varType)) {
                        System.out.println("Type mismatch for variable: " + varName + ". Found: " + varProps.varType + ", Expected: " + varType);
                        return false; // Type mismatch
                    }
                } else {
                    System.out.println("Variable " + varName + " not found in symbol table.");
                    return false; // Variable not found
                }
            }
        }
    
        // All variables passed the type check
        return true; 
    }
    
    

    // Type check algorithm
    private boolean typeCheckAlgo(Node algo) {
        return typeCheckInstru(algo);
    }

    // Type check instructions
    private boolean typeCheckInstru(Node instru) {
        // Base case: No instructions
        if (instru == null) {
            return true;
        }
        return typeCheckCommand(instru) && typeCheckInstru(instru);
    }

    // Type check a command
    private boolean typeCheckCommand(Node command) {
        // Check if the command is of type "COMMAND"
        if (!command.NodeName.equals("COMMAND")) {
            return false; // Not a valid command node
        }
    
        // Check if it's a skip or halt command
        if (command.childNodes.size() == 1) {
            String token = command.childNodes.get(0).NodeName;
            return token.equals("skip") || token.equals("halt"); // These commands are always valid
        }
    
        // Handle print command
        if (command.childNodes.size() > 1 && command.childNodes.get(0).NodeName.equals("print")) {
            String atomicType = typeCheckAtomic(command.childNodes.get(1)); // Check the atomic value after 'print'
            return atomicType.equals("n") || atomicType.equals("t"); // Valid types for print
        }
    
        // Handle return command
        if (command.childNodes.size() > 1 && command.childNodes.get(0).NodeName.equals("return")) {
            String atomicType = typeCheckAtomic(command.childNodes.get(1)); // Check the atomic value after 'return'
          //  String functionType = symbolTable.getFunctionType(); // Retrieve the expected function return type
           return true;
         // return atomicType.equals(functionType); // Validate the atomic type against function return type
        }
    
        // Handle assignment command
        if (command.childNodes.size() > 1 && command.childNodes.get(0).NodeName.equals("ASSIGN")) {
            return typeCheckAssign(command.childNodes.get(0)); // Validate assignment
        }
    
        // Handle function call command
        if (command.childNodes.size() > 0 && command.childNodes.get(0).NodeName.matches("F_[a-z]([a-z0-9])*")) {
            String callType = typeCheckCall(command.childNodes.get(0)); // Check the function call type
            return callType.equals("v"); // Ensure the function returns void
        }
    
        // Handle if command
        if (command.childNodes.size() > 1 && command.childNodes.get(0).NodeName.equals("if")) {
            return typeCheckBranch(command); // Validate the if structure
        }
    
        return false; // Default case if nothing matches
    }
    

    // Type check atomic values
    private String typeCheckAtomic(Node atomicNode) {
       // String token = tokenList.get(0).name;
    
        // Define regex patterns
       /*  String variablePattern = "V_[a-z]([a-z0-9]*)"; // Matches variable names
        String numberPattern = "-?[0-9]+(\\.[0-9]+)?"; // Matches integers and decimals
        String stringPattern = "\"[A-Z][a-z]{0,7}\""; // Matches strings enclosed in double quotes
    
        // Check if the token matches variable name
        if (token.matches(variablePattern)) {
            // Retrieve the variable's type from the symbol table
            VariableProps variableProps = getVariableProps(token); // Implement this method to fetch from symbol table
            if (variableProps != null) {
                return variableProps.varType; // Return the variable's type
            } else {
                System.out.println("Error: Variable " + token + " is not defined.");
                return "undefined"; // Variable is not defined
            }
        } 
        // Check if the token matches a constant (number)
        else if (token.matches(numberPattern)) {
            // Determine if it's an integer or a float
            if (token.contains(".")) {
                return "float"; // It's a float
            } else {
                return "int"; // It's an integer
            }
        } 
        // Check if the token matches a string constant
        else if (token.matches(stringPattern)) {
            return "string"; // It's a string
        } 
    
        System.out.println("Error: Invalid atomic value " + token);
        return "invalid"; // Invalid atomic value
        /* */
        return "int";
    }
    
    // Placeholder for method to get variable properties from the symbol table
    private VariableProps getVariableProps(String varName) {
        // Implement logic to retrieve VariableProps from the symbol table
        // Example:
       /*  for (VariableProps vp : table) {
            if (vp.oldName.equals(varName)) {
                return vp; // Return the variable properties if found
            }
        }/* */
        return null; // Not found
    }
    

    // Type check assignment
    private boolean typeCheckAssign(Node assign) {
       // String varType = symbolTable.getType(assign.getVarName());
       // String termType = typeCheckTerm(assign.getTerm());
         
       return false;
       // return varType.equals(termType);
    }

    // Type check function calls
    private String typeCheckCall(Node call) {
        return "true";
        //String functionName = call.getFunctionName();
        //return symbolTable.getFunctionReturnType(functionName);
    }

    // Type check branch statements
    private boolean typeCheckBranch(Node branch) {
       // String condType = typeCheckCond(branch.getCondition());
       /*  if (!condType.equals("b")) {
            return false; // Condition must be boolean
        }
        /* */
        return typeCheckInstru(branch) && typeCheckInstru(branch);
    }

    // Type check conditions
    private String typeCheckCond(Node cond) {
       /*  if (cond.isSimple()) {
            return typeCheckSimple(cond.getSimple());
        } else if (cond.isComposit()) {
            return typeCheckComposit(cond.getComposit());
        }/* */

        return "u"; // Undefined
    }

    // Type check simple conditions
    private String typeCheckSimple(Node simple) {
       // String binOpType = typeCheckBinOp(simple.getBinOp());
       // String atomic1Type = typeCheckAtomic(simple.getAtomic1());
       // String atomic2Type = typeCheckAtomic(simple.getAtomic2());

       // if (binOpType.equals("c") && atomic1Type.equals("n") && atomic2Type.equals("n")) {
            //return "b"; // boolean
       // }
        return "u"; // Undefined
    }

    // Type check composite conditions
    private String typeCheckComposit(Node composit) {
       // String binOpType = typeCheckBinOp(composit.getBinOp());
       // String simple1Type = typeCheckSimple(composit.getSimple1());
       // String simple2Type = typeCheckSimple(composit.getSimple2());
            return "b";
        //return (binOpType.equals("b") && simple1Type.equals("b") && simple2Type.equals("b")) ? "b" : "u";
    }

    // Type check binary operations
    private String typeCheckBinOp(Node binOp) {
        /*if (binOp.isLogical()) {
            return "b"; // boolean type
        } else if (binOp.isComparison()) {
            return "c"; // comparison type
        } else if (binOp.isArithmetic()) {
            return "n"; // numeric type
        }/* */
        return "u"; // Undefined
    }

    // Type check functions
    private boolean typeCheckFunctions(Node functions) {
        // Base case: No functions
        if (functions == null) {
            return true;
        
            
       // return typeCheckFunction(functions) && typeCheckFunctions(functions.getNext());
    }
      return false;

}
    // Type check a single function
    private boolean typeCheckFunction(Node function) {
       // boolean headerValid = typeCheckHeader(function.getHeader());
       // boolean bodyValid = typeCheckBody(function.getBody());

        //return headerValid && bodyValid;
        return true;
    }

    // Type check function headers
    private boolean typeCheckHeader(Node header) {
    /* */
       // String functionName = header.getFunctionName();
       // String returnType = header.getReturnType();
       // symbolTable.link(returnType, functionName);
       // return symbolTable.getType(functionName).equals(returnType);
       return true;
    /* */
    }

    // Type check function bodies
    private boolean typeCheckBody(Node body) {
        return typeCheckProlog(body)
            && typeCheckLocVars(body)
            && typeCheckAlgo(body)
            && typeCheckEpilog(body)
            && typeCheckFunctions(body);
    }

    // Type check prolog and epilog
    private boolean typeCheckProlog(Node prolog) {
        return true; // Prolog is empty block
    }

    private boolean typeCheckEpilog(Node epilog) {
        return true; // Epilog is empty block
    }

    // Type check local variables
    private boolean typeCheckLocVars(Node locVars) {
        if (locVars == null) {
            return true; // No local variables
        }

      //  String varType = locVars.getVarType();
      //  String varName = locVars.getVarName();

        // Link variable name with its type in the symbol table
       // symbolTable.link(varType, varName);

       // return typeCheckLocVars(locVars.getNext());
        return true;
    }


    private String getChildValue(Node node) {
        // Assuming the child node contains text or other sub-nodes that represent the value
        if (node.childNodes.size() > 0) {
            return node.childNodes.get(0).NodeName; // Modify based on your node structure
        }
        return "";
    }
}
