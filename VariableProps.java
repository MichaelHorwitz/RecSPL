public class VariableProps {
    String translatedName;
    String value;
    String oldName;  
    String varType;  

    public VariableProps(String varType) {
        this.varType = varType;
    }
    public VariableProps(){}
     // Method to retrieve the type of the variable
     public String getType() {
        return this.varType;
    }
}
