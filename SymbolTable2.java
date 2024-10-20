import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;



import java.util.ArrayList;
class SymbolTable2 {

    ArrayList<SymbolNode> varTable=new ArrayList<SymbolNode>();   //A list of variables
    ArrayList<SymbolNode> procTable=new ArrayList<SymbolNode>();
    StringBuilder htmlContent = new StringBuilder();
    Node parseTree;
    static String scope="MAIN";

    public SymbolTable2(Node tree){
        this.parseTree=tree;
    } 

    public void addSymbols(){
        Node prog=parseTree;
        for(Node child:prog.childNodes){
            if(child.NodeName.equals("GLOBVARS")){
                GLOBVARS(child);
               // System.out.println("i was here for glob");
            }
            else if(child.NodeName.equals("ALGO")){
                ALGO(child);
            }
            else if(child.NodeName.equals("FUNCTIONS")){
               FUNCTIONS(child);
            }
        } 

        SymbolTablesToHtml("SymbolTable.html");
}
private void GLOBVARS(Node globvars) {
    String name = null;
    String type = null;
    // Debug output and process each child node
    for (Node child : globvars.childNodes) {
        if (child.NodeName.equals("VTYP")) {
            // Retrieve the type directly (assuming it's the first child node's value)
            type = getChildValue(child); // Extract the type value
            System.out.println("Type: " + type); // Debug output
        } else if (child.NodeName.equals("VNAME")) {
            // Retrieve the variable name directly (assuming it's the first child node's value)
            name = getChildValue(child); // Extract the name value
            System.out.println("Name: " + name); // Debug output
            
            // Create a SymbolNode with the name and other attributes
            SymbolNode node = new SymbolNode(name, false, scope);
            
            // Check if the variable already exists in the current scope
            if (!variableLookUp(node.name, scope)) {
                varTable.add(node); // Add to the variable table if it doesn't exist
            } else {
                // Output semantic error and exit if the variable already exists
                System.err.println("Semantic Error: Variable with the name " + node.name + " already exists in scope " + scope);
                System.exit(1);
            }
        }
    }
}


  
public void ALGO(Node algo) {
    // Expecting the structure: begin INSTRUC end
    for (Node child : algo.childNodes) {
        if (child.NodeName.equals("begin")) {
            System.out.println("Starting ALGO processing.");
        } else if (child.NodeName.equals("INSTRUC")) {
            INSTRUC(child); // Process instructions
        } else if (child.NodeName.equals("end")) {
            System.out.println("Ending ALGO processing.");
            return; 
            // Check if there are FUNCTIONS to process after the end of ALGO
            //if (i + 1 < algo.childNodes.size() && algo.childNodes.get(i + 1).NodeName.equals("FUNCTIONS")) {
               // FUNCTIONS(algo.childNodes.get(i + 1)); // Process FUNCTIONS if present
           // }
           // return; // Exit ALGO processing when 'end' is encountered
        }
       
    }
}


public void INSTRUC(Node instruc) {
    // Check if INSTRUC is null or has no children
    if (instruc == null || instruc.childNodes.isEmpty()) {
        return; // Nullable, so just return if there's nothing to process
    }

    // Process each child node
    for (int i = 0; i < instruc.childNodes.size(); i++) {
        Node child = instruc.childNodes.get(i);
        System.out.println("Inside INSTRUC NodeName: " + child.NodeName); // Debug output

        // Check if the child is a COMMAND
        if (child.NodeName.equals("COMMAND")) {
            COMMAND(child); // Process the COMMAND node
        } else if (child.NodeName.equals(";")) {
            // Skip the semicolon token
        } else {
            System.out.println("Unexpected child in INSTRUC: " + child.NodeName);
        }

        // Check for another INSTRUC after the current child
        if (i + 1 < instruc.childNodes.size() && instruc.childNodes.get(i + 1).NodeName.equals("INSTRUC")) {
            System.out.println("Found next INSTRUC, processing recursively");
            INSTRUC(instruc.childNodes.get(i + 1)); // Recursively process the next INSTRUC
            break; // Exit the loop since we've handled the next INSTRUC
        }
    }
}


public void COMMAND(Node command) {
    System.out.println("Inside COMMAND");

    for (int i = 0; i < command.childNodes.size(); i++) {
        Node child = command.childNodes.get(i);
        System.out.println("Found child NodeName: " + child.NodeName); // Debug output

        if (child.NodeName.equals("skip")) {
            // No action needed for skip
        } else if (child.NodeName.equals("halt")) {
            System.out.println("Halting execution."); // Add halt logic as necessary
        } else if (child.NodeName.equals("print")) {
            if (i + 1 < command.childNodes.size()) {
                Node atomicNode = command.childNodes.get(i + 1);
                ATOMIC(atomicNode); // Process the ATOMIC node
                i++; // Skip the ATOMIC node
            } else {
                System.err.println("Error: No ATOMIC node after print.");
            }
        } else if (child.NodeName.equals("ASSIGN")) {
            ASSIGN(child); // Process assignment
        } else if (child.NodeName.equals("CALL")) {
            System.out.println("Processing CALL");

            // Ensure CALL node contains function name and arguments
            Node functionNameNode = null;
            List<Node> argumentNodes = new ArrayList<>();

            // Find FNAME and ATOMIC nodes
            for (Node callChild : child.childNodes) {
                if (callChild.NodeName.equals("FNAME")) {
                    functionNameNode = callChild; // Store the function name node
                } else if (callChild.NodeName.equals("ATOMIC")) {
                    argumentNodes.add(callChild); // Store the arguments
                }
            }

            // Check if function name and arguments were found
            if (functionNameNode != null) {
                System.out.println("Function Name: " + functionNameNode.NodeName);

                // Process the function call with arguments
                for (Node argument : argumentNodes) {
                    ATOMIC(argument); // Process each argument
                }
            } else {
                System.err.println("Error: CALL missing function name or arguments.");
            }
        } else if (child.NodeName.equals("BRANCH")) {
            BRANCH(child); // Process BRANCH
        } else {
            System.err.println("Unknown command: " + child.NodeName);
        }
    }
}


public void ATOMIC(Node atomic) {
    for (Node child : atomic.childNodes) {
        System.out.println("I'm ATOMIC's child: " + child.NodeName); 
        
        if (child.NodeName.equals("VNAME")) {
            if (child.childNodes.size() > 0) {
                String variableName = getChildValue(child); // Correctly fetch the variable name
              //  // Debug output
                //System.out.println("Checking existence of variable: " + variableName + " in scope: " + scope);
                // Check if the variable exists in the symbol table
                if (variableExists(variableName, scope)) {
                  //  System.out.println("Variable " + variableName + " exists in the symbol table.");
                } else {
                  //  System.out.println("Variable " + variableName + " does not exist in the symbol table.");
                    System.exit(1);
                    // Handle the case where the variable does not exist (e.g., throw an error)
                }
            }
        } 
        else if (child.NodeName.equals("CONST")) {
            if (child.childNodes.size() > 0) {
                String constantValue = getChildValue(child); // Get the constant value correctly
                System.out.println("Constant value found: " + constantValue);
            }
        }
    }
   
}



 

 public void ASSIGN(Node assign) {
    // Variables to store VNAME and the assignment type
    String vname = null;
    boolean isInputAssignment = false;
    boolean isTermAssignment = false;
         System.err.println("entering assign");
    // Parse the children of the 'assign' node
    for (Node child : assign.childNodes) {
        // Step 1: Find the VNAME (variable name)
        if (child.NodeName.equals("VNAME")) {
            vname = getChildValue(child);  // Extract the variable name (VNAME)
            System.out.println("Variable Name: " + vname);
        }

        // Step 2: Check if it's an input assignment or term assignment
        if (child.NodeName.equals("INPUT_TOKEN")) {  // Assuming "INPUT_TOKEN" represents '< input'
            isInputAssignment = true;  // Detect input assignment
        } else if (child.NodeName.equals("TERM")) {
            isTermAssignment = true;  // Detect term assignment
            TERM(child);  // Call the TERM parsing method for deep nesting
        }
    }

    // Step 3: Handle Input Assignment (VNAME < input)
    if (isInputAssignment) {
        // No runtime input, just handle the assignment as per grammar
        assignVariable(vname, "<input>");
        System.out.println("Assigned '< input>' to " + vname);
    }

    // Step 4: Handle Term Assignment (VNAME = TERM)
    if (isTermAssignment) {
        // TERM processing is handled in the TERM method
        System.out.println("Assigned TERM to " + vname);
    }
}

// Helper method to handle TERM
private void TERM(Node termNode) {
    // Recursively parse the TERM, allowing deep nesting
    for (Node child : termNode.childNodes) {
        if (child.NodeName.equals("ATOMIC")) {
            String atomicValue = getChildValue(child);  // Extract atomic value
            System.out.println("Atomic value: " + atomicValue);
            // Perform evaluation and assignment logic here for atomic values
        } else if (child.NodeName.equals("CALL")) {
            // Handle function calls within TERM if applicable
            CALL(child);
        } else if (child.NodeName.equals("OP")) {
            // Handle operators (binary/unary operations) within TERM
           // OP(child);
        }
    }
}

public void BRANCH(Node branch) {
    Node condNode = null;
    Node thenAlgoNode = null;
    Node elseAlgoNode = null;
    boolean isThen = false;
    boolean isElse = false;

    // Step 1: Parse the child nodes to identify COMPOSIT or SIMPLE as the condition, then ALGO in 'then' and 'else'
    for (Node child : branch.childNodes) {
        if (child.NodeName.equals("COMPOSIT") || child.NodeName.equals("SIMPLE")) {
            condNode = child;  // Assign the condition node
        } else if (child.NodeName.equals("then")) {
            isThen = true;  // Now expect the next ALGO node to be part of 'then'
            isElse = false;
        } else if (child.NodeName.equals("else")) {
            isElse = true;  // Now expect the next ALGO node to be part of 'else'
            isThen = false;
        } else if (child.NodeName.equals("ALGO")) {
            if (isThen) {
                thenAlgoNode = child;  // Assign the ALGO for the 'then' part
            } else if (isElse) {
                elseAlgoNode = child;  // Assign the ALGO for the 'else' part
            }
        }
    }

    // Ensure that the condition node is not null before proceeding
    if (condNode == null) {
        throw new RuntimeException("Error: Condition node is missing in BRANCH");
    }

    // Step 2: Process the condition
    boolean conditionResult = COND(condNode);  // This will return true or false based on the evaluation of COMPOSIT or SIMPLE

    // Step 3: Execute the appropriate ALGO based on the condition result
    if (conditionResult) {
        System.out.println("Condition is TRUE, executing THEN branch");
        ALGO(thenAlgoNode);  // Execute the 'then' ALGO
    } else {
        System.out.println("Condition is FALSE, executing ELSE branch");
        ALGO(elseAlgoNode);  // Execute the 'else' ALGO
    }
}


// Method to evaluate the COND node
public boolean COND(Node cond) {
    for (Node child : cond.childNodes) {
        if (child.NodeName.equals("SIMPLE")) {
            return SIMPLE(child);  // Evaluate simple conditions
        } else if (child.NodeName.equals("COMPOSIT")) {
            return COMPOSIT(child);  // Evaluate composite conditions
        }
    }
    return false;  // Default return if no condition is matched
}

// Method to evaluate SIMPLE conditions (BINOP with ATOMIC terms)
public boolean SIMPLE(Node simple) {
    String operator = "";
    String arg1 = "", arg2 = "";
    
    for (Node child : simple.childNodes) {
        if (child.NodeName.equals("BINOP")) {
            operator = getChildValue(child);  // Get the binary operator (eq, grt, etc.)
        } else if (child.NodeName.equals("ATOMIC")) {
            if (arg1.isEmpty()) {
                arg1 = getChildValue(child);  // Get the first atomic value
            } else {
                arg2 = getChildValue(child);  // Get the second atomic value
            }
        }
    }
    
    // Evaluate the binary operator on the atomic arguments
    return evaluateBinop(operator, arg1, arg2);
}

// Method to evaluate COMPOSIT conditions (nested SIMPLE or UNOP)
public boolean COMPOSIT(Node composit) {
    String operator = "";
    boolean simple1Result = false, simple2Result = false;

    for (Node child : composit.childNodes) {
        if (child.NodeName.equals("BINOP")) {
            operator = getChildValue(child);  // Get the binary operator (and, or, etc.)
        } else if (child.NodeName.equals("SIMPLE")) {
            if (!simple1Result) {
                simple1Result = SIMPLE(child);  // Evaluate first SIMPLE condition
            } else {
                simple2Result = SIMPLE(child);  // Evaluate second SIMPLE condition
            }
        } else if (child.NodeName.equals("UNOP")) {
            return UNOP(child);  // Evaluate unary operator on SIMPLE condition
        }
    }

    // Evaluate the binary operator on the two simple results
    return evaluateBinop(operator, simple1Result, simple2Result);
}

// Method to evaluate UNOP (like not, sqrt)
public boolean UNOP(Node unop) {
    String operator = "";
    boolean simpleResult = false;
    
    for (Node child : unop.childNodes) {
        if (child.NodeName.equals("UNOP")) {
            operator = getChildValue(child);  // Get the unary operator (not, sqrt)
        } else if (child.NodeName.equals("SIMPLE")) {
            simpleResult = SIMPLE(child);  // Evaluate the SIMPLE condition
        }
    }
    
    // Evaluate the unary operator on the simple result
    return evaluateUnop(operator, simpleResult);
}

// Helper method to evaluate binary operators (and, or, eq, grt, etc.)
// Helper method to evaluate binary operators
private boolean evaluateBinop(String operator, Object arg1, Object arg2) {
    switch (operator) {
        case "eq":
            return arg1.toString().equals(arg2.toString());  // String comparison
        case "grt":
            return Integer.parseInt(arg1.toString()) > Integer.parseInt(arg2.toString());  // Numeric comparison
        case "and":
            return (boolean)arg1 && (boolean)arg2;  // Boolean AND
        case "or":
            return (boolean)arg1 || (boolean)arg2;  // Boolean OR
        // Add other BINOP cases here (like add, sub for numeric operations)
        default:
            return false;
    }
}


// Helper method to evaluate unary operators (not, sqrt)
private boolean evaluateUnop(String operator, boolean simpleResult) {
    switch (operator) {
        case "not":
            return !simpleResult;
        // Add other UNOP cases here (like sqrt if needed)
        default:
            return false;
    }
}

// Helper method to assign a value to a variable (can update your symbol table or other storage)
private void assignVariable(String vname, String value) {
    // Assign the value to the variable in your symbol table or runtime environment
    // Example: varTable.put(vname, value); 
    // Implement your specific logic here for storing the variable
}

// Helper method to retrieve the value of a child node

// Helper method to get the parent scope (you need to implement this based on how scope works in your system)



private void FUNCTIONS(Node functions) {
   
    
    // Loop through each child node in FUNCTIONS
    for (Node child : functions.childNodes) {
{
           
       
        if (child.NodeName.equals("DECL")) {
            System.out.println("Processing FUNCTIONS node...");
            DECL(child);
        }
    }
}
}
private void DECL(Node decl) {
    String functionName = null;
    String returnType = null;
    Node header = null;
    Node body = null;
    String parentScope = scope; // Track the parent scope before opening a new one for the function

    // Process the child nodes in DECL to get HEADER, FNAME, and BODY
    for (Node child : decl.childNodes) {
        if (child.NodeName.equals("HEADER")) {
            header = child; // Capture HEADER node for processing
        } else if (child.NodeName.equals("FNAME")) {
            functionName = getChildValue(child);
        } else if (child.NodeName.equals("BODY")) {
            body = child; // Capture BODY node for later processing
        }
    }

    // Ensure the function name is provided in FNAME
    if (functionName == null) {
        System.err.println("Semantic Error: Function name is missing");
        System.exit(1);
    }

    // Ensure function name does not conflict with parent or sibling scopes
    if (scopeHasConflictingFunction(functionName, parentScope)) {
        System.err.println("Semantic Error: Function " + functionName + " cannot have the same name as its parent or sibling scope");
        System.exit(1);
    }

    // Apply Semantic Rule: No recursive call to MAIN
    if (functionName.equals("main") && scope.equals("main")) {
        System.err.println("Semantic Error: Recursive call to MAIN is not allowed");
        System.exit(1);
    }

    // Create a new function node and add it to the procTable
    SymbolNode funcNode = new SymbolNode(functionName, true, scope);
    funcNode.type = returnType; // Set the return type

    // Add the function node to the procedure table (procTable)
    procTable.add(funcNode);

    // Open a new scope for the function
    scope = functionName;

    // Process the function body if provided
    if (body != null) {
        BODY(body); // Process the BODY node
    } else {
        System.err.println("Semantic Error: Function " + functionName + " is missing a BODY");
        System.exit(1);
    }

    // Restore the previous scope
    scope = parentScope;
}
private void BODY(Node body) {
    Node prolog = null;
    Node locvars = null;
    Node algo = null;
    Node epilog = null;
    Node subfuncs = null;

    // Process the body node
    for (Node child : body.childNodes) {
        if (child.NodeName.equals("PROLOG")) {
            prolog = child; // Store prolog node
        } else if (child.NodeName.equals("LOCVARS")) {
            locvars = child; // Store locvars node
        } else if (child.NodeName.equals("ALGO")) {
            algo = child; // Store algo node
        } else if (child.NodeName.equals("EPILOG")) {
            epilog = child; // Store epilog node
        } else if (child.NodeName.equals("SUBFUNCS")) {
            subfuncs = child; // Store subfunctions node
        }
    }

    // Process PROLOG
    if (prolog != null) {
        // Assuming PROLOG is just a curly brace, handle if necessary
        // e.g., System.out.println("Entering function scope"); or similar actions
    }

    // Process LOCVARS
    if (locvars != null) {
        processLocVars(locvars);
    }

    // Process ALGO
    if (algo != null) {
        // Implement your logic for processing algorithmic statements/commands here
        ALGO(algo); // Placeholder for algo processing
    }

    // Process EPILOG
    if (epilog != null) {
        // Assuming EPILOG is just a curly brace, handle if necessary
        // e.g., System.out.println("Exiting function scope"); or similar actions
    }

    // Process SUBFUNCS
    if (subfuncs != null) {
        // Handle subfunctions as needed
        FUNCTIONS(subfuncs); // Assuming FUNCTIONS is implemented to handle this
    }
}
private void CALL(Node callNode) {
    String functionName = null;
    List<String> arguments = new ArrayList<>();

    // Extract the function name and arguments (ATOMIC nodes)
    for (Node child : callNode.childNodes) {
        if (child.NodeName.equals("FNAME")) {
            functionName = getChildValue(child);
        } else if (child.NodeName.equals("ATOMIC")) {
            arguments.add(getChildValue(child)); // Extract arguments (ATOMIC)
        }
    }

    // Ensure function name was found
    if (functionName == null) {
        System.err.println("Semantic Error: Missing function name in CALL");
        System.exit(1);
    }

    // Check if the function is declared in the current or ancestor scopes
    if (!functionLookUp(functionName, scope)) {
        System.err.println("Semantic Error: Function " + functionName + " not declared in current scope " + scope + " or any ancestor scope.");
        System.exit(1);
    }

    // Check for recursive call to MAIN
    if (functionName.equals("main")) {
        System.err.println("Semantic Error: Recursive call to MAIN is not allowed");
        System.exit(1);
    }

    // Verify argument count against the function declaration (semantic check)
    SymbolNode functionDeclaration = findFunctionDeclaration(functionName);
    if (functionDeclaration == null) {
        System.err.println("Semantic Error: Function " + functionName + " declaration not found.");
        System.exit(1);
    }

    // Further processing of the arguments can go here (e.g., type-checking)
}
private SymbolNode findFunctionDeclaration(String functionName) {
    // Iterate through the procedure table to find the function declaration
    for (SymbolNode node : procTable) {
        if (node.name.equals(functionName)) {
            return node; // Function found
        }
    }
    return null; // Function not found
}

private void processLocVars(Node locvars) {
    // Expecting a comma-separated list of VTYP VNAME pairs
    for (int i = 0; i < locvars.childNodes.size(); i++) {
        Node child = locvars.childNodes.get(i);
        
        // Check for VTYP
        if (child.NodeName.equals("VTYP")) {
            String type = getChildValue(child); // Extract variable type

            // Expect the next sibling to be VNAME
            Node nameNode = (i + 1 < locvars.childNodes.size()) ? locvars.childNodes.get(i + 1) : null;

            if (nameNode != null && nameNode.NodeName.equals("VNAME")) {
                String name = getChildValue(nameNode); // Extract variable name
                SymbolNode localVarNode = new SymbolNode(name, false, scope);

                // Check for variable existence in the current scope
                if (!variableLookUp(localVarNode.name, scope)) {
                    varTable.add(localVarNode); // Add local variable to the variable table
                } else {
                    System.err.println("Semantic Error: Variable " + localVarNode.name + " already exists in scope " + scope);
                    System.exit(1);
                }

                // Skip the next nameNode since we've processed it
                i++; 
            }
        }}
    }
    private boolean scopeHasConflictingFunction(String functionName, String parentScope) {
        // Check the parent scope for function name conflicts
        for (SymbolNode node : procTable) {
            if (node.name.equals(functionName) && node.scope.equals(parentScope)) {
                return true; // Conflict found
            }
        }
        return false;
    }



private boolean functionLookUp(String name, String scope) {
    // Check if function already exists in the procTable
    for (SymbolNode node : procTable ) {
        if (node.name.equals(name) && node.scope.equals(scope)) {
            return true; // Function found
        }
    }
    return false; // Function not found
}








// A helper method to retrieve the value of the first child node's text
private String getChildValue(Node node) {
    // Assuming the relevant value is stored in the first child node
    if (node.childNodes.size() > 0) {
        return node.childNodes.get(0).toString(); // Adjust if necessary based on node structure
    }
    return ""; // Return an empty string if there are no child nodes
}

public boolean variableLookUp(String name, String scope) {
    for (SymbolNode node : varTable) {
        if (node.name.equals(name) && node.scope.equals(scope)) {
            return true; // Found the variable in the correct scope
        }
    }
    return false; // Variable not found
}


public boolean variableExists(String name,String scope){
    for(int i=varTable.size()-1;i>=0;i--){   //Since we are checking variable scopes,the last declared variable with local scope will be towards the end
        SymbolNode s=varTable.get(i);
        if(s.name.equals(name)){
            if(s.scope.equals(scope) || s.scope.equals("MAIN")){
                return true;
            }
        }
    }
    return false;
}
public void printTables(){

    //print Vtable
    System.out.println("Variables' Table");
    for(SymbolNode vNode:varTable){
        System.out.println("| Variable name:"+vNode.name+" \t| Variable type:"+vNode.type+" \t| Variable used?: "+vNode.used+" \t| Variable scope:"+vNode.scope);
    }

    System.out.println();
    System.out.println("Procedures' Table");
    for(SymbolNode vNode:procTable){
        System.out.println("| Procedure name:"+vNode.name+" \t| Procedure called?: "+vNode.used+" \t| Procedure defined?:"+vNode.defined);
    }
}

public void SymbolTablesToHtml(String outputFilePath) {
        

    htmlContent.append("<h1>Variable Table</h1>");
    htmlContent.append("<table border=\"1\" style=\"font-size: 25px;\">");
    htmlContent.append("<tr><th>Variable name</th><th>Variable type</th><th>Variable used?</th><th>Variable scope</th></tr>");
    for (SymbolNode vNode:varTable){
        htmlContent.append("<tr>");
        htmlContent.append("<td>").append(vNode.name).append("</td>");
        htmlContent.append("<td>").append(vNode.type).append("</td>");
        htmlContent.append("<td>").append(vNode.used).append("</td>");
        htmlContent.append("<td>").append(vNode.scope).append("</td>");
        htmlContent.append("</tr>");
    }
    htmlContent.append("</table>");

    // Generate HTML markup for the procedure symbol table
    htmlContent.append("<h1>Procedure Table</h1>");
    htmlContent.append("<table border=\"1\" style=\"font-size: 25px;\">");
    htmlContent.append("<tr><th>Procedure name</th><th>Procedure called?</th><th>Procedure defined?</th></tr>");
    for (SymbolNode pNode : procTable) {
        htmlContent.append("<tr>");
        htmlContent.append("<td>").append(pNode.name).append("</td>");
        htmlContent.append("<td>").append(pNode.used).append("</td>");
        htmlContent.append("<td>").append(pNode.defined).append("</td>");
        htmlContent.append("</tr>");
    }
    htmlContent.append("</table>");

    // Write HTML content to file
    try (FileWriter writer = new FileWriter(outputFilePath)) {
        writer.write(htmlContent.toString());
    } catch (IOException e) {
        e.printStackTrace();
    }
}

} 

   