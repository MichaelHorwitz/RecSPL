import java.util.HashMap;
import java.util.Map;
import java.util.Stack;



import java.util.ArrayList;

public class SymbolTable {
    HashMap<Integer, VariableProps> table;
    ArrayList<HashMap<Integer, VariableProps>> listOfTables;
    Stack<HashMap<Integer, VariableProps>> stackOfTables;
    ArrayList<String> funcCalls;
    public SymbolTable(){
        table = new HashMap<>();
        stackOfTables = new Stack<>();
    }
    public void generateFromTree(Node root){
        if (root == null) {
            System.out.println("Invalid root");
            return;
        }
        funcCalls = new ArrayList<>();
        recGen(root);
        if (!funcCalls.isEmpty()) {
            System.out.println("The following functions were called but not declared");
            System.out.println(funcCalls);
        }
        System.out.println("-------------------------------------------------------");
        System.out.println("Finished scope analysis");
       typeCheck(root);
    }

   // public HashMap<String, VariableProps> getTable() {
       // return table;
    //}
    // Used to recursively generate the table from the tree
    public void recGen(Node currNode){
        if (currNode == null) {
            return;
        }
        else if (currNode.NodeName.equals("CALL")){
           // var fName = currNode.childNodes.getFirst().childNodes.getFirst().NodeName;
           // if (!checkAlreadyInTable(fName, table)){
               // funcCalls.add(fName);
           // }
        }
        else if(currNode.NodeName.equals("LOCVARS")){
            HashMap<Integer, VariableProps> currTable = stackOfTables.peek();
            String lastType = "";
            for (Node node : currNode.childNodes) {
                if (node.NodeName.equals("VTYP")) {
                    lastType = node.childNodes.get(0).NodeName;
                }
                else if (node.NodeName.equals("VNAME")) {
                    VariableProps newVar = new VariableProps();
                    String varName = node.childNodes.get(0).NodeName;
                    if (checkAlreadyInTable(varName, table)) {
                        System.out.println("Variable already declared: " + varName);
                        return;
                    }
                    newVar.oldName = varName;
                    newVar.translatedName = "v" + node.id;
                    newVar.varType = lastType;
                    table.put(node.id, newVar);
                    currTable.put(node.id, newVar);
                }
            }
        }
        else if (currNode.NodeName.equals("PROG")){
            stackOfTables.push(new HashMap<>());
            for (Node node : currNode.childNodes) {
                recGen(node);
            }
        }
        else if (currNode.NodeName.equals("HEADER")){
            stackOfTables.push(new HashMap<>());
            String lastFType = "";
            for (Node node : currNode.childNodes) {
                if (node.NodeName.equals("FTYP")) {
                    lastFType = node.childNodes.get(0).NodeName;
                }
                if (node.NodeName.equals("FNAME")) {
                    VariableProps fVar = new VariableProps();
                    String fName = node.childNodes.get(0).NodeName;
                    if (checkAlreadyInTable(fName, table)) {
                        System.out.println("Function already declared: " + fName);
                        return;
                    }
                    fVar.oldName = fName;
                    fVar.translatedName = "f" + node.id;
                    fVar.varType = lastFType;
                    table.put(node.id, fVar);
                    stackOfTables.peek().put(node.id, fVar);
                    for (int i = 0; i < funcCalls.size(); i++) {
                        if (fName.equals(funcCalls.get(i))) {
                            funcCalls.remove(i);
                            i--;
                        }
                    }
                }
                if (node.NodeName.equals("VNAME")) {
                    VariableProps newVar = new VariableProps();
                    String varName = node.childNodes.get(0).NodeName;
                    if (checkAlreadyInTable(varName, table)) {
                        System.out.println("Variable already declared: " + varName);
                        return;
                    }
                    newVar.oldName = varName;
                    newVar.translatedName = "v" + node.id;
                    newVar.varType = "num";
                    table.put(node.id, newVar);
                    stackOfTables.peek().put(node.id, newVar);
                }
            }
        }
        else if (currNode.NodeName.equals("GLOBVARS")){
            var currTable = stackOfTables.peek();
            String lastType = "";
            for (Node node : currNode.childNodes) {
                if (node.NodeName.equals("VTYP")){
                    lastType = node.childNodes.get(0).NodeName;
                }
                else if (node.NodeName.equals("VNAME")) {
                    VariableProps newVar = new VariableProps();
                    String varName = node.childNodes.get(0).NodeName;
                    if (checkAlreadyInTable(varName, table)) {
                        System.out.println("Variable already declared: " + varName);
                        return;
                    }
                    newVar.oldName = varName;
                    newVar.translatedName = "v" + node.id;
                    newVar.varType = lastType;
                    table.put(node.id, newVar);
                    currTable.put(node.id, newVar);
                }
                else if(node.NodeName.equals( "GLOBVARS")){
                    recGen(node);
                }
            }
        } else if (currNode.NodeName.equals("ASSIGN")) {
            String varName = currNode.childNodes.get(0).childNodes.get(0).NodeName;
            VariableProps matchedVarProps = null;

            for (HashMap<Integer, VariableProps> map : stackOfTables) {
                for (VariableProps varProps : map.values()) {
                    if (varProps.oldName.equals(varName)) {
                        matchedVarProps = varProps;
                        break;
                    }
                }
                if (matchedVarProps != null) {
                    break;
                }
            }

            if (matchedVarProps != null) {
                VariableProps newVar = new VariableProps();
                newVar.oldName = matchedVarProps.oldName;
                newVar.translatedName = matchedVarProps.translatedName;
                newVar.varType = matchedVarProps.varType;
               // table.put(currNode.childNodes.getFirst().id, newVar);
                //stackOfTables.peek().put(currNode.childNodes.getFirst().id, newVar);
            } else {
                System.out.println("Could not find variable " + varName);
            }

        } else if (currNode.NodeName.equals("VNAME")){
            String varName = currNode.childNodes.get(0).NodeName;
            VariableProps matchedVarProps = null;

            for (HashMap<Integer, VariableProps> map : stackOfTables) {
                for (VariableProps varProps : map.values()) {
                    if (varProps.oldName.equals(varName)) {
                        matchedVarProps = varProps;
                        break;
                    }
                }
                if (matchedVarProps != null) {
                    break;
                }
            }

            if (matchedVarProps != null) {
                VariableProps newVar = new VariableProps();
                newVar.oldName = matchedVarProps.oldName;
                newVar.translatedName = matchedVarProps.translatedName;
                newVar.varType = matchedVarProps.varType;
                table.put(currNode.id, newVar);
                stackOfTables.peek().put(currNode.id, newVar);
            } else {
                System.out.println("Could not find variable " + varName);
            }
        }
        else {
            for (Node node : currNode.childNodes) {
                recGen(node);
            }
        }
    }













//***************************************type checking************************


public boolean typeCheck(Node root) {
    if (root == null) {
        return false; // Handle null case
    }

    // Ensure we're processing the PROG node
    if (!root.NodeName.equals("PROG")) {
        System.out.println("Error: Expected PROG node, found: " + root.NodeName);
        return false;
    }

    System.out.println("Type checking PROG");

    boolean globVarsPassed = false;
    boolean algoPassed = false;
    boolean functionsPassed = false;

    // Loop through the children of PROG
    for (Node child : root.childNodes) {
        if (child.NodeName.equals("GLOBVARS")) {
            System.out.println("Type checking GLOBVARS");
            if (!typeCheckGLOVARS1(child)) {
                return false; // Return false if GLOBVARS fails
            }
            globVarsPassed = true;
        } else if (child.NodeName.equals("ALGO")) {
            System.out.println("Type checking ALGO");
            if (!typeCheckAlgo(child)) {
                return false; // Return false if ALGO fails
            }
            algoPassed = true;
        } else if (child.NodeName.equals("FUNCTIONS")) {
            System.out.println("Type checking FUNCTIONS");
            if (!typeCheckFunctions(child)) {
                return false; // Return false if FUNCTIONS fails
            }
            functionsPassed = true;
        }
    }

    // Ensure all three sections passed type checking
    if (globVarsPassed && algoPassed && functionsPassed) {
        return true;
    }

    // If any section was missing, return false
    System.out.println("Error: Missing GLOBVARS, ALGO, or FUNCTIONS in PROG.");
    return false;
}

private boolean typeCheckGLOVARS1(Node globVarsNode) {
    String varName = null;
    String varType = null;
    boolean status=false;
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
                status=true;
                System.out.println("wha i found at end"+ status);
          
            } else {
                System.out.println("Variable " + varName + " not found in symbol table.");
                return false; // Variable not found
            }
        }
    }

    // All variables passed the type check
    return true; 
}


private boolean typeCheckAlgo(Node algo) {

      // Loop through the children of PROG
      for (Node child : algo.childNodes) {
        if (child.NodeName.equals("INSTRUC")) {
            // Type check each GLOBVARS node
            if (!typeCheckInstruc1(child)) {
                return false; // Return false if any GLOBVARS fails
            }
        }
    }
       
    return true;
   
    // return typeCheckInstruc1(algo);
}

// Type check instructions
private boolean typeCheckInstruc1(Node instruc) {
    // Base case: No instructions
    System.out.println("I'm inside instruc");
    if (instruc == null) {
        return true; // No instructions to check
    }
    
    // Iterate over child nodes of instruc
    for (Node child : instruc.childNodes) {
        // If the child node is a COMMAND, check it
        if (child.NodeName.equals("COMMAND")) {
            if (!typeCheckCommand(child)) {
                return false; // Return false if any command check fails
            }
        }
        // If the child node is another INSTRUC, recursively check it
        else if (child.NodeName.equals("INSTRUC")) {
            if (!typeCheckInstruc1(child)) {
                return false; // Return false if any nested INSTRUC check fails
            }
        }
    }
    
    return true; // All checks passed
}



private boolean typeCheckCommand(Node command) {
    System.out.println("lets check command");
      
    // Ensure the command node is valid
    if (command == null || command.childNodes.size() == 0) {
        return false;
    }

    // Get the first child node to determine the type of command
    String commandType = command.childNodes.get(0).NodeName;
    System.out.println("Processing command type: " + commandType);

    // Handle "skip" and "halt" commands (base cases)
    if (commandType.equals("skip") || commandType.equals("halt")) {
        return true;  // These commands are always valid
    }
    
    // Handle "print" command
    else if (commandType.equals("print")) {
        // Ensure there are enough child nodes
        if (command.childNodes.size() > 1) {
            Node vnameOrConstNode = command.childNodes.get(1); // Access the VNAME or CONST directly
            System.out.println("Who I mean: " + vnameOrConstNode.NodeName); // Print the type of the node
            System.out.println("Who my child is: " + vnameOrConstNode); // Print the entire node for debugging
            typeCheckAtomic(vnameOrConstNode);
            // Check if the node is a VNAME or CONST
            //return typeCheckAtomic(vnameOrConstNode); // Validate if it's a valid atomic value
        } else {
            System.out.println("Error: Print command does not have enough arguments.");
            return false; // Invalid if no argument follows 'print'
        }
    }

    
    else if (commandType.equals("ASSIGN")) {
        // Implement your logic for assignment commands
        System.out.println("entering command");
         return typeCheckAssign(command.childNodes.get(0));
        
    }
    else if(commandType.equals("BRANCH")){
        System.out.println("entering the branch");
      return typeCheckBranch(command.childNodes.get(0));

    }
    else if(commandType.equals("return")){

        if (command.childNodes.size() > 1) {
            Node vnameOrConstNode = command.childNodes.get(1); // Access the VNAME or CONST directly
            System.out.println("Who I mean: " + vnameOrConstNode.NodeName); // Print the type of the node
            System.out.println("Who my child is: " + vnameOrConstNode); // Print the entire node for debugging
            typeCheckAtomic(vnameOrConstNode);
    }}
    else if(commandType.equals("CALL")){
    System.out.println("entering call to call");
      return typeCheckCall(command.childNodes.get(0));
   }
  

return true;
}
private boolean typeCheckAssign(Node assignNode) {
    // Ensure the node is indeed an ASSIGN type
    if (!assignNode.NodeName.equals("ASSIGN")) {
        System.out.println("Error: Expected ASSIGN node but found: " + assignNode.NodeName);
        return false;
    }

    // Ensure the ASSIGN node has the expected structure
    if (assignNode.childNodes.size() < 3) { // Should have at least [VNAME, '=', TERM]
        System.out.println("Error: Assign command does not have enough components.");
        return false;
    }

    // Create nodes for VNAME and TERM
    Node vnameNode = assignNode.childNodes.get(0); // First child should be VNAME
    Node operatorNode = assignNode.childNodes.get(1); // Second child should be '='
    Node termNode = assignNode.childNodes.get(2); // Third child should be TERM

    // Validate the variable name
    if (!vnameNode.NodeName.equals("VNAME")) {
        System.out.println("Error: Expected VNAME in assignment but found: " + vnameNode.NodeName);
        return false;
    }

    // Check if the variable exists in the symbol table
    VariableProps varProps = getVariableProps(vnameNode.childNodes.get(0).NodeName);
    if (varProps == null) {
        System.out.println("Error: Variable not found in symbol table: " + vnameNode.childNodes.get(0).NodeName);
        return false;
    }

    // Get the variable type from the symbol table
    String varType = varProps.varType; // This should be 'n' or 'text', for example

    // Type check the TERM and ensure it matches the variable type
    String termType = typeCheckTerm(termNode); // Implement this function to get the type of the term
    if (termType == null) {
        return false; // If term type is not valid
    }

    System.out.println("Assigning " + varType + " to " + termType);

    // Check if the variable type is numeric and matches the input
    if (varType.equals("n") && !isNumeric(termNode)) {
        System.out.println("Error: Cannot assign non-numeric value to numeric variable.");
        return false; // If VNAME is numeric, TERM must be numeric
    }

    // Check if the variable type matches the term type
    if (!varType.equals(termType)) {
        System.out.println("Error: Type mismatch. Cannot assign " + termType + " to " + varType);
        return false; // The types must match
    }
            System.out.println("assigned properly");
    return true; // The assignment is valid
}

// Helper method to check if TERM is numeric
private boolean isNumeric(Node termNode) {
    // Assuming TERM is represented as either a constant or a numeric expression
    if (termNode.NodeName.equals("CONST")) {
        return true; // Assuming CONST is always numeric
    }
    // Add more checks for other types of TERM if needed
    return false; // By default, assume it's not numeric
}

// Dummy implementation of typeCheckTerm to return the type of TERM






private boolean typeCheckAtomic(Node atomic) {
    if (atomic.NodeName.equals("VNAME")) {
        VariableProps varProps = getVariableProps(atomic.childNodes.get(0).NodeName);  // Lookup the variable in the symbol table
        if (varProps == null) {
            System.out.println("Error: Variable not found in symbol table: " + atomic.childNodes.get(0).NodeName);
            return false;
        }
        // Variable found, so its type is valid
        return true;
    } else if (atomic.NodeName.equals("CONST")) {
        // You can add additional logic to validate the constant type if needed
        return true;  // Assuming constants are always valid
    }
    // If it's neither a VNAME nor CONST, it's invalid
    System.out.println("Error: Invalid atomic value: " + atomic.NodeName);
    return false;
}

private String typeCheckTerm(Node term) {
    System.out.println("whats entering term: " + term.NodeName);
    if (term.childNodes.size() > 0) {
        Node firstChild = term.childNodes.get(0);

        if (firstChild.NodeName.equals("CONST")) {
            return typeCheckConst(firstChild);  // Check the type of the constant
        }

        if (firstChild.NodeName.equals("VNAME")) {
            VariableProps varProps = getVariableProps(firstChild.childNodes.get(0).NodeName);
            if (varProps != null) {
                return varProps.varType;  // Return the type of the variable
            }
        }
    
       else if (firstChild.NodeName.equals("CALL")) {
        System.out.println("starting here");
       return typecheckCallTerm(firstChild);
       }
    
      else if (firstChild.NodeName.equals("UNOP")) {
        if (firstChild.childNodes.size() == 2) {
            return typeCheckOp(firstChild); // Handle unary operators
        } }
     else if(firstChild.NodeName.equals("BINOP")){
         if (firstChild.childNodes.size() == 3) {
           return  typeCheckBinop(firstChild); // Handle binary operators
        }
    

}
    }
    System.out.println("Error: Invalid term: " + term.NodeName);
    return null;  // Return null for invalid terms
}
 




private boolean typeCheckCall(Node callNode) {
    System.out.println("inside call function");
    
    // Check if the call node has at least 4 children: function name + 3 parameters
    if (callNode.childNodes.size() < 4) {
        System.out.println("Error: CALL must have a function name and three parameters.");
        return false;  // Return false for an invalid call
    }

    // Get the function name
    String functionName = callNode.childNodes.get(0).childNodes.get(0).NodeName; 
    System.out.println("function name: " + functionName);

    // Traverse into the ATOMIC nodes
    String type1 = typeCheckAtomics(callNode.childNodes.get(1).childNodes.get(0)); // First parameter's atomic value
    String type2 = typeCheckAtomics(callNode.childNodes.get(2).childNodes.get(0)); // Second parameter's atomic value
    String type3 = typeCheckAtomics(callNode.childNodes.get(3).childNodes.get(0)); // Third parameter's atomic value

    // Check if all three parameters are numeric
    if ("num".equals(type1) && "num".equals(type2) && "num".equals(type3)) {
         
        VariableProps functionProps = getVariableProps(functionName);
        System.out.println("need to get this functions return type"+ functionName);

        return true;
    }
        // Check the symbol table for the function type
       // VariableProps functionProps = getVariableProps(functionName);
        //if (functionProps != null) {
           // return true;
           // return functionProps.varType.equals("num");  // Check if the function's return type is numeric
        //} 
        
        //else  {
           // System.out.print("function name"+ functionName);
           // System.out.println("Error: Function not found in symbol table: " + functionName);
           // return false;  // Function not found
        //}
      
    else {
        System.out.println("Error: Parameters must all be numeric. Received: " + type1 + ", " + type2 + ", " + type3);
        return false;  // Parameters do not match expected types
    }
   
} private String typecheckCallTerm(Node callNode){


    String functionName = callNode.childNodes.get(0).childNodes.get(0).NodeName; 
    System.out.println("function name: " + functionName);
    VariableProps functionProps = getVariableProps(functionName);
    if (functionProps != null) {
      
       return functionProps.varType ; // Check if the function's return type is numeric
    } 
   return "unknown type";


}

private String typeCheckOp(Node opNode) {
    // Ensure the node has enough children (UNOP and ARG)
    if (opNode.childNodes.size() < 2) {
        System.out.println("Error: OP must have an UNOP and an ARG.");
        return "u";  // Undefined
    }

    // Get the UNOP (first child) and ARG (second child)
    String unopType = typeCheckUnop(opNode.childNodes.get(0));
    String argType = typeCheckArg(opNode.childNodes.get(1));

    // Check if the UNOP and ARG are both boolean or both numeric
    if (unopType.equals("bool") && argType.equals("bool")) {
        return "bool";  // Bool type
    } else if (unopType.equals("num") && argType.equals("num")) {
        return "num";  // Numeric type
    } else {
        System.out.println("Error: Mismatched types for UNOP and ARG. UNOP: " + unopType + ", ARG: " + argType);
        return "u";  // Undefined if types don't match
    }
}

// Type checking for UNOP (e.g., "not", "sqrt")
private String typeCheckUnop(Node unopNode) {
    String unopName = unopNode.NodeName;
    
    // Check if the UNOP is boolean or numeric
    if (unopName.equals("not")) {
        return "bool";  // Boolean type
    } else if (unopName.equals("sqrt")) {
        return "num";  // Numeric type
    } else {
        System.out.println("Error: Invalid UNOP: " + unopName);
        return "u";  // Undefined if unrecognized UNOP
    }
}

// Type checking for ARG (which can be either an ATOMIC or another OP)
private String typeCheckArg(Node argNode) {
    if (argNode.NodeName.equals("ATOMIC")||argNode.NodeName.equals("CONST")) {
        return typeCheckAtomics(argNode);  // Check type of atomic values
    } else if (argNode.NodeName.equals("OP")) {
        return typeCheckOp(argNode);  // Recursively check the type of nested operations
    } else {
        System.out.println("Error: Invalid ARG: " + argNode.NodeName);
        return "u";  // Undefined if not recognized
    }
}

private String typeCheckBinop(Node binopNode) {
    System.out.println("i enter binop");
    // Ensure the node has enough children: BINOP, ARG1, and ARG2
    if (binopNode.childNodes.size() < 3) {
        System.out.println("Error: BINOP must have two arguments.");
        return "u";  // Undefined
    }

    // Get the BINOP (first child), ARG1 (second child), and ARG2 (third child)
    String binopType = typeCheckBinopType(binopNode.childNodes.get(0));
    String arg1Type = typeCheckArg(binopNode.childNodes.get(1));
    String arg2Type = typeCheckArg(binopNode.childNodes.get(2));

    // Check if both arguments are boolean and BINOP is a boolean operation
    if (binopType.equals("bool") && arg1Type.equals("bool") && arg2Type.equals("bool")) {
        return "bool";  // Bool type
    } 
    // Check if both arguments are numeric and BINOP is a numeric operation
    else if (binopType.equals("num") && arg1Type.equals("num") && arg2Type.equals("num")) {
        return "num";  // Numeric type
    }
    // Check if BINOP is a comparison operation and both arguments are numeric
    else if (binopType.equals("c") && arg1Type.equals("num") && arg2Type.equals("num")) {
        return "bool";  // Comparison results in a boolean type
    } 
    else {
        System.out.println("Error: Mismatched types for BINOP and arguments. BINOP: " + binopType + ", ARG1: " + arg1Type + ", ARG2: " + arg2Type);
        return "u";  // Undefined if types don't match
    }
}

// Type checking for BINOP (e.g., "or", "and", "eq", "grt", "add", "sub", "mul", "div")
private String typeCheckBinopType(Node binopNode) {
    String binopName = binopNode.NodeName;

    // Check if BINOP is boolean
    if (binopName.equals("or") || binopName.equals("and")) {
        return "bool";  // Boolean type
    } 
    // Check if BINOP is a comparison operator
    else if (binopName.equals("eq") || binopName.equals("grt")) {
        return "c";  // Comparison type
    } 
    // Check if BINOP is a numeric operator
    else if (binopName.equals("add") || binopName.equals("sub") || binopName.equals("mul") || binopName.equals("div")) {
        return "num";  // Numeric type
    } 
    else {
        System.out.println("Error: Invalid BINOP: " + binopName);
        return "u";  // Undefined if unrecognized BINOP
    }
}

// Reuse the typeCheckArg method for both ARG1 and ARG2 (same as defined earlier)



private String typeCheckAtomics(Node atomic) {
    System.out.println("atomic node: " + atomic.NodeName);  // Directly log the atomic node name

    if (atomic.NodeName.startsWith("V_")) {  // Assuming variable names start with "V_"
        // Lookup the variable in the symbol table by its name (which is atomic.NodeName)
        VariableProps varProps = getVariableProps(atomic.NodeName);  // Use the atomic node name directly
        
        if (varProps == null) {
            System.out.println("Error: Variable not found in symbol table: " + atomic.NodeName);
            return "u";  // Undefined if variable not found
        }
        // Return the type of the variable (e.g., "num", "text")
        System.out.println("Returning variable type: " + varProps.varType);
        return varProps.varType;  
    } else if (atomic.NodeName.equals("CONST")) {
        // Determine if the constant is a number or text
        String constValue = atomic.childNodes.get(0).NodeName;  // Assuming the value is stored in the first child node
        
        // Check if it's a numeric constant
        if (constValue.matches("-?[0-9]+(\\.[0-9]+)?")) {
            System.out.print( "const value ha this "+constValue);
            return "num";  // Numeric constant
        }
        // Check if it's a text constant
        else if (constValue.matches("\"[A-Z][a-z]{0,7}\"")) {
            return "text";  // Text constant
        } else {
            System.out.println("Error: Invalid constant value: " + constValue);
            return "u";  // Undefined if constant is neither a valid number nor valid text
        }
    }
    
    // If it's neither a VNAME nor CONST, it's invalid
    System.out.println("Error: Invalid atomic value: " + atomic.NodeName);
    return "u";  // Undefined if the atomic node type is not recognized
}





private String typeCheckConst(Node constNode) {
    // Assuming constNode has a single child node containing the value
    String constValue = constNode.childNodes.get(0).NodeName; // Get the value of the constant
    System.out.println("Checking constant value: " + constValue);

    if (constValue.matches("-?[0-9]+(\\.[0-9]+)?")) {  // Check for numeric constant
        return "num";  // Return "num" for numeric constants
    } else if (constValue.matches("\"[A-Z][a-z]{0,7}\"")) {  // Check for text constant
        return "text";  // Return "text" for string constants
    }

    System.out.println("Error: Invalid constant value: " + constValue);
    return null;  // Return null for invalid constants
}


// Type check a command
private boolean typeCheckBranch(Node branchNode) {
    // Ensure the node has children for COND, ALGO1, and ALGO2
    if (branchNode.childNodes.size() < 3) {
        System.out.println("Error: Invalid BRANCH structure, expected COND, ALGO1, and ALGO2.");
        return false;  // Invalid BRANCH structure
    }

    Node condNode = branchNode.childNodes.get(0);  // COND
    Node algo1Node = branchNode.childNodes.get(1);  // ALGO1
    Node algo2Node = branchNode.childNodes.get(2);  // ALGO2

    // Type-check the condition (COND)
    String condType = typeCheckCond(condNode);
    
    // If the condition is a boolean ('b'), type-check both branches
    if (condType.equals("bool")) {
        return typeCheckAlgo(algo1Node) && typeCheckAlgo(algo2Node);
    } else {
        System.out.println("Error: COND in BRANCH must evaluate to boolean. Received: " + condType);
        return false;  // Invalid COND type
    }
}
private String typeCheckCond(Node condNode) {
    // Cond can be SIMPLE or COMPOSIT
    if (condNode.NodeName.equals("SIMPLE")) {
        return typeCheckSimple(condNode);
    } else if (condNode.NodeName.equals("COMPOSIT")) {
        return typeCheckComposit(condNode);
    } else {
        System.out.println("Error: Invalid COND type: " + condNode.NodeName);
        return "u";  // Undefined type
    }
}
private String typeCheckSimple(Node simpleNode) {
    // SIMPLE is of the form BINOP( ATOMIC1, ATOMIC2 )
    if (simpleNode.childNodes.size() < 3) {
        System.out.println("Error: SIMPLE must have a BINOP and two ATOMICs.");
        return "u";  // Undefined
    }

    String binopType = typeCheckBinopType(simpleNode.childNodes.get(0));  // BINOP
    String atomic1Type = typeCheckAtomics(simpleNode.childNodes.get(1));  // ATOMIC1
    String atomic2Type = typeCheckAtomics(simpleNode.childNodes.get(2));  // ATOMIC2

    // Handle boolean BINOP (e.g., and, or) with boolean ATOMICs
    if (binopType.equals("bool") && atomic1Type.equals("bool") && atomic2Type.equals("bool")) {
        return "bool";  // Boolean result
    }
    // Handle comparison BINOP (e.g., eq, grt) with numeric ATOMICs
    else if (binopType.equals("c") && atomic1Type.equals("num") && atomic2Type.equals("num")) {
        return "bool";  // Comparison results in a boolean
    } else {
        System.out.println("Error: Invalid types in SIMPLE. BINOP: " + binopType + ", ATOMIC1: " + atomic1Type + ", ATOMIC2: " + atomic2Type);
        return "u";  // Undefined
    }
}
private String typeCheckComposit(Node compositNode) {
    // COMPOSIT ::= BINOP( SIMPLE1, SIMPLE2 ) or UNOP( SIMPLE )
    if (compositNode.NodeName.equals("BINOP")) {
        // COMPOSIT is of the form BINOP(SIMPLE1, SIMPLE2)
        if (compositNode.childNodes.size() < 3) {
            System.out.println("Error: COMPOSIT BINOP must have two SIMPLEs.");
            return "u";  // Undefined
        }

        String binopType = typeCheckBinopType(compositNode.childNodes.get(0));  // BINOP
        String simple1Type = typeCheckSimple(compositNode.childNodes.get(1));  // SIMPLE1
        String simple2Type = typeCheckSimple(compositNode.childNodes.get(2));  // SIMPLE2

        // Boolean BINOP with boolean SIMPLEs
        if (binopType.equals("b") && simple1Type.equals("b") && simple2Type.equals("b")) {
            return "b";  // Boolean result
        } else {
            System.out.println("Error: Invalid types in COMPOSIT. BINOP: " + binopType + ", SIMPLE1: " + simple1Type + ", SIMPLE2: " + simple2Type);
            return "u";  // Undefined
        }
    } else if (compositNode.NodeName.equals("UNOP")) {
        // COMPOSIT is of the form UNOP(SIMPLE)
        if (compositNode.childNodes.size() < 2) {
            System.out.println("Error: COMPOSIT UNOP must have a SIMPLE.");
            return "u";  // Undefined
        }

        String unopType = typeCheckUnopType(compositNode.childNodes.get(0));  // UNOP
        String simpleType = typeCheckSimple(compositNode.childNodes.get(1));  // SIMPLE

        // Boolean UNOP with boolean SIMPLE
        if (unopType.equals("b") && simpleType.equals("b")) {
            return "b";  // Boolean result
        } else {
            System.out.println("Error: Invalid types in COMPOSIT UNOP. UNOP: " + unopType + ", SIMPLE: " + simpleType);
            return "u";  // Undefined
        }
    } else {
        System.out.println("Error: Invalid COMPOSIT type: " + compositNode.NodeName);
        return "u";  // Undefined
    }
}
private String typeCheckUnopType(Node unopNode) {
    String unopName = unopNode.NodeName;

    // Check if UNOP is "not"
    if (unopName.equals("not")) {
        return "bool";  // Boolean type
    }
    // Check if UNOP is "sqrt"
    else if (unopName.equals("sqrt")) {
        return "num";  // Numeric type
    } else {
        System.out.println("Error: Invalid UNOP: " + unopName);
        return "u";  // Undefined
    }
}

//*************************************************************************************************











public boolean typeCheckFunctions(Node root) {
    // Base case: no functions to check
    if (root == null) {
        return true;
    }

    // Assuming FUNCTIONS1 structure where root has child nodes representing DECL and FUNCTIONS2
    for (Node child : root.childNodes) {
        if (child.NodeName.equals("DECL")) {
            System.out.println("entering DECL");
            // Perform typecheck on DECL node
            if (!typeCheckDecl(child)) {
                return false;
            }
        }
        else if (child.NodeName.equals("FUNCTIONS")) {
            // Recursively check the remaining functions
            if (!typeCheckFunctions(child)) {
                return false;
            }
        }
    }

    // If all declarations and functions passed type-checking
    return true;
}


// This method checks a single function declaration.
private boolean typeCheckDecl(Node decl) {
    System.out.println("i enter decl");
    return typeCheckHeader(decl) && typeCheckBody(decl);
}

// This method checks the function header.
private boolean typeCheckHeader(Node decl) {
    // Assuming HEADER is the first child of DECL
    Node headerNode = decl.childNodes.get(0);
    
    if (headerNode.NodeName.equals("HEADER")) {
        // Extract FTYP, FNAME, and VNAMEs from HEADER
        Node ftypNode = headerNode.childNodes.get(0);  // FTYP node
        Node ftyp = ftypNode.childNodes.get(0);        // Go deeper into FTYP
        System.out.println("Function type: " + ftyp.NodeName);

        Node fnameNode = headerNode.childNodes.get(1);  // FNAME node
        Node fname = fnameNode.childNodes.get(0);       // Go deeper into FNAME
        System.out.println("Function name: " + fname.NodeName);

        Node vname1Node = headerNode.childNodes.get(2);  // VNAME1 node
        Node vname1 = vname1Node.childNodes.get(0);      // Go deeper into VNAME1
        System.out.println("vname1: " + vname1.NodeName);

        Node vname2Node = headerNode.childNodes.get(3);  // VNAME2 node
        Node vname2 = vname2Node.childNodes.get(0);      // Go deeper into VNAME2
        System.out.println("vname2: " + vname2.NodeName);

        Node vname3Node = headerNode.childNodes.get(4);  // VNAME3 node
        Node vname3 = vname3Node.childNodes.get(0);      // Go deeper into VNAME3
        System.out.println("vname3: " + vname3.NodeName);
        
        // Validate function type, function name, and ensure VNAMEs are numeric
        return validateFTypeAndFname(ftyp, fname) && 
               validateVName(vname1) && 
               validateVName(vname2) && 
               validateVName(vname3);
    }
    
    System.out.println("Error: Missing HEADER in DECL.");
    return false;
}

private boolean validateVName(Node vname) {
    // Retrieve the variable properties for the VNAME
    String varName = vname.NodeName;
    VariableProps varProps = getVariableProps(varName);
    
    if (varProps != null) {
        System.out.println("Validating variable: " + varProps.oldName + " with Type: " + varProps.varType);
        // Check if the variable is of numeric type 'n'
        if (varProps.varType.equals("num")) {
            return true;  // VNAME is valid and numeric
        } else {
            System.out.println("Error: VNAME " + varProps.oldName + " is not of numeric type.");
            return false;  // VNAME is not numeric
        }
    } else {
        System.out.println("Error: VNAME " + varName + " not found in the symbol table.");
        return false;  // VNAME not found
    }
}

private boolean validateFTypeAndFname(Node ftyp, Node fname) {
    // Get the function name and type
    String functionName = fname.NodeName;
    String functionType = ftyp.NodeName;

    // Retrieve the function properties from the symbol table (assuming this method exists)
    VariableProps varProps = getVariableProps(functionName);
    
    // Check if varProps is not null and compare types
    if (varProps != null) {
        System.out.println("Found function: " + varProps.oldName + " with Type: " + varProps.varType);
        
        // Validate function name and type
        if (varProps.oldName.equals(functionName) && varProps.varType.equals(functionType)) {
            return true;  // Valid function name and type
        } else {
            System.out.println("Error: Function name or type mismatch.");
            return false;  // Mismatch in function name or type
        }
    } else {
        System.out.println("Error: Function not found in the symbol table.");
        return false;  // Function not found in the symbol table
    }
}


// This method checks the function body.
private boolean typeCheckBody(Node body) {
     System.out.println("type checking body");
    return typeCheckProlog(body)
        && typeCheckLocVars(body)
        && typeCheckAlgo(body)
        && typeCheckEpilog(body)
        && typeCheckSubFunctions(body);
}

// Check prolog, always returns true as per base case.
private boolean typeCheckProlog(Node prolog) {
    System.out.println("inside prlog");
    return true; // Base case
}

// Check the local variables.
private boolean typeCheckLocVars(Node declNode) {
    // Ensure that we're processing the DECL node
    if (declNode == null || !declNode.NodeName.equals("DECL")) {
        System.out.println("Error: Invalid DECL node.");
        return false;
    }

    Node headerNode = declNode.childNodes.get(0); // Get the HEADER node

    if (headerNode == null || !headerNode.NodeName.equals("HEADER")) {
        System.out.println("Error: Missing HEADER in DECL.");
        return false;
    }
System.out.println("inside localvars");
    String varType = "";
    boolean status = true; // Assume success until proven otherwise

    // Loop through the child nodes of the HEADER node to find VNAMEs
    for (Node child : headerNode.childNodes) {
        if (child.NodeName.equals("FTYP")) {
            varType = getChildValue(child).trim(); // Clean the variable type
            while (varType.endsWith(":")) {
                varType = varType.substring(0, varType.length() - 1);
            }
            System.out.println("Variable TYPE: " + varType);
        } 
        else if (child.NodeName.equals("VNAME")) {
            // Retrieve and clean the variable name
            String varName = getChildValue(child).trim();
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
                System.out.println("Variable " + varName + " matches type: " + varType);
            } else {
                System.out.println("Variable " + varName + " not found in symbol table.");
                return false; // Variable not found
            }
        }
    }

    // Return true if all variables are successfully checked
    return status;
}


// Check the main algorithm.


// Check the epilog, always returns true as per base case.
private boolean typeCheckEpilog(Node epilog) {
    System.out.println("entering epilog");
    return true; // Base case
}

// Check sub-functions.
private boolean typeCheckSubFunctions(Node subFunctions) {
    System.out.println("leaving subfunctions");
    return typeCheckFunctions(subFunctions);
}

// Simulated method to get type of a given variable/type.












































































































































































private VariableProps getVariableProps(String varName) {
    // Iterate through the entries of the table map
    for (Map.Entry<Integer, VariableProps> entry : table.entrySet()) {
        VariableProps vp = entry.getValue(); // Get the VariableProps from the entry
        if (vp.oldName.equals(varName)) {
            return vp; // Return the variable properties if found
        }
    }
    return null; // Not found
}







private String getChildValue(Node node) {
    // Assuming the child node contains text or other sub-nodes that represent the value
    if (node.childNodes.size() > 0) {
        return node.childNodes.get(0).NodeName; // Modify based on your node structure
    }
    return "";
}

    public boolean checkAlreadyInTable(String newVarName, HashMap<Integer, VariableProps> table){
        for (VariableProps varProps : table.values()) {
            if (varProps.oldName.equals(newVarName)) {
                return true;
            }
        }
        return false;
    }
    public void printTable() {
        for (Integer key : table.keySet()) {
            VariableProps varProps = table.get(key);
            System.out.println("ID: " + key + ", Old Name: " + varProps.oldName + ", Translated Name: " + varProps.translatedName + ", Type: " + varProps.varType);
        }
    }

   public  HashMap<Integer, VariableProps> getmap(){

        return this.table;

    }
}