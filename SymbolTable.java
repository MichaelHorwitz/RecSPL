import java.util.HashMap;
import java.util.Stack;
import java.util.ArrayList;

public class SymbolTable {
    HashMap<Integer, VariableProps> table;
    //ArrayList<HashMap<Integer, VariableProps>> listOfTables;
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
    }

    public HashMap<String, VariableProps> getTable() {
        return table;
    }
    // Used to recursively generate the table from the tree
    public void recGen(Node currNode){
        if (currNode == null) {
            return;
        }
        else if(currNode.NodeName.equals("LOCVARS")){
            HashMap<Integer, VariableProps> currTable = stackOfTables.peek();
            for (Node node : currNode.childNodes) {
                if (node.NodeName.equals("VNAME")) {
                    VariableProps newVar = new VariableProps();
                    String varName = node.childNodes.get(0).NodeName;
                    if (checkAlreadyInTable(varName, table)) {
                        System.out.println("Variable already declared: " + varName);
                        return;
                    }
                    newVar.oldName = varName;
                    newVar.translatedName = "v" + node.id;
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
            for (Node node : currNode.childNodes) {
                if (node.NodeName.equals("VNAME")) {
                    VariableProps newVar = new VariableProps();
                    String varName = node.childNodes.get(0).NodeName;
                    if (checkAlreadyInTable(varName, table)) {
                        System.out.println("Variable already declared: " + varName);
                        return;
                    }
                    newVar.oldName = varName;
                    newVar.translatedName = "v" + node.id;
                    table.put(node.id, newVar);
                    stackOfTables.peek().put(node.id, newVar);
                }
            }
        }
        else if (currNode.NodeName.equals("GLOBVARS")){
            var currTable = stackOfTables.peek();    
            for (Node node : currNode.childNodes) {
                if (node.NodeName.equals("VNAME")) {
                    VariableProps newVar = new VariableProps();
                    String varName = node.childNodes.get(0).NodeName;
                    if (checkAlreadyInTable(varName, table)) {
                        System.out.println("Variable already declared: " + varName);
                        return;
                    }
                    newVar.oldName = varName;
                    newVar.translatedName = "v" + node.id;
                    table.put(node.id, newVar);
                    currTable.put(node.id, newVar);
                }
            }
        } else if (currNode.NodeName.equals("ASSIGN")) {
            var currTable = stackOfTables.peek();    
            for (Node node : currNode.childNodes) {
                    if (node.NodeName.equals("VNAME")) {
                        VariableProps newVar = new VariableProps();
                        String varName = node.childNodes.get(0).NodeName;
                        if (checkAlreadyInTable(varName, currTable)) {
                            System.out.println("Variable already declared: " + varName);
                            return;
                        }
                        newVar.oldName = varName;
                        newVar.translatedName = "v" + node.id;
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
            System.out.println("ID: " + key + ", Old Name: " + varProps.oldName + ", Translated Name: " + varProps.translatedName);
        }
    }
}
