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
    public StringBuilder doTranslate(){
        return transProg(rootNode);
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

    public String lookupInTable(Node node) {
        return symbolTable.table.get(node.id).translatedName;
    }

    public StringBuilder newLabel() {
        StringBuilder sb = new StringBuilder("lab");
        return sb.append(currLabel++);
    }

    public StringBuilder transAlgo(Node algoNode) {
        return transInstruc(algoNode.childNodes.get(0));
    }

    public StringBuilder transAtomic(Node atomicNode) {
        String firstName = atomicNode.childNodes.getFirst().NodeName;
        if (firstName.substring(0,2).equals("V_")) {
            return new StringBuilder(symbolTable.table.get(atomicNode.childNodes.getFirst().id).translatedName);
        }
        else {
            return transConst(atomicNode.childNodes.getFirst());
        }
    }

    public StringBuilder transConst(Node constNode) {
        return new StringBuilder(constNode.NodeName);
    }

    public StringBuilder transArg(Node argNode) {
        return new StringBuilder();
    }

    public StringBuilder transUnop(Node unopNode) {
        switch (unopNode.childNodes.getFirst().NodeName) {
            case "not":
                return new StringBuilder("!");
            case "sqrt":
                return new StringBuilder("sqrt");
        }
        return new StringBuilder();
    }

    public StringBuilder transBinop(Node binopNode) {
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

    public StringBuilder transOp(Node opNode, String place) {
        String firstName = opNode.childNodes.getFirst().NodeName;
        if (firstName.equals("UNOP")) {
            StringBuilder place1 = newVar();
            StringBuilder code1 = transArg(opNode.childNodes.get(2));
            StringBuilder unop = transUnop(opNode.childNodes.getFirst());
            return code1.append("\n").append(place).append(":=").append(unop).append("()").append(place1)
                    .append(")x`\n");
        }
        if (firstName.equals("BINOP")) {
            StringBuilder place1 = newVar();
            StringBuilder place2 = newVar();
            StringBuilder code1 = transArg(opNode.childNodes.get(2));
            StringBuilder code2 = transArg(opNode.childNodes.get(4));
            StringBuilder binop = transBinop(opNode.childNodes.getFirst());
            return code1.append("\n").append(code2).append("\n").append(place).append(" := ").append(place1).append(" ")
                    .append(binop).append(" ").append(place2).append("\n");
        }
        return new StringBuilder();
    }

    public StringBuilder transTerm(Node termNode) {
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

    public StringBuilder transAssign(Node assignNode) {
        String op = assignNode.childNodes.get(1).NodeName;
        if (op.equals("<") || op.equals("input")) {
            StringBuilder code1 = transVname(assignNode.childNodes.get(1));
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

    public StringBuilder transVname(Node vNameNode) {
        return new StringBuilder(symbolTable.table.get(vNameNode.id).translatedName);
    }

    public StringBuilder transCall(Node callNode) {
        StringBuilder ret = new StringBuilder("CALL_");
        ret.append(lookupInTable(callNode.childNodes.getFirst()));
        ret.append("(");
        ret.append(transAtomic(callNode.childNodes.get(1)));
        ret.append(",");
        ret.append(transAtomic(callNode.childNodes.get(2)));
        ret.append(",");
        ret.append(transAtomic(callNode.childNodes.get(3)));
        ret.append(")\n");
        return ret;
    }

    public StringBuilder transSimple(Node simpNode, StringBuilder labelt, StringBuilder labelf) {
        StringBuilder relopCode = transBinop(simpNode.childNodes.getFirst());
        StringBuilder t1 = newVar();
        StringBuilder t2 = newVar();
        StringBuilder code1 = transAtomic(simpNode.childNodes.get(2));
        StringBuilder code2 = transAtomic(simpNode.childNodes.get(4));
        StringBuilder sb = new StringBuilder();
        sb.append(code1).append("\n")
                .append(code2).append("\n")
                .append(" IF ").append(t1).append(" ").append(relopCode).append(" ").append(t2)
                .append(" THEN ").append(labelt).append(" ELSE ").append(labelf);
        return sb;
    }

    public StringBuilder transComposit(Node compositNode, StringBuilder labelt, StringBuilder labelf) {
        Node op = compositNode.childNodes.getFirst();
        String opName = op.childNodes.getFirst().NodeName;
        if (op.NodeName.equals("BINOP")) {
            if (opName.equals("or")) {
                var label1 = newLabel();
                var code1 = transSimple(compositNode.childNodes.get(2), labelt, label1);
                var code2 = transSimple(compositNode.childNodes.get(4), labelt, labelf);
                return code1.append("\n LABEL ").append(label1).append("\n").append(code2);
            }
            if (opName.equals("and")) {
                var label1 = newLabel();
                var code1 = transSimple(compositNode.childNodes.get(2), label1, labelf);
                var code2 = transSimple(compositNode.childNodes.get(4), labelt, labelf);
                return code1.append("\n LABEL ").append(label1).append("\n").append(code2);
            }
            if (opName.equals("eq") || opName.equals("grt") || opName.equals("add") || opName.equals("sub") || opName.equals("mul") || opName.equals("div")) {
                var code1 = transSimple(compositNode.childNodes.get(2), labelt, labelf);
                return code1;
            }
        }
        return new StringBuilder();
    }

    public StringBuilder transCond(Node condNode, StringBuilder label1, StringBuilder label2) {
        Node childNode = condNode.childNodes.getFirst();
        if (childNode.NodeName.equals("SIMPLE")) {
            return transSimple(childNode, label1, label2);
        }
        if (childNode.NodeName.equals("COMPOSIT")) {
            return transComposit(childNode, label1, label2);
        }
        return new StringBuilder();
    }

    public StringBuilder transBranch(Node callNode) {
        StringBuilder label1 = newLabel();
        StringBuilder label2 = newLabel();
        StringBuilder label3 = newLabel();
        StringBuilder code1 = transCond(callNode.childNodes.get(1), label1, label2);
        StringBuilder code2 = transAlgo(callNode.childNodes.get(3));
        StringBuilder code3 = transAlgo(callNode.childNodes.get(5));
        return code1.append("\n").append(" LABEL ").append(label1).append("\n").append(code2).append(" GOTO ")
                .append(label3).append(" LABEL ").append(label2).append("\n").append(code3).append(" LABEL ")
                .append(label3).append("\n");
    }

    public StringBuilder transCommand(Node commandNode) {
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

    public StringBuilder transInstruc(Node instrucNode) {
        // Instruc null
        if (instrucNode.childNodes.size() == 0) {
            return new StringBuilder("REM END \n");
        }
        else{
            StringBuilder code1 = transCommand(instrucNode.childNodes.get(0));
            StringBuilder code2 = transInstruc(instrucNode.childNodes.getLast());
            return code1.append(code2);
        }
   

    }

    public StringBuilder transProg(Node prognode) {
        StringBuilder code = new StringBuilder("");
        Node algoNode = new Node("");
        for (Node node : prognode.childNodes) {
            if (node.NodeName.equals("ALGO")) {
                algoNode = node;
                break;
            }
        }
        var algo = transAlgo(algoNode);
        code.append(algo);
        code.append("STOP\n");

        return code;
    }
}
