//import java.util.HashMap;
//
//import java.util.List;
//
//
//
//import java.io.FileWriter;
//import java.io.IOException;
//
//import java.util.ArrayList;
//import java.util.Stack;
//class SymbolTable2 {
//
//    ArrayList<SymbolNode> varTable=new ArrayList<SymbolNode>();   //A list of variables
//    ArrayList<SymbolNode> procTable=new ArrayList<SymbolNode>();
//    StringBuilder htmlContent = new StringBuilder();
//    Node parseTree;
//    static String scope="MAIN";
//    Stack<String> scopeStack = new Stack<>();
//    public SymbolTable2(Node tree){
//        this.parseTree=tree;
//    }
//
//    public void addSymbols(){
//        Node prog=parseTree;
//        for(Node child:prog.childNodes){
//            if(child.NodeName.equals("GLOBVARS")){
//                GLOBVARS(child);
//               // System.out.println("i was here for glob");
//            }
//            else if(child.NodeName.equals("ALGO")){
//                ALGO(child);
//            }
//            else if(child.NodeName.equals("FUNCTIONS")){
//              FUNCTIONS(child);
//            }
//        }
//
//         SymbolTablesToHtml("SymbolTable.html");
//}
//private void GLOBVARS(Node globvars) {
//    String name = null;
//    String type = null;
//
//    // Debug output and process each child node
//    for (Node child : globvars.childNodes) {
//        if (child.NodeName.equals("VTYP")) {
//            // Retrieve the type and clean it by trimming whitespace and removing trailing semicolons
//            type = getChildValue(child).trim(); // Clean leading/trailing whitespace
//            while (type.endsWith(":")) { // Remove all trailing semicolons
//                type = type.substring(0, type.length() - 1);
//            }
//            System.out.println("Type: " + type); // Debug output
//        } else if (child.NodeName.equals("VNAME")) {
//            // Clean the variable name by trimming whitespace and removing trailing colons
//            name = getChildValue(child).trim();
//            while (name.endsWith(":")) { // Remove all trailing colons
//                name = name.substring(0, name.length() - 1);
//            }
//
//            System.out.println("Name: " + name); // Debug output
//
//            // Create a SymbolNode with the cleaned name and type
//            SymbolNode node = new SymbolNode(name, false, scope);
//            node.type = type;
//
//            // Check if the variable already exists in the current scope
//            if (!variableLookUp(node.name, scope)) {
//                varTable.add(node);
//            } else {
//                // Output semantic error and exit if the variable already exists
//                System.err.println("Semantic Error: Variable with the name " + node.name + " already exists in scope " + scope);
//                System.exit(1);
//            }
//        }
//    }
//}
//
//
//
//
//
//
//public void ALGO(Node algo) {
//    // Expecting the structure: begin INSTRUC end
//    for (Node child : algo.childNodes) {
//        if (child.NodeName.equals("begin")) {
//            System.out.println("Starting ALGO processing.");
//        } else if (child.NodeName.equals("INSTRUC")) {
//            INSTRUC(child); // Process instructions
//        } else if (child.NodeName.equals("end")) {
//            System.out.println("Ending ALGO processing.");
//            return;
//            // Check if there are FUNCTIONS to process after the end of ALGO
//            //if (i + 1 < algo.childNodes.size() && algo.childNodes.get(i + 1).NodeName.equals("FUNCTIONS")) {
//               // FUNCTIONS(algo.childNodes.get(i + 1)); // Process FUNCTIONS if present
//           // }
//           // return; // Exit ALGO processing when 'end' is encountered
//        }
//
//    }
//}
//
//
//public void INSTRUC(Node instruc) {
//    // Check if INSTRUC is null or has no children
//    if (instruc == null || instruc.childNodes.isEmpty()) {
//        return; // Nullable, so just return if there's nothing to process
//    }
//
//    // Process each child node
//    for (int i = 0; i < instruc.childNodes.size(); i++) {
//        Node child = instruc.childNodes.get(i);
//        System.out.println("Inside INSTRUC NodeName: " + child.NodeName); // Debug output
//
//        // Check if the child is a COMMAND
//        if (child.NodeName.equals("COMMAND")) {
//            COMMAND(child); // Process the COMMAND node
//        } else if (child.NodeName.equals(";")) {
//            // Skip the semicolon token
//        } else {
//            System.out.println("Unexpected child in INSTRUC: " + child.NodeName);
//        }
//
//        // Check for another INSTRUC after the current child
//        if (i + 1 < instruc.childNodes.size() && instruc.childNodes.get(i + 1).NodeName.equals("INSTRUC")) {
//            System.out.println("Found next INSTRUC, processing recursively");
//            INSTRUC(instruc.childNodes.get(i + 1)); // Recursively process the next INSTRUC
//            break; // Exit the loop since we've handled the next INSTRUC
//        }
//    }
//}
//
//
//public void COMMAND(Node command) {
//    System.out.println("Inside COMMAND");
//
//    for (int i = 0; i < command.childNodes.size(); i++) {
//        Node child = command.childNodes.get(i);
//        System.out.println("Found child NodeName: " + child.NodeName); // Debug output
//
//        if (child.NodeName.equals("skip")) {
//            // No action needed for skip
//        } else if (child.NodeName.equals("halt")) {
//            System.out.println("Halting execution."); // Add halt logic as necessary
//        } else if (child.NodeName.equals("print")) {
//            if (i + 1 < command.childNodes.size()) {
//                Node atomicNode = command.childNodes.get(i + 1);
//                ATOMIC(atomicNode); // Process the ATOMIC node
//                i++; // Skip the ATOMIC node
//            } else {
//                System.err.println("Error: No ATOMIC node after print.");
//            }
//        } else if (child.NodeName.equals("ASSIGN")) {
//            ASSIGN(child); // Process assignment
//        } else if (child.NodeName.equals("CALL")) {
//            System.out.println("Processing CALL");
//             CALL(child);
//            // Ensure CALL node contains function name and arguments
//            Node functionNameNode = null;
//            List<Node> argumentNodes = new ArrayList<>();
//
//            // Find FNAME and ATOMIC nodes
//            for (Node callChild : child.childNodes) {
//                if (callChild.NodeName.equals("FNAME")) {
//                    functionNameNode = callChild; // Store the function name node
//                } else if (callChild.NodeName.equals("ATOMIC")) {
//                    argumentNodes.add(callChild); // Store the arguments
//                }
//            }
//
//            // Check if function name and arguments were found
//            if (functionNameNode != null) {
//                System.out.println("Function Name: " + functionNameNode.NodeName);
//
//                // Process the function call with arguments
//                for (Node argument : argumentNodes) {
//                    ATOMIC(argument); // Process each argument
//                }
//            } else {
//                System.err.println("Error: CALL missing function name or arguments.");
//            }
//        } else if (child.NodeName.equals("BRANCH")) {
//          //  BRANCH(child); // Process BRANCH
//        } else {
//            System.err.println("Unknown command: " + child.NodeName);
//        }
//    }
//}
//
//
//public void ATOMIC(Node atomic) {
//    if (atomic.childNodes != null) {
//
//
//        // Proceed with the existing logic for ATOMIC nodes
//        for (Node child : atomic.childNodes) {
//            String variableName = child.NodeName.trim();  // Ensure variable name is trimmed
//            System.out.println("Checking if variable '" + variableName + "' exists in the scope '" + scope + "'.");
//
//            // Check if the name follows the variable naming pattern (e.g., V_varName)
//            if (variableName.matches("V_[a-z]([a-z0-9])*")) {
//                // Check if the variable exists in the symbol table
//                boolean exists = variableExists(variableName, scope);
//
//                // Output the result of the variable existence check
//                if (exists) {
//                    System.out.println("Variable '" + variableName + "' exists in the scope '" + scope + "'.");
//                } else {
//                    System.out.println("Variable '" + variableName + "' does NOT exist in the scope '" + scope + "'.");
//                    System.exit(1);  // Exit if the variable does not exist
//                }
//            } else if (child.NodeName.matches("-?[0-9]+(\\.[0-9]+)?") || child.NodeName.matches("\"[A-Z][a-z]{0,7}\"")) {
//                String constantValue = getChildValue(child);  // Get the constant value
//                System.out.println("Constant value found: " + constantValue);
//            } else {
//                System.out.println("Unexpected NodeName: " + child.NodeName);
//            }
//        }
//    }
//}
//
//
//
//
//
//
//public void ASSIGN(Node assign) {
//    String vname = null;
//    boolean isTermAssignment = false;
//
//    System.err.println("Entering ASSIGN");
//
//    // Step 1: Find the VNAME (variable name)
//    for (Node child : assign.childNodes) {
//        if (child.NodeName.equals("VNAME")) {
//            // Assuming the actual name is a child of VNAME
//            if (!child.childNodes.isEmpty()) {
//                // Extract and clean the variable name (VNAME)
//                vname = getChildValue(child.childNodes.get(0)).trim(); // Clean leading/trailing whitespace
//                while (vname.endsWith(":")) { // Remove all trailing colons
//                    vname = vname.substring(0, vname.length() - 1);
//                }
//                System.out.println("Variable Name: " + vname);
//            }
//        }
//
//        // Step 2: Detect term assignment
//        if (child.NodeName.equals("TERM")) {
//            isTermAssignment = true;
//            TERM(child); // Process the term assignment
//        }
//    }
//
//    // Step 3: Check if the variable has been declared
//    if (vname != null && !variableExists(vname, scope)) {
//        System.err.println("Semantic Error: Variable " + vname + " must be declared before assignment.");
//        System.exit(1);
//    }
//
//    // Step 4: Handle Term Assignment (VNAME = TERM)
//    if (isTermAssignment) {
//        // Process TERM in the TERM method
//        System.out.println("Assigned TERM to " + vname);
//    }
//}
//
//
//// Helper method to handle TERM
//private void TERM(Node termNode) {
//    // Recursively parse the TERM, allowing deep nesting
//    for (Node child : termNode.childNodes) {
//        if (child.NodeName.equals("ATOMIC")) {
//            String atomicValue = getChildValue(child);  // Extract atomic value
//            System.out.println("Atomic value: " + atomicValue);
//            // Perform evaluation and assignment logic here for atomic values
//        } else if (child.NodeName.equals("CALL")) {
//            // Handle function calls within TERM if applicable
//            CALL(child);
//        } else if (child.NodeName.equals("OP")) {
//            // Handle operators (binary/unary operations) within TERM
//           // OP(child);
//        }
//    }
//}
//
//
//private void assignVariable(String vname, String value) {
//    // Assign the value to the variable in your symbol table or runtime environment
//    // Example: varTable.put(vname, value);
//    // Implement your specific logic here for storing the variable
//}
//
//// Helper method to retrieve the value of a child node
//
//// Helper method to get the parent scope (you need to implement this based on how scope works in your system)
//
//
//
//
//
//private void CALL(Node callNode) {
//    String functionName = null;
//    List<String> arguments = new ArrayList<>();
//
//    // Extract the function name and arguments (ATOMIC nodes)
//    for (Node child : callNode.childNodes) {
//        if (child.NodeName.equals("FNAME")) {
//            functionName = getChildValue(child);
//        } else if (child.NodeName.equals("ATOMIC")) {
//            arguments.add(getChildValue(child)); // Extract arguments (ATOMIC)
//        }
//    }
//
//    // Ensure function name was found
//    if (functionName == null) {
//        System.err.println("Semantic Error: Missing function name in CALL");
//        System.exit(1);
//    }
//
//    // Check if the function is declared in the current or ancestor scopes
//    if (!functionLookUp(functionName, scope)) {
//        System.err.println("Semantic Error: Function " + functionName + " not declared in current scope " + scope + " or any ancestor scope.");
//        System.exit(1);
//    }
//
//    // Check for recursive call to MAIN
//    if (functionName.equals("main")) {
//        System.err.println("Semantic Error: Recursive call to MAIN is not allowed");
//        System.exit(1);
//    }
//
//    // Verify argument count against the function declaration (semantic check)
//    SymbolNode functionDeclaration = findFunctionDeclaration(functionName);
//    if (functionDeclaration == null) {
//        System.err.println("Semantic Error: Function " + functionName + " declaration not found.");
//        System.exit(1);
//    }
//
//    // Further processing of the arguments can go here (e.g., type-checking)
//}
//private SymbolNode findFunctionDeclaration(String functionName) {
//    // Iterate through the procedure table to find the function declaration
//    for (SymbolNode node : procTable) {
//        if (node.name.equals(functionName)) {
//            return node; // Function found
//        }
//    }
//    return null; // Function not found
//}
//
//
//
//
//private boolean functionLookUp(String name, String scope) {
//    // Check if function already exists in the procTable
//    for (SymbolNode node : procTable ) {
//        if (node.name.equals(name) && node.scope.equals(scope)) {
//            return true; // Function found
//        }
//    }
//    return false; // Function not found
//}
//
//
//
//
//
//
//
//
//// A helper method to retrieve the value of the first child node's text
//private String getChildValue(Node node) {
//    // Assuming the relevant value is stored in the first child node
//    if (node.childNodes.size() > 0) {
//        return node.childNodes.get(0).toString(); // Adjust if necessary based on node structure
//    }
//    return ""; // Return an empty string if there are no child nodes
//}
//
//public boolean variableLookUp(String name, String scope) {
//    for (SymbolNode node : varTable) {
//        if (node.name.equals(name) && node.scope.equals(scope)) {
//            return true; // Found the variable in the correct scope
//        }
//    }
//    return false; // Variable not found
//}
//
//
//public boolean variableExists(String name, String scope) {
//    for (int i = varTable.size() - 1; i >= 0; i--) {
//        SymbolNode s = varTable.get(i);
//
//        // Trim any leading/trailing spaces or characters in the name
//        String storedName = s.name.trim();
//        String currentScope = s.scope.trim();
//        String currentName = name.trim();  // Also trim the name being checked
//
//        // Check if the variable name matches
//        if (storedName.equals(currentName)) {
//            // Check if it matches the current scope or the global scope (MAIN)
//
//                return true;
//            }
//
//    }
//    return false;
//}
//
//public void FUNCTIONS(Node functionsNode) {
//    // Process each function declaration
//    for (Node funcChild : functionsNode.childNodes) {
//
//            String functionName = null;
//            String functionType = null;
//            List<String> incomingParams = new ArrayList<>();
//            List<SymbolNode> localVars = new ArrayList<>();
//
//            for (Node headerChild : funcChild.childNodes) {
//                if (headerChild.NodeName.equals("HEADER")) {
//                    // Extract function type and name
//                    functionType = getChildValue(headerChild.childNodes.get(0)); // FTYP
//                    functionName = getChildValue(headerChild.childNodes.get(1)); // FNAME
//
//                    // Assuming that the incoming parameters follow in a specific node
//                    Node paramsNode = headerChild.childNodes.get(2); // Adjust index as necessary
//                    for (Node param : paramsNode.childNodes) {
//                        if (param.NodeName.equals("VNAME")) {
//                            String paramName = getChildValue(param).trim();
//                            incomingParams.add(paramName);
//                        }
//                    }
//                } else if (headerChild.NodeName.equals("BODY")) {
//                    // Process the body of the function
//                    processFunctionBody(headerChild, localVars);
//                }
//            }
//
//            // Store function information in the procedure table
//            SymbolNode functionNode = new SymbolNode(functionName, true, scope);
//            functionNode.type = functionType;
//            procTable.add(functionNode);
//
//            // Add incoming parameters as local variables
//            for (String param : incomingParams) {
//                SymbolNode localNode = new SymbolNode(param, false, scope);
//                localVars.add(localNode);
//            }
//
//    }
//}
//
//private void processFunctionBody(Node bodyNode, List<SymbolNode> localVars) {
//    for (Node bodyChild : bodyNode.childNodes) {
//        if (bodyChild.NodeName.equals("PROLOG")) {
//            // Handle prolog if necessary (e.g., setup)
//        } else if (bodyChild.NodeName.equals("LOCVARS")) {
//            // Process local variable declarations
//            for (Node locVarChild : bodyChild.childNodes) {
//                if (locVarChild.NodeName.equals("VTYP")) {
//                    String localType = getChildValue(locVarChild).trim();
//                    // Assuming the local variable names are defined next
//                    for (Node localNameChild : locVarChild.childNodes) {
//                        String localName = getChildValue(localNameChild).trim();
//                        SymbolNode localNode = new SymbolNode(localName, false, scope);
//                        localNode.type = localType;
//                        localVars.add(localNode);
//                    }
//                }
//            }
//        } else if (bodyChild.NodeName.equals("ALGO")) {
//            ALGO(bodyChild); // Process the ALGO section
//        } else if (bodyChild.NodeName.equals("EPILOG")) {
//            // Handle epilog if necessary
//        } else if (bodyChild.NodeName.equals("SUBFUNCS")) {
//            FUNCTIONS(bodyChild); // Recursively process any sub-functions
//        }
//    }
//}
//
//public void printTables(){
//
//    //print Vtable
//    System.out.println("Variables' Table");
//    for(SymbolNode vNode:varTable){
//        System.out.println("| Variable name:"+vNode.name+" \t| Variable type:"+vNode.type+" \t| Variable used?: "+vNode.used+" \t| Variable scope:"+vNode.scope);
//    }
//
//    System.out.println();
//    System.out.println("Procedures' Table");
//    for(SymbolNode vNode:procTable){
//        System.out.println("| Procedure name:"+vNode.name+" \t| Procedure called?: "+vNode.used+" \t| Procedure defined?:"+vNode.defined);
//    }
//}
//
//public void SymbolTablesToHtml(String outputFilePath) {
//
//
//    htmlContent.append("<h1>Variable Table</h1>");
//    htmlContent.append("<table border=\"1\" style=\"font-size: 25px;\">");
//    htmlContent.append("<tr><th>Variable name</th><th>Variable type</th><th>Variable used?</th><th>Variable scope</th></tr>");
//    for (SymbolNode vNode:varTable){
//        htmlContent.append("<tr>");
//        htmlContent.append("<td>").append(vNode.name).append("</td>");
//        htmlContent.append("<td>").append(vNode.type).append("</td>");
//        htmlContent.append("<td>").append(vNode.used).append("</td>");
//        htmlContent.append("<td>").append(vNode.scope).append("</td>");
//        htmlContent.append("</tr>");
//    }
//    htmlContent.append("</table>");
//
//    // Generate HTML markup for the procedure symbol table
//    htmlContent.append("<h1>Procedure Table</h1>");
//    htmlContent.append("<table border=\"1\" style=\"font-size: 25px;\">");
//    htmlContent.append("<tr><th>Procedure name</th><th>Procedure called?</th><th>Procedure defined?</th></tr>");
//    for (SymbolNode pNode : procTable) {
//        htmlContent.append("<tr>");
//        htmlContent.append("<td>").append(pNode.name).append("</td>");
//        htmlContent.append("<td>").append(pNode.used).append("</td>");
//        htmlContent.append("<td>").append(pNode.defined).append("</td>");
//        htmlContent.append("</tr>");
//    }
//    htmlContent.append("</table>");
//
//    // Write HTML content to file
//    try (FileWriter writer = new FileWriter(outputFilePath)) {
//        writer.write(htmlContent.toString());
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//}
//
//}
//
//