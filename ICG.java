import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ICG {
    SymbolTable symbolTable;
    Node rootNode;
    Pattern numberPattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
    int currPlace;
    int currLabel;
    public class ReturnCodeArgs{
        public StringBuilder code;
        public ArrayList<StringBuilder> argList;
    }
    public ICG(){
        currPlace = 0;
        currLabel = 0;
        symbolTable = new SymbolTable();
        rootNode = new Node("PROG");
    }
    public ICG(SymbolTable st, Node rn){
        symbolTable = st;
        rootNode = rn;
        currPlace = 0;
        currLabel = 0;
    }
    public StringBuilder newVar(){
        StringBuilder sb = new StringBuilder("var");
        return sb.append(currPlace++);
    }
    public StringBuilder newLabel() {
        StringBuilder sb = new StringBuilder("lab");
        return sb.append(currLabel++);
    }
    public StringBuilder transOp(StringBuilder operator){
        if (operator.toString().equals("and")){
            return new StringBuilder("&");
        }
        return new StringBuilder();
    }
    public StringBuilder TransExp(Node exp, StringBuilder place){
        if (exp.NodeName.equals("CONST")) {
            if (numberPattern.matcher(exp.NodeName).matches()) {
                int v = Integer.parseInt(exp.NodeName);
                StringBuilder sb = new StringBuilder();
                sb.append(place);
                sb.append(":=");
                sb.append(v);
                return sb;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(place);
                sb.append(":=");
                sb.append(exp.NodeName);
                return sb;
            }
        }
        if (exp.NodeName.equals("COMPOSIT")) {
            Node opNode = exp.childNodes.getFirst();
            if (opNode.NodeName.equals("UNOP")) {
                StringBuilder place1 = newVar();
                StringBuilder code1 = TransExp(exp.childNodes.get(2), place1);
                StringBuilder op = transOp(new StringBuilder(opNode.childNodes.getFirst().NodeName));
                return code1.append(place).append(":=").append(op).append(place1);
            }
            if (opNode.NodeName.equals("BINOP")){
                StringBuilder place1 = newVar();
                StringBuilder place2 = newVar();
                StringBuilder code1 = TransExp(exp.childNodes.get(2), place1);
                StringBuilder code2 = TransExp(exp.childNodes.get(4), place2);
                StringBuilder op = transOp(new StringBuilder(opNode.childNodes.getFirst().NodeName));
                return code1.append(code2).append(place).append(":=").append(op).append(place2);
            }
        }
        return new StringBuilder();
    }
    public ReturnCodeArgs TransExp(Node exp) {
        StringBuilder place = newVar();
        StringBuilder code1 = TransExp(exp, place);
        ReturnCodeArgs ret = new ReturnCodeArgs();
        ret.argList.add(place);
        ret.code = code1;
        return ret;
    }

    public StringBuilder TransBranch(Node branch){
        StringBuilder label1 = newLabel();
        StringBuilder label2 = newLabel();
        StringBuilder label3 = newLabel();
        StringBuilder code1 = transCond(branch.childNodes.get(1));
        StringBuilder code2 = transAlgo(branch.childNodes.get(3));
        StringBuilder code3 = transAlgo(branch.childNodes.get(3));
        return code1.append("LABEL ").append(label1).append(code2).append("GOTO ").append(label3).append("LABEL ").append(label2).append(code3).append("LABEL ").append(label3);
    }
    public StringBuilder TransCommand(Node commandNode){
        if(commandNode.childNodes.getFirst().NodeName.equals("BRANCH")){
            return TransBranch(commandNode);
        }
        return new StringBuilder();
    }
    public StringBuilder transCond(Node node){
        return new StringBuilder();
    }
    public StringBuilder transAlgo(Node node){
        return new StringBuilder();
    }
}
