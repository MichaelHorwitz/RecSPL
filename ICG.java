
public class ICG {
    SymbolTable symbolTable;
    Node rootNode;
    public ICG(){
        symbolTable = new SymbolTable();
        rootNode = new Node();
    }
    public ICG(SymbolTable st, Node rn){
        symbolTable = st;
        rootNode = rn;
    }
    public StringBuilder TransExp(Node exp, StringBuilder place){
        if (exp.childNodes) {
            
        }
    }
}
