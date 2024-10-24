import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ICG {
    SymbolTable symbolTable;
    Node rootNode;
    Pattern numberPattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
    int currPlace;
    int currLabel;

    public class ReturnCodeArgs {
        public StringBuilder code;
        public ArrayList<StringBuilder> argList;
    }

    public ICG() {
        currPlace = 0;
        currLabel = 0;
        symbolTable = new SymbolTable();
        rootNode = new Node("PROG");
    }

    public ICG(SymbolTable st, Node rn) {
        symbolTable = st;
        rootNode = rn;
        currPlace = 0;
        currLabel = 0;
    }

    public StringBuilder newVar() {
        StringBuilder sb = new StringBuilder("var");
        return sb.append(currPlace++);
    }
    public String lookupInTable(Node node){
        return symbolTable.table.get(node.id).translatedName;
    }
    public StringBuilder newLabel() {
        StringBuilder sb = new StringBuilder("lab");
        return sb.append(currLabel++);
    }
    public StringBuilder transAlgo(Node algoNode){
        return transInstruc(algoNode.childNodes.get(1));
    }
    public StringBuilder transAtomic(Node atomicNode){
        String firstName = atomicNode.childNodes.getFirst().NodeName;
        if (firstName.equals("VNAME")) {
            return new StringBuilder(symbolTable.table.get(atomicNode.childNodes.getFirst().id).translatedName);
        }
        if (firstName.equals("CONST")) {
            return transConst(atomicNode.childNodes.getFirst());
        }
        return new StringBuilder();
    }
    public StringBuilder transConst(Node constNode){
        return new StringBuilder(constNode.childNodes.getFirst().NodeName);
    }
    public StringBuilder transArg(Node argNode){
        return new StringBuilder();
    }
    public StringBuilder transUnop(Node unopNode){
        switch (unopNode.childNodes.getFirst().NodeName) {
            case "not":
                return new StringBuilder("!");
            case "sqrt":
                return new StringBuilder("sqrt");
        }
        return new StringBuilder();
    }
    public StringBuilder transBinop(Node binopNode){
        switch (binopNode.childNodes.getFirst().NodeName) {
            case "and":
                return new StringBuilder("AND");
            case "or":
                return new StringBuilder("OR");
            case "eq":
                return new StringBuilder("=");
            case "grt":
                return new StringBuilder(">");
            case "add":
                return new StringBuilder("+");
            case "sub":
                return new StringBuilder("-");
            case "mul":
                return new StringBuilder("*");
            case "div":
                return new StringBuilder("/");
        }
        return new StringBuilder();
    }
    public StringBuilder transOp(Node opNode, String place){
        String firstName = opNode.childNodes.getFirst().NodeName;
        if (firstName.equals("UNOP")) {
            StringBuilder place1 = newVar();
            StringBuilder code1 = transArg(opNode.childNodes.get(2));
            StringBuilder unop = transUnop(opNode.childNodes.getFirst());
            return code1.append("\n").append(place).append(":=").append(unop).append("()").append(place1).append(")x`\n");
        }
        if(firstName.equals("BINOP")){
            StringBuilder place1 = newVar();
            StringBuilder place2 = newVar();
            StringBuilder code1 = transArg(opNode.childNodes.get(2));
            StringBuilder code2 = transArg(opNode.childNodes.get(4));
            StringBuilder binop = transBinop(opNode.childNodes.getFirst());
            return code1.append("\n").append(code2).append("\n").append(place).append(" := ").append(place1).append(" ").append(binop).append(" ").append(place2).append("\n");
        }
        return new StringBuilder();
    }
    public StringBuilder transTerm(Node termNode){
        String firstName = termNode.childNodes.getFirst().NodeName;
        if (firstName.equals("ATOMIC")) {
            return transAtomic(termNode.childNodes.getFirst());
        }
        if (firstName.equals("CALL")) {
            return transCall(termNode.childNodes.getFirst());
        }
        if (firstName.equals("OP")) {
            return transOp(termNode.childNodes.getFirst(), newVar().toString());
        }
        return new StringBuilder();
    }
    public StringBuilder transAssign(Node assignNode){
        String op = assignNode.childNodes.get(1).NodeName;
        if (op.equals("<")) {
            StringBuilder code1 = transVname(assignNode.childNodes.get(2));
            return new StringBuilder("INPUT ").append(code1).append("\n");
        }
        if (op.equals("=")) {
            StringBuilder place = newVar();
            StringBuilder x = transVname(assignNode.childNodes.get(0));
            StringBuilder transedTerm = transTerm(assignNode.childNodes.getLast());
            return transedTerm.append(x).append(":=").append(place).append("\n");
        }
        return new StringBuilder();
    }
    public StringBuilder transVname(Node vNameNode){
        return new StringBuilder(symbolTable.table.get(vNameNode.id).translatedName);
    }
    public StringBuilder transCall(Node callNode){
        StringBuilder ret = new StringBuilder("CALL_");
        ret.append(lookupInTable(callNode.childNodes.getFirst()));
        ret.append("(");
        ret.append(transAtomic(callNode.childNodes.get(2)));
        ret.append(",");
        ret.append(transAtomic(callNode.childNodes.get(4)));
        ret.append(",");
        ret.append(transAtomic(callNode.childNodes.get(6)));
        ret.append(")\n");
        return ret;
    }
    public StringBuilder transBranch(Node callNode){
        
        return new StringBuilder();
    }
    public StringBuilder transCommand(Node commandNode){
        String firstName = commandNode.childNodes.getFirst().NodeName;
        if (firstName.equals("skip")) {
            return new StringBuilder("REM DO NOTHING\n");
        }
        if (firstName.equals("halt")) {
            return new StringBuilder("STOP\n");
        }
        if (firstName.equals("print")) {
            StringBuilder code1 = transAtomic(commandNode.childNodes.get(1));
            return new StringBuilder("PRINT ").append(code1).append("\n");
        }
        if (firstName.equals("ATOMIC")) {
            return new StringBuilder("FUNCTIONS NOT IMPLEMENTED\n");
        }
        if (firstName.equals("ASSIGN")) {
            return transAssign(commandNode.childNodes.getFirst());
        }
        if (firstName.equals("CALL")) {
            return transCall(commandNode.childNodes.getFirst());
        }
        if (firstName.equals("BRANCH")) {
            return transBranch(commandNode.childNodes.getFirst());
        }
        
        return new StringBuilder();
    }
    public StringBuilder transInstruc(Node instrucNode){
        //Instruc null
        if (instrucNode.childNodes.size() == 0) {
            return new StringBuilder("REM END \n");
        }
        if (instrucNode.childNodes.size() == 3) {
            StringBuilder code1 = transCommand(instrucNode.childNodes.get(0));
            StringBuilder code2 = transInstruc(instrucNode.childNodes.getLast());
            return code1.append(code2);
        }
        return new StringBuilder();

    }
    public StringBuilder transProg(Node prognode){
        StringBuilder code = new StringBuilder("");
        code.append(prognode.childNodes.get(2));
        code.append("STOP\n");

        return code;
    }
}
