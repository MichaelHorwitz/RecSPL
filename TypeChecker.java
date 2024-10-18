import java.util.HashMap;

class TypeChecker {
    HashMap<String, VariableProps> symbolTable;
    String currentFunctionType; // To keep track of the current function's return type

    public TypeChecker(HashMap<String, VariableProps> symbolTable) {
        this.symbolTable = symbolTable;
    }

    // Type check for the entire program
    public boolean typeCheckPROG(Node progNode) {
        return typeCheckGLOBVARS(progNode.getChild("GLOBVARS")) &&
               typeCheckALGO(progNode.getChild("ALGO")) &&
               typeCheckFUNCTIONS(progNode.getChild("FUNCTIONS"));
    }

    // Base case: GLOBVARS can be empty
    public boolean typeCheckGLOBVARS(Node globVarsNode) {
        if (globVarsNode == null) return true; // Base case: GLOBVARS can be empty

        // Loop through each child of GLOBVARS
        for (Node child : globVarsNode.childNodes) {
            if (!typeCheckGLOBVARS1(child)) {
                return false; // If any variable check fails, return false
            }
        }
        return true; // All checks passed
    }

    public boolean typeCheckGLOBVARS1(Node globVars1Node) {
        // Assuming 'typeof' returns the type string based on the VTYP node
        String varType = typeof(globVars1Node.getChild("VTYP"));
        String varName = globVars1Node.getChild("VNAME").NodeName; // Accessing NodeName or use another method

        // Check for variable redeclaration
        if (symbolTable.containsKey(varName)) {
            System.out.println("Error: Variable " + varName + " already declared.");
            return false; // Redeclaration error
        }

        // Add to the symbol table
        symbolTable.put(varName, new VariableProps(varType));

        // Recurse to the next global variable if present
        return typeCheckGLOBVARS(globVars1Node.getChild("GLOBVARS2"));
    }

    // Check the type of a VTYP
    private String typeof(Node vtypNode) {
        if (vtypNode.childNodes.size() > 0) {
            Node valueNode = vtypNode.childNodes.get(0); // Get the first child node from the childNodes list

            // Now we assume the token (text, num, etc.) is stored in the NodeName
            String token = valueNode.NodeName.trim(); // Assuming the actual value is in NodeName

            // Return the corresponding type based on the token
            if (token.equals("num")) return "num";
            if (token.equals("text")) return "text";
        }

        return "unknown";
    }

    // Type check ALGO
    public boolean typeCheckALGO(Node algoNode) {
        return typeCheckINSTRUC(algoNode.getChild("INSTRUC"));
    }

    // INSTRUC type check (recursive)
    public boolean typeCheckINSTRUC(Node instrucNode) {
        if (instrucNode == null) return true; // base case
        return typeCheckCOMMAND(instrucNode.getChild("COMMAND")) &&
               typeCheckINSTRUC(instrucNode.getChild("INSTRUC2")); // Recurse through instructions
    }

    // COMMAND type check
    public boolean typeCheckCOMMAND(Node commandNode) {
        switch (commandNode.NodeName) {
            case "skip":
            case "halt":
                return true; // These are always valid

            case "print":
                return commandNode.childNodes.size() > 0 && typeCheckPrint(commandNode.childNodes.get(0));

            case "return":
                return commandNode.childNodes.size() > 0 && typeCheckRETURN(commandNode.childNodes.get(0));

            case "ASSIGN":
                return commandNode.childNodes.size() > 0 && typeCheckASSIGN(commandNode.childNodes.get(0));

            case "CALL":
                return commandNode.childNodes.size() > 0 && typeCheckCALL(commandNode.childNodes.get(0));

            case "BRANCH":
                return commandNode.childNodes.size() > 0 && typeCheckBRANCH(commandNode.childNodes.get(0));

            default:
                System.out.println("Error: Unknown command " + commandNode.NodeName);
                return false; // Unknown command
        }
    }

    // Check print command
    private boolean typeCheckPrint(Node atomicNode) {
        String atomicType = typeCheckATOMIC(atomicNode);
        return atomicType.equals("num") || atomicType.equals("text");
    }

    // Get the expected return type of the current function
    private String currentFunctionReturnType() {
        return this.currentFunctionType;
    }

    // Type check for return statement
    public boolean typeCheckRETURN(Node returnNode) {
        String expectedType = currentFunctionReturnType();
        String returnValueType = typeCheckATOMIC(returnNode.getChild("ATOMIC"));

        if (!expectedType.equals(returnValueType)) {
            System.out.println("Error: Return type " + returnValueType + " does not match expected type " + expectedType);
            return false; // Type mismatch error
        }
        return true;
    }

    // Type check for assignment
    public boolean typeCheckASSIGN(Node assignNode) {
        Node vnameNode = assignNode.childNodes.get(0); // Assuming first child is VNAME
        String vname = vnameNode.NodeName; // The VNAME is stored in NodeName

        // Check if variable is declared
        if (!symbolTable.containsKey(vname)) {
            System.out.println("Error: Variable " + vname + " is not declared.");
            return false; // Variable not declared
        }

        // Check if the first child is the assignment operator "<" or "="
        if (assignNode.childNodes.size() > 1 && assignNode.childNodes.get(1).NodeName.equals("<")) {
            // Handle the input case (VNAME < input)
            String vnameType = symbolTable.get(vname).getType();
            return vnameType.equals("num"); // Only numeric inputs allowed
        } else {
            // Handle the VNAME = TERM case
            Node termNode = assignNode.childNodes.get(2); // Assuming third child is TERM
            String termType = typeCheckTERM(termNode);
            
            // Compare the type of the variable with the type of the term
            if (!symbolTable.get(vname).getType().equals(termType)) {
                System.out.println("Error: Type mismatch in assignment of " + vname + ": expected " + symbolTable.get(vname).getType() + " but got " + termType);
                return false; // Type mismatch
            }
            return true;
        }
    }

    // Type check for function call
    public boolean typeCheckCALL(Node callNode) {
        String fname = callNode.getChild("FNAME").NodeName; // Get function name
        if (!symbolTable.containsKey(fname)) {
            System.out.println("Error: Function " + fname + " is not declared.");
            return false; // Function not declared
        }

        // Assume we retrieve the function type (return type)
        currentFunctionType = symbolTable.get(fname).getType(); // Assuming VariableProps includes function return type
        // Add additional parameter type checks if necessary
        return true; // Indicate successful check
    }

    // Type check for branch (if-then-else)
    public boolean typeCheckBRANCH(Node branchNode) {
        Node condNode = branchNode.getChild("COND");
        if (!typeCheckCOND(condNode)) return false;

        Node algo1 = branchNode.getChild("ALGO1");
        Node algo2 = branchNode.getChild("ALGO2");
        return typeCheckALGO(algo1) && typeCheckALGO(algo2);
    }
    public String typeCheckSIMPLE(Node simpleNode) {
        // Assuming SIMPLE can be a binary operation or a simple comparison
        if (simpleNode.NodeName.equals("BINOP")) {
            // Get the children of BINOP, which could be ATOMIC nodes
            String leftType = typeCheckATOMIC(simpleNode.getChild("LEFT_OPERAND"));  // Adjust based on your structure
            String rightType = typeCheckATOMIC(simpleNode.getChild("RIGHT_OPERAND")); // Adjust based on your structure
    
            // Example logic for binary operations
            if (leftType.equals("num") && rightType.equals("num")) {
                return "b"; // Result of comparison is boolean
            } else if (leftType.equals("text") && rightType.equals("text")) {
                return "b"; // Result of comparison is boolean
            } else {
                System.out.println("Error: Incompatible types in binary operation.");
                return "unknown"; // Handle incompatible types
            }
        } else if (simpleNode.NodeName.equals("UNOP")) {
            // If the SIMPLE node is a unary operation
            String operandType = typeCheckATOMIC(simpleNode.getChild("OPERAND")); // Adjust based on your structure
            if (operandType.equals("b")) {
                return "b"; // Unary operation on a boolean
            } else {
                System.out.println("Error: Invalid operand type for unary operation.");
                return "unknown"; // Handle invalid operand type
            }
        }
        
        // Add more checks as necessary for your grammar rules
        return "unknown"; // Default case if no matching structure is found
    }
    public boolean typeCheckCOND(Node condNode) {
        String condType = typeCheckSIMPLE(condNode.getChild("SIMPLE"));
        return condType.equals("b"); // Expecting boolean type
    }
    
    // Type check a condition
   
    // Type check for ATOMIC
    public String typeCheckATOMIC(Node atomicNode) {
        if (atomicNode.NodeName.equals("VNAME")) {
            // Access the variable name node and get its type from the symbol table
            String variableName = atomicNode.getChild("VNAME").NodeName; // Ensure you're getting the variable name correctly
            if (symbolTable.containsKey(variableName)) {
                return symbolTable.get(variableName).getType(); // Return the type of the variable
            } else {
                System.out.println("Error: Variable " + variableName + " not declared.");
                return "unknown"; // Variable not declared
            }
        } else if (atomicNode.NodeName.equals("CONST")) {
            return typeCheckCONST(atomicNode.getChild("CONST")); // Ensure you pass the correct child node
        }
        return "unknown"; // Should not happen
    }
    
    // Type check for constants
    public String typeCheckCONST(Node constNode) {
        Node tokenNode = constNode.getChild("TOKEN"); // Get the child node representing the token
        if (tokenNode != null) {
            String tokenClass = tokenNode.NodeName; // Access NodeName to determine the type
            if (tokenClass.equals("N")) return "num"; // Check for numeric constant
            if (tokenClass.equals("T")) return "text"; // Check for text constant
        }
        
        System.out.println("Error: Unknown token class for constant.");
        return "unknown"; // Handle unknown token class
    }
    
    
    

    // Type check for a term
    public String typeCheckTERM(Node termNode) {
        Node firstChild = termNode.getChild("ATOMIC"); // Check for ATOMIC first
        if (firstChild != null) {
            return typeCheckATOMIC(firstChild); // Pass to typeCheckATOMIC
        }
    
        firstChild = termNode.getChild("CALL"); // Check for CALL next
        if (firstChild != null) {
            return typeCheckCALL(firstChild) ? currentFunctionType : "unknown"; // Use currentFunctionType
        }
    
        firstChild = termNode.getChild("OP"); // Check for OP last
        if (firstChild != null) {
            return typeCheckOP(firstChild); // Pass to typeCheckOP
        }
    
        return "unknown"; // In case of an unknown term
    }
    
    
    
    

    // Placeholder for operation checks
    private String typeCheckOP(Node opNode) {
        // Assume we handle binary and unary operations here
        // Return expected type based on the operation
        return "num"; // Example, assuming operations return num type
    }

    public boolean typeCheckFUNCTIONS(Node functionsNode) {
        if (functionsNode == null) return true; // Base case: no functions to check
    
        // Loop through each function in the FUNCTIONS node
        for (Node functionNode : functionsNode.childNodes) {
            if (!typeCheckFUNCTION(functionNode)) {
                return false; // If any function check fails, return false
            }
        }
        return true; // All function checks passed
    }
    
    private boolean typeCheckFUNCTION(Node functionNode) {
        Node headerNode = functionNode.getChild("HEADER"); // Get the HEADER of the function
        Node bodyNode = functionNode.getChild("BODY");     // Get the BODY of the function
    
        // Check function header for name and return type
        String functionName = headerNode.getChild("FNAME").NodeName; // Assuming FNAME is the function name
        String returnType = typeof(headerNode.getChild("RETURNTYPE")); // Get return type
    
        // Check if the function name is already declared
        if (symbolTable.containsKey(functionName)) {
            System.out.println("Error: Function " + functionName + " already declared.");
            return false; // Redeclaration error
        }
    
        // Add the function to the symbol table with its return type
      //  symbolTable.put(functionName, new VariableProps(returnType, true)); // Mark as function in symbol table
    
        // Check the body of the function
        if (!typeCheckBODY(bodyNode)) {
            return false; // If body type check fails, return false
        }
    
        return true; // Function type check passed
    }
    
    private boolean typeCheckBODY(Node bodyNode) {
        // Check the body for local variables and instructions
        Node prologNode = bodyNode.getChild("PROLOG");
        Node locVarsNode = bodyNode.getChild("LOCVARS");
        Node algoNode = bodyNode.getChild("ALGO");
        Node epilogNode = bodyNode.getChild("EPILOG");
    
        // You can perform checks for local variables and the instructions here
        return typeCheckPROLOG(prologNode) &&
               typeCheckLOCVARS(locVarsNode) &&
               typeCheckALGO(algoNode) &&
               typeCheckEPILOG(epilogNode);
    }
    
    // Implement any other methods needed to check PROLOG, LOCVARS, and EPILOG
    private boolean typeCheckPROLOG(Node prologNode) {
        // Check prolog details (if any)
        return true; // Placeholder
    }
    
    private boolean typeCheckLOCVARS(Node locVarsNode) {
        // Check local variable declarations
        return typeCheckGLOBVARS(locVarsNode); // You might have a separate method for local vars
    }
    
    private boolean typeCheckEPILOG(Node epilogNode) {
        // Check epilog details (if any)
        return true; // Placeholder
    }
    
}
