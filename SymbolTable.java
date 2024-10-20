import java.util.HashMap;
import java.util.Stack;
import java.util.ArrayList;

public class SymbolTable {
    HashMap<Integer, VariableProps> table;
    ArrayList<HashMap<Integer, VariableProps>> listOfTables;
    Stack<HashMap<Integer, VariableProps>> stackOfTables;
    public SymbolTable(){
        table = new HashMap<>();
        stackOfTables = new Stack<>();
    }
    public void generateFromTree(Node root){
        if (root == null) {
            System.out.println("Invalid root");
            return;
        }
        recGen(root);
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
            var currTable = stackOfTables.peek();
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
                        currTable.put(node.id, newVar);
                        table.put(node.id, newVar);
                    }
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