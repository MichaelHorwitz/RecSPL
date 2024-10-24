import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class SymbolTable {
    private class Pair{
        public String name;
        public int scope;
        public Pair(){

        }
        public Pair(String name, int scope) {
            this.name = name;
            this.scope = scope;
        }
    }
public static final String RED = "\033[0;31m";
public static final String YELLOW = "\033[0;33m";

    HashMap<Integer, VariableProps> table;
    ArrayList<HashMap<Integer, VariableProps>> listOfTables;
    Stack<HashMap<Integer, VariableProps>> stackOfTables;
    ArrayList<Pair> funcCalls;
    ArrayList<Pair> funcDefs;
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
        funcDefs = new ArrayList<>();
        recGen(root);
        funcCheck(root);
        // if (!funcCalls.isEmpty()) {
        //     System.out.println("The following functions were called but not declared");
        //     System.out.println(funcCalls);
        // }
        System.out.println("-------------------------------------------------------");
        System.out.println( RED+"Finished scope analysis");
        System.out.println("-------------------------------------------------------");
      
       typeCheck(root);
       System.out.println(YELLOW +"Finished type checking");

       System.out.println("-------------------------------------------------------");
       System.out.println();
       System.out.println();
    }

   // public HashMap<String, VariableProps> getTable() {
       // return table;
    //}
    // Used to recursively generate the table from the tree
    public int currScope;
    public void funcCheck(Node currNode){
        funcCalls = new ArrayList<>();
        currScope = 0;
        recFunCheck(currNode, currScope);
        for (Pair call : funcCalls) {
            boolean match = false;
            for (Pair def : funcDefs) {
                if (call.name.equals(def.name)) {
                    if(call.scope == def.scope || call.scope == def.scope + 1){
                        // System.out.println(call.name);
                        // System.out.println(call.scope);
                        // System.out.println(def.scope);
                    
                        match = true;
                    }
                }
            }
            if (!match) {
                System.out.println("Invalid function call: " + call.name);
                
            }
        }
    }
    public void recFunCheck(Node currNode, int currScope){
        if (currNode.NodeName.equals("DECL")) {
            Node fNameNode = currNode.childNodes.getFirst().childNodes.get(1);
            funcDefs.add(new Pair(fNameNode.childNodes.getFirst().NodeName, currScope));
            currScope++;
            for (Node node : currNode.childNodes) {
                recFunCheck(node, currScope);
            }
            return;
        } else if (currNode.NodeName.equals("CALL")) {
            Node fNameNode = currNode.childNodes.getFirst();
            funcCalls.add(new Pair(fNameNode.childNodes.getFirst().NodeName, currScope));
            for (Node node : currNode.childNodes) {
                recFunCheck(node, currScope);
            }
            return;
        } else {
            for (Node node : currNode.childNodes) {
                recFunCheck(node, currScope);
            }
            return;
        }
    }
    public void recGen(Node currNode){
        if (currNode == null) {
            return;
        }
        else if (currNode.NodeName.equals("EPILOG")){
            stackOfTables.pop();
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
                    if (checkAlreadyInTable(varName, currTable)) {
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
                    if (checkAlreadyInTable(varName, stackOfTables.peek())) {
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

   // System.out.println("Type checking PROG");

    boolean globVarsPassed = false;
    boolean algoPassed = false;
    boolean functionsPassed = false;

    // Loop through the children of PROG
    for (Node child : root.childNodes) {
        if (child.NodeName.equals("GLOBVARS")) {
          //  System.out.println("Type checking GLOBVARS");
            if (!typeCheckGLOVARS1(child)) {
                return false; // Return false if GLOBVARS fails
            }
            globVarsPassed = true;
        } else if (child.NodeName.equals("ALGO")) {
           // System.out.println("Type checking ALGO");
            if (!typeCheckAlgo(child)) {
                return false; // Return false if ALGO fails
            }
            algoPassed = true;
        } else if (child.NodeName.equals("FUNCTIONS")) {
           // System.out.println("Type checking FUNCTIONS");
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
          //  System.out.println("Variable TYPE: " + varType);
        } 
        else if (child.NodeName.equals("VNAME")) {
            varName = getChildValue(child).trim();
            while (varName.endsWith(":")) {
                varName = varName.substring(0, varName.length() - 1);
            }
           // System.out.println("Variable NAME: " + varName);
            
            // Retrieve the variable properties from the symbol table
            VariableProps varProps = getVariableProps(varName);
            
            // Check if varProps is not null and compare types
            if (varProps != null) {
              //  System.out.println("Found Variable: " + varProps.oldName + " with Type: " + varProps.varType);
                if (!varProps.varType.equals(varType)) {
                    System.out.println("Type mismatch for variable: " + varName + ". Found: " + varProps.varType + ", Expected: " + varType);
                    return false; // Type mismatch
                }
                status=true;
              //  System.out.println("wha i found at end"+ status);
          
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
    // Ensure we are actually working with the ALGO node
    for (Node child : algo.childNodes) {
        if (child.NodeName.equals("INSTRUC")) {
            // Type check each instruction node inside ALGO
            if (!typeCheckInstruc1(child)) {
                return false; // Return false if any INSTRUC fails
            }
        }
    }
    return true; // If all checks pass
}


// Type check instructions
private boolean typeCheckInstruc1(Node instruc) {
    // Base case: No instructions
   // System.out.println("I'm inside instruc");
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
   // System.out.println("lets check command");
      
    // Ensure the command node is valid
    if (command == null || command.childNodes.size() == 0) {
        return false;
    }

    // Get the first child node to determine the type of command
    String commandType = command.childNodes.get(0).NodeName;
    //System.out.println("Processing command type: " + commandType);

    // Handle "skip" and "halt" commands (base cases)
    if (commandType.equals("skip") || commandType.equals("halt")) {
        return true;  // These commands are always valid
    }
    
    // Handle "print" command
    else if (commandType.equals("print")) {
        // Ensure there are enough child nodes
        if (command.childNodes.size() > 1) {
            Node vnameOrConstNode = command.childNodes.get(1); // Access the VNAME or CONST directly
           // System.out.println("Who I mean: " + vnameOrConstNode.NodeName); // Print the type of the node
           // System.out.println("Who my child is: " + vnameOrConstNode); // Print the entire node for debugging
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
      //  System.out.println("entering command");
         return typeCheckAssign(command.childNodes.get(0));
        
    }
    else if(commandType.equals("BRANCH")){
      //  System.out.println("entering the branch");
      return typeCheckBranch(command.childNodes.get(0));

    }
    else if(commandType.equals("return")){

        if (command.childNodes.size() > 1) {
            Node vnameOrConstNode = command.childNodes.get(1); // Access the VNAME or CONST directly
          ///  System.out.println("Who I mean: " + vnameOrConstNode.NodeName); // Print the type of the node
           // System.out.println("Who my child is: " + vnameOrConstNode); // Print the entire node for debugging
            typeCheckAtomic(vnameOrConstNode);
    }}
    else if(commandType.equals("CALL")){
  //  System.out.println("entering call to call");
      return typeCheckCall(command.childNodes.get(0));
   }
  

return true;
}
private boolean typeCheckAssign(Node assignNode) {
    if (!assignNode.NodeName.equals("ASSIGN")) {
        return false;
    }

    if (assignNode.childNodes.size() < 3) {
        System.out.println("Error: Assign command does not have enough components.");
        return false;
    }

    Node vnameNode = assignNode.childNodes.get(0); 
    Node termNode = assignNode.childNodes.get(2); 

    VariableProps varProps = getVariableProps(vnameNode.childNodes.get(0).NodeName);
    if (varProps == null) {
        System.out.println("Error: Variable not found in symbol table: " + vnameNode.childNodes.get(0).NodeName);
        return false;
    }

    String varType = varProps.varType; 
    String termType = typeCheckTerm(termNode);

    if (termType == null) {
        return false;
    }

    // If term is a function call, ensure argument types match
    if (termNode.NodeName.equals("CALL")) {
        String callType = typecheckCallTerm(termNode); // Check function call types
        if (!callType.equals(varType)) {
            System.out.println("Error: Type mismatch. Cannot assign " + callType + " to " + varType);
            return false;
        }
    }

    if (!varType.equals(termType)) {
        System.out.println("Error: Type mismatch. Cannot assign " + termType + " to " + varType);
        return false;
    }

    return true;
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
  //  System.out.println("whats entering term: " + term.NodeName);
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
       // System.out.println("starting here");
       return typecheckCallTerm(firstChild);
       }
    
      else if (firstChild.NodeName.equals("UNOP")) {
        if (firstChild.childNodes.size() == 2) {
            return typeCheckOp(firstChild); // Handle unary operators
        } }
     else if(firstChild.NodeName.equals("BINOP")){
         if (firstChild.childNodes.size() == 3) {
           return  typeCheckBinopType(firstChild); // Handle binary operators
        }
    

}
    }
    System.out.println("Error: Invalid term: " + term.NodeName);
    return null;  // Return null for invalid terms
}
 
private String typeCheckBinops(Node binope){
  
    // System.out.println("this who i enter with " + binope.NodeName);

   // System.out.println("aww banda");
    return "u";
}




private boolean typeCheckCall(Node callNode) {
   // System.out.println("i enter the call");

    // Check if the call node has at least 4 children: function name + 3 parameters
    if (callNode.childNodes.size() < 4) {
        System.out.println("Error: CALL must have a function name and three parameters.");
        return false;  // Return false for an invalid call
    }

    // Get the function name
    String functionName = callNode.childNodes.get(0).childNodes.get(0).NodeName;
   
    // Traverse into the ATOMIC nodes
    String type1 = getNodeType(callNode.childNodes.get(1).childNodes.get(0)); // First parameter's type
    String type2 = getNodeType(callNode.childNodes.get(2).childNodes.get(0)); // Second parameter's type
    String type3 = getNodeType(callNode.childNodes.get(3).childNodes.get(0)); // Third parameter's type

   // System.out.println("what I'm carrying: " + callNode.childNodes.get(3).childNodes.get(0));

    // Check if all three parameters are numeric
    if ("num".equals(type1) && "num".equals(type2) && "num".equals(type3)) {
        VariableProps functionProps = getVariableProps(functionName);
        return true;  // Return true if all parameters are valid
    } else {
        System.out.println("Error: Parameters must all be numeric. Received: " + type1 + ", " + type2 + ", " + type3);
        return false;  // Parameters do not match expected types
    }
}

// Helper function to determine the node type, checks if it's a numeric or valid atomic, otherwise calls typeCheckAtomics
private String getNodeType(Node node) {
    // If the node is already a known numeric constant or valid type, return "num"
    if (node.NodeName.matches("-?[0-9]+(\\.[0-9]+)?")) {  // Assuming numeric constants are represented as digits
        return "num";
    } else {
        // Otherwise, pass it through typeCheckAtomics to determine its type
        return typeCheckAtomics(node);
    }
}

private String typecheckCallTerm(Node callNode) {
    String functionName = callNode.childNodes.get(0).childNodes.get(0).NodeName; // Get function name
    String argument1 = callNode.childNodes.get(1).childNodes.get(0).NodeName;   // First argument
    String argument2 = callNode.childNodes.get(2).childNodes.get(0).NodeName;   // Second argument
    String argument3 = callNode.childNodes.get(3).childNodes.get(0).NodeName;   // Third argument

    // Get function properties (like return type) from the symbol table
    VariableProps functionProps = getVariableProps(functionName);
    if (functionProps == null) {
        System.out.println("Error: Function not found in symbol table: " + functionName);
        return "unknown type";
    }

    // Get expected argument types for the function (assuming numeric types for simplicity)
    // This could be based on how you define the function in your symbol table
    String expectedArgType = "n"; // Assume function expects numeric arguments

    // Check each argument's type
    if (!isNumeric(argument1)) {
        System.out.println("Error: Argument 1 "+argument1+" is not numeric in function call to " + functionName);
        return "mismatch";
    }
    if (!isNumeric(argument2)) {
        System.out.println("Error: Argument 2 is not numeric in function call to " + functionName);
        return "mismatch";
    }
    if (!isNumeric(argument3)) {
        System.out.println("Error: Argument 3 is not numeric in function call to " + functionName);
        return "mismatch";
    }

    // If all arguments are valid, return the function's return type
    return functionProps.varType; // This should be the return type of the function
}

// Helper method to check if an argument is numeric based on its name
private boolean isNumeric(String argumentName) {
    // Check if the argument is a number using regex
    if (argumentName.matches("-?[0-9]+(\\.[0-9]+)?")) {
        return true; // The argument is a number (integer or decimal)
    }

    // If not a constant, check if it's a numeric variable
    VariableProps varProps = getVariableProps(argumentName);
    if (varProps != null && varProps.varType.equals("num")) {
        return true; // The argument is a variable of numeric type
    }

    return false; // The argument is neither a number nor a numeric variable
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
    System.out.println("got this ar" +argNode.NodeName );
    if (argNode.NodeName.equals("ATOMIC")||argNode.NodeName.equals("CONST")||argNode.NodeName.equals("VNAME")) {
        return typeCheckAtomics(argNode);  // Check type of atomic values
    } else if (argNode.NodeName.equals("OP")) {
        return typeCheckOp(argNode);  // Recursively check the type of nested operations
    } 
    
    else {
        System.out.println("Error: Invalid ARG: " + argNode.NodeName);
        return "u";  // Undefined if not recognized
    }
}

private String typeCheckBinop(Node binopNode) {
   // System.out.println("i enter binop");
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
    // Make sure the binopNode has at least one child (the operator)
    if (binopNode.childNodes.size() < 1) {
        System.out.println("Error: BINOP node has no operator.");
        return "u";  // Undefined
    }
   
    // Get the operator from the first child
    String binopName = binopNode.childNodes.get(0).NodeName;
   // System.out.println("what im returning" + binopName);
   // System.out.println("BINOP operator: " + binopName);

    // Check if BINOP is boolean
    if (binopName.equals("or") || binopName.equals("and")) {
       
        return "bool";  // Boolean type
    }
    // Check if BINOP is a comparison operator
    else if (binopName.equals("eq") || binopName.equals("grt")) {
      //  System.out.println("Entering eq");
        return "c";  // Comparison type
    }
    // Check if BINOP is a numeric operator
    else if (binopName.equals("add") || binopName.equals("sub") || binopName.equals("mul") || binopName.equals("div")) {
        return "num";  // Numeric type
    }
    else {
        System.out.println("Error: Invalid BINOP operator: " + binopName);
        return "u";  // Undefined if unrecognized BINOP
    }
}




// Reuse the typeCheckArg method for both ARG1 and ARG2 (same as defined earlier)



private String typeCheckAtomics(Node atomic) {
    // Check if the node is either a variable or a constant.
    
    String nodeName = atomic.NodeName;  // Get the atomic node's name
    
    // Case 1: Handle variable names (either "VNAME" or directly the variable names like "V_x", "V_y")
    if (nodeName.equals("VNAME") || nodeName.startsWith("V_")) {  
        String varName;
        
        if (nodeName.equals("VNAME")) {
            varName = atomic.childNodes.get(0).NodeName;  // Extract the variable name from the child node
        } else {
            varName = nodeName;  // Directly use the node name if it's like "V_x", "V_y"
        }
        
        if (varName.startsWith("V_")) {  // Ensure it starts with "V_" as expected for variable names
            VariableProps varProps = getVariableProps(varName);  // Lookup the variable in the symbol table
            if (varProps == null) {
                System.out.println("Error: Variable not found in symbol table: " + varName);
                return "u";  // Return undefined if variable not found
            }
            return varProps.varType;  // Return the type of the variable (e.g., "num", "text")
        } else {
            System.out.println("Error: Invalid variable name format: " + varName);
            return "u";  // Invalid variable name format
        }
    } 
    
    // Case 2: Handle constants (CONST)
    else if (nodeName.equals("CONST")) {
        String constValue = atomic.childNodes.get(0).NodeName;  // Extract the constant value
        
        // Check if it's a numeric constant
        if (constValue.matches("-?[0-9]+(\\.[0-9]+)?")) {
            return "num";  // Numeric constant
        }
        // Check if it's a valid text constant
        else if (constValue.matches("\"[A-Za-z]{1,8}\"")) {
            return "text";  // Text constant
        } else {
            System.out.println("Error: Invalid constant value: " + constValue);
            return "u";  // Undefined for invalid constant
        }
    } 
    
    // Case 3: Unexpected node type
    else {
        System.out.println("Error: Unexpected node type: " + nodeName);
        return "u";  // Return undefined for unexpected node types
    }
}


   





private String typeCheckConst(Node constNode) {
    // Assuming constNode has a single child node containing the value
    String constValue = constNode.childNodes.get(0).NodeName; // Get the value of the constant
   // System.out.println("Checking constant value: " + constValue);

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
   // System.out.println("final condtype is"+ condType);
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
    // SIMPLE must have at least a BINOP.
    if (simpleNode.childNodes.size() < 1) {
        System.out.println("Error: SIMPLE must have at least a BINOP.");
        return "u";  // Undefined
    }

    Node binopNode = simpleNode.childNodes.get(0);
    String binopType = typeCheckBinopType(binopNode);  // Get the type of the BINOP

    if (binopNode.childNodes.size() < 2) {
        System.out.println("Error: BINOP must have two ATOMICs.");
        return "u";  // Undefined
    }

    String atomic1Type = typeCheckAtomics(binopNode.childNodes.get(1));  // ATOMIC1
    String atomic2Type = typeCheckAtomics(binopNode.childNodes.get(2));  // ATOMIC2

    // Logical operations
    if (binopType.equals("bool") && atomic1Type.equals("bool") && atomic2Type.equals("bool")) {
        return "bool";  // Valid logical operation
    }
    // Comparison operations
    else if (binopType.equals("c") && atomic1Type.equals("num") && atomic2Type.equals("num")) {
        return "bool";  // Valid comparison operation
    }
    // Numeric operations (if applicable)
    else if (binopType.equals("num") && atomic1Type.equals("num") && atomic2Type.equals("num")) {
        return "num";  // Valid numeric operation
    } else {
        System.out.println("Error: Invalid types in SIMPLE. BINOP: " + binopType + ", ATOMIC1: " + atomic1Type + ", ATOMIC2: " + atomic2Type);
        return "u";  // Undefined
    }
}

private String typeCheckComposit(Node compositNode) {
    if (compositNode.NodeName.equals("COMPOSIT")) {
        System.out.println("Inside COMPOSIT: " + compositNode.NodeName);
        
        // Ensure COMPOSIT has exactly two SIMPLE nodes
        if (compositNode.childNodes.size() != 2) {
            System.out.println("Error: COMPOSIT must have exactly two SIMPLE nodes.");
            return "u";  // Undefined
        }

        Node simple1Node = compositNode.childNodes.get(0);
        Node simple2Node = compositNode.childNodes.get(1);

        String simple1Type = typeCheckSimple(simple1Node);  // Check type of SIMPLE1
        String simple2Type = typeCheckSimple(simple2Node);  // Check type of SIMPLE2

        // Check the types of the SIMPLE nodes against the type of the first BINOP
        String binopType1 = typeCheckBinopType(simple1Node.childNodes.get(0));
        String binopType2 = typeCheckBinopType(simple2Node.childNodes.get(0));

        // Adjusted composite type check
        if (binopType1.equals("bool") && simple1Type.equals("bool") && simple2Type.equals("bool")) {
            return "bool";  // Valid logical operation
        } else if (binopType1.equals("c") && simple1Type.equals("bool") && simple2Type.equals("bool")) {
            return "bool";  // Comparison returns a boolean
        } else {
            System.out.println("Error: Invalid types in COMPOSIT. SIMPLE1: " + simple1Type + ", SIMPLE2: " + simple2Type);
            return "u";  // Undefined
        }
    } 
        // UNOP type checking remains as is

        else if (compositNode.NodeName.equals("UNOP")) {
            // Check for UNOP structure
            if (compositNode.childNodes.size() != 1) {
                System.out.println("Error: COMPOSIT UNOP must have exactly one SIMPLE.");
                return "u";  // Undefined
            }
    
            // Get the SIMPLE node
            Node simpleNode = compositNode.childNodes.get(0);
            String unopType = typeCheckUnopType(compositNode.childNodes.get(0));  // Get type from UNOP
            String simpleType = typeCheckSimple(simpleNode);  // Check type of SIMPLE
    
            // Validate UNOP type
            if (unopType.equals("bool") && simpleType.equals("bool")) {
                return "bool";  // Valid UNOP returns a boolean
            } else {
                System.out.println("Error: Invalid types in COMPOSIT UNOP. UNOP: " + unopType + ", SIMPLE: " + simpleType);
                return "u";  // Undefined
            }
    
        } 
        
    else {
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
           // System.out.println("entering DECL");
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
    Node headerNode = decl.childNodes.get(0);  // Assuming HEADER is the first child of DECL
    Node ftypNode = headerNode.childNodes.get(0);  // Get the FTYP node

    return typeCheckHeader(decl) && typeCheckBody(decl, ftypNode);
}
private boolean validateReturnType(Node algoNode, Node ftypNode) {
    boolean returnFound = false;  // Flag to indicate if we found a return statement

    for (Node child : algoNode.childNodes) {
        if (child.NodeName.equals("COMMAND")) {
           // System.out.println("Entered COMMAND node");

            for (Node commandChild : child.childNodes) {
                // Check if the COMMAND contains a return statement
                if (commandChild.NodeName.equals("return")) {
                   // System.out.println("Found return statement");
                    returnFound = true;  // Set the flag that return has been found
                } else if (returnFound) {
                    // If return was found, check the next node
                    if (commandChild.NodeName.equals("CONST")|| commandChild.NodeName.equals("-?[0-9]+(\\.[0-9]+)?")) {
                       // System.out.println("Found return value node: " + commandChild.NodeName);

                        // Check if the CONST is a number
                        if (!isNumericConst(commandChild)) {
                            System.out.println("Error: Return value is not numeric.");
                            return false;
                        }
                        
                        // The return type is valid
                        return true;  // Return type is valid, can exit early
                    } else if (commandChild.NodeName.equals("VNAME")) {
                     //   System.out.println("Found return value node: " + commandChild.NodeName);
                        
                        // Check if VNAME type is numeric
                        if (!isNumericVName(commandChild)) {
                            System.out.println("Error: Return value is not numeric.");
                            return false;
                        }

                        // The return type is valid
                        return true;  // Return type is valid, can exit early
                    }
                }
            }
        }

        // Recursively check deeper in the tree if needed
        if (!validateReturnType(child, ftypNode)) {
            return false;
        }
    }
    return true;  // Return true if all return statements match the expected return type
}

// Function to check if the CONST is a numeric value
private boolean isNumericConst(Node constNode) {

    String constValue = constNode.childNodes.get(0).NodeName;  // Assuming NodeName holds the value of CONST
    // Regex to match numeric values
    //System.out.println("this is return value"+ constValue);
    return constValue.matches("-?[0-9]+(\\.[0-9]+)?");
}

// Function to check if the VNAME is numeric
private boolean isNumericVName(Node vnameNode) {
    if (vnameNode.childNodes.isEmpty()) {
      //  System.out.println("Error: VNAME node has no child nodes.");
        return false;  // No variable name to check
    }

    String variableName = vnameNode.childNodes.get(0).NodeName;  // Get the variable name
   // System.out.println("Variable name: " + variableName);

    VariableProps varProps = getVariableProps(variableName);  // Retrieve variable properties

    if (varProps == null) {
        System.out.println("Error: Variable " + variableName + " not found in symbol table.");
        return false;  // Variable not found in the symbol table
    }

    //System.out.println("Variable: " + variableName + " Type: " + varProps.varType);

    // Check if varProps is not null and is of type numeric
    return varProps.varType.equals("num");  // Assuming 'n' indicates numeric type
}




// This method checks the function header.
private boolean typeCheckHeader(Node decl) {
    // Assuming HEADER is the first child of DECL
    Node headerNode = decl.childNodes.get(0);
    
    if (headerNode.NodeName.equals("HEADER")) {
        // Extract FTYP, FNAME, and VNAMEs from HEADER
        Node ftypNode = headerNode.childNodes.get(0);  // FTYP node
        Node ftyp = ftypNode.childNodes.get(0);        // Go deeper into FTYP
       // System.out.println("Function type: " + ftyp.NodeName);

        Node fnameNode = headerNode.childNodes.get(1);  // FNAME node
        Node fname = fnameNode.childNodes.get(0);       // Go deeper into FNAME
       // System.out.println("Function name: " + fname.NodeName);

        Node vname1Node = headerNode.childNodes.get(2);  // VNAME1 node
        Node vname1 = vname1Node.childNodes.get(0);      // Go deeper into VNAME1
       // System.out.println("vname1: " + vname1.NodeName);

        Node vname2Node = headerNode.childNodes.get(3);  // VNAME2 node
        Node vname2 = vname2Node.childNodes.get(0);      // Go deeper into VNAME2
       // System.out.println("vname2: " + vname2.NodeName);

        Node vname3Node = headerNode.childNodes.get(4);  // VNAME3 node
        Node vname3 = vname3Node.childNodes.get(0);      // Go deeper into VNAME3
       // System.out.println("vname3: " + vname3.NodeName);
        
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
       // System.out.println("Validating variable: " + varProps.oldName + " with Type: " + varProps.varType);
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
  //("function name : "+ functionName + "and return type:"+ functionType);
    // Retrieve the function properties from the symbol table (assuming this method exists)
    VariableProps varProps = getVariableProps(functionName);
    
    // Check if varProps is not null and compare types
    if (varProps != null) {
        //System.out.println("Found function: " + varProps.oldName + " with Type: " + varProps.varType);
        
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


// Helper method to find the ALGO node in BODY
// Helper method to recursively find the ALGO node in BODY



// Helper method to recursively find the ALGO node in BODY
private Node findAlgoNode(Node body) {
    for (Node child : body.childNodes) {
        if (child.NodeName.equals("ALGO")) {
            return child;  // Return the ALGO node if found
        } else {
            // Recursively search in the children of this node
            Node algoNode = findAlgoNode(child);
            if (algoNode != null) {
                return algoNode;  // Return if found deeper in the tree
            }
        }
    }
    return null;  // Return null if ALGO is not found
}


// Check prolog, always returns true as per base case.
private boolean typeCheckProlog(Node prolog) {
   // System.out.println("inside prlog");
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
//System.out.println("inside localvars");
    String varType = "";
    boolean status = true; // Assume success until proven otherwise

    // Loop through the child nodes of the HEADER node to find VNAMEs
    for (Node child : headerNode.childNodes) {
        if (child.NodeName.equals("FTYP")) {
            varType = getChildValue(child).trim(); // Clean the variable type
            while (varType.endsWith(":")) {
                varType = varType.substring(0, varType.length() - 1);
            }
          //  System.out.println("Variable TYPE: " + varType);
        } 
        else if (child.NodeName.equals("VNAME")) {
            // Retrieve and clean the variable name
            String varName = getChildValue(child).trim();
            while (varName.endsWith(":")) {
                varName = varName.substring(0, varName.length() - 1);
            }
            //System.out.println("Variable NAME: " + varName);
           // 
            // Retrieve the variable properties from the symbol table
            VariableProps varProps = getVariableProps(varName);
            
            // Check if varProps is not null and compare types
            if (varProps != null) {
              //  System.out.println("Found Variable: " + varProps.oldName + " with Type: " + varProps.varType);
                if (!varProps.varType.equals(varType)) {
                  //  System.out.println("Type mismatch for variable: " + varName + ". Found: " + varProps.varType + ", Expected: " + varType);
                    return false; // Type mismatch
                }
               // System.out.println("Variable " + varName + " matches type: " + varType);
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
   // System.out.println("entering epilog");
    return true; // Base case
}
// Helper method to find the SUBFUNCS node in BODY
// Helper method to find the SUBFUNCS node in BODY
// Helper method to find the SUBFUNCS node inside BODY
// Helper method to find the SUBFUNCS node inside BODY
// Find the ALGO node within BODY

// Helper method to find the SUBFUNCS node inside BODY, DECL, or FUNCTIONS
private Node findSubFuncsNode(Node node) {
   // System.out.println("finding subfuncts: " + node.NodeName);

    // Check if the current node is SUBFUNCS
    if (node.NodeName.equals("SUBFUNCS")) {
       // System.out.println("found it");
        return node;
    }

    // Traverse all child nodes recursively
    for (Node child : node.childNodes) {
        Node foundSubFuncs = findSubFuncsNode(child);  // Recursive search in all children
        if (foundSubFuncs != null) {
            return foundSubFuncs;  // Return the SUBFUNCS if found
        }
    }

    return null;  // Return null if SUBFUNCS is not found
}


// Type check sub-functions inside SUBFUNCS
private boolean typeCheckSubFunctions(Node subFuncsNode) {
   // System.out.println("Checking sub-functions...");
    for (Node function : subFuncsNode.childNodes) {
        if (!typeCheckFunctions(function)) {
            return false;  // Return false if any sub-function fails to type-check
        }
    }
    return true;  // All sub-functions passed the type-check
}

// Main function to type-check BODY node, including ALGO and SUBFUNCS
private boolean typeCheckBody(Node body, Node ftypNode) {
    Node algoNode = findAlgoNode(body);  // Get the ALGO node from BODY
    Node subFuncsNode = findSubFuncsNode(body);  // Get the SUBFUNCS node from BODY

    if (algoNode != null) {
        return typeCheckProlog(body)
            && typeCheckLocVars(body)
            && typeCheckAlgo(algoNode)
            && validateReturnType(algoNode, ftypNode)  // Validate the return type
            && typeCheckEpilog(body)
            && (subFuncsNode == null || typeCheckSubFunctions(subFuncsNode));  // Check SUBFUNCS if present
    } else {
        System.out.println("Error: ALGO node not found in BODY.");
        return false;  // Return false if ALGO is missing
    }
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
    public void printTableToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Integer key : table.keySet()) {
                VariableProps varProps = table.get(key);
                writer.write("ID: " + key + ", Old Name: " + varProps.oldName + ", Translated Name: " + varProps.translatedName + ", Type: " + varProps.varType);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
   public  HashMap<Integer, VariableProps> getmap(){

        return this.table;

    }
}