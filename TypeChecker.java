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
                
                // Check the type in the symbol table for this specific variable
                //VariableProps varProps = table.get(0);
                  
                   
                // Ensure varProps is not null and check for type match
               // if (varProps == null || !varProps.varType.equals(varType)) {
                  //  System.out.println("table has this" varProps.getType()+ "expecting this"+varType);
                   // System.out.println("Type mismatch for variable: " + varName);
                   // return false; // Type mismatch
              //  }
           // }
        //}
    
       // All variables passed the type check
    }
    
        }
    
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
        /*  if (command.isSkip()) {
            return true; // Base case

        } else if (command.isHalt()) {
            return true; // Base case

        } else if (command.isPrint()) {
            String atomicType = typeCheckAtomic(command.getAtomic());
            return atomicType.equals("n") || atomicType.equals("t");

        } else if (command.isReturn()) {
            String atomicType = typeCheckAtomic(command);
           // String functionType = symbolTable.getFunctionType(); // Find the function type in scope
            return atomicType.equals(functionType);

        } else if (command.isAssign()) {
            return typeCheckAssign(command.getAssign());

        } else if (command.isCall()) {
            String callType = typeCheckCall(command.getCall());
            return callType.equals("v"); // Check for void-type

        } else if (command.isBranch()) {
            return typeCheckBranch(command);
        }/* */

        return false;
    }

    // Type check atomic values
    private String typeCheckAtomic(Node atomic) {
       /*  if (atomic.isVarName()) {
            return symbolTable.getType(atomic.getVarName());

        } else if (atomic.isConst()) {
            return atomic.getConstType(); // Return type of constant
        }
        /* */ 
         return "u"; // Undefined
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
