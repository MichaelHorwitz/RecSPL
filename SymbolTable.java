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
            funcCalls.add(currNode.childNodes.getFirst().childNodes.getFirst().NodeName);
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
                table.put(currNode.id, newVar);
                stackOfTables.peek().put(currNode.id, newVar);
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
    if (root.NodeName.equals("PROG")) {
        System.out.println("Type checking PROG");

        // Loop through the children of PROG
        for (Node child : root.childNodes) {
            if (child.NodeName.equals("ALGO")) {
                System.out.println("sending algo to command");
                // Type check each GLOBVARS node
                if (!typeCheckAlgo(child)) {
                    return false; // Return false if any GLOBVARS fails
                }
            }
        }
        return true; // All GLOBVARS passed type checking
    } else if (root.NodeName.equals("PROG")) {
        System.out.println("Type checking ALgo");

        // Loop through the children of PROG
        for (Node child : root.childNodes) {
            if (child.NodeName.equals("ALGO")) {
                // Type check each GLOBVARS node
                if (!typeCheckAlgo(child)) {
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
    else if(commandType.equals("BRANCH")){}
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
      //  return typeCheckCall(firstChild);
    } else if (firstChild.NodeName.equals("OP")) {
        if (firstChild.childNodes.size() == 2) {
            //return typeCheckUnaryOp(firstChild); // Handle unary operators
        } else if (firstChild.childNodes.size() == 3) {
           // return typeCheckBinaryOp(firstChild); // Handle binary operators
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
        // Check the symbol table for the function type
        VariableProps functionProps = getVariableProps(functionName);
        if (functionProps != null) {
            return functionProps.varType.equals("num");  // Check if the function's return type is numeric
        } else {
            System.out.print("function name"+ functionName);
            System.out.println("Error: Function not found in symbol table: " + functionName);
            return false;  // Function not found
        }
    } else {
        System.out.println("Error: Parameters must all be numeric. Received: " + type1 + ", " + type2 + ", " + type3);
        return false;  // Parameters do not match expected types
    }
}



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