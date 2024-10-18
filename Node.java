import java.util.ArrayList;

public class Node {
    public String NodeName; // Use this for storing the node's name
    public ArrayList<Node> childNodes; // List to store child nodes
    public int line; // To store the line number, if needed
    public int id;
    public Node(String name) {
        this.NodeName = name;
        this.childNodes = new ArrayList<>(); // Initialize the childNodes list
    }
    public Node getChild(String name) {
        for (Node child : childNodes) {
            if (child.NodeName.equals(name)) {
                return child;
            }
        }
        return null; // Return null if no matching child is found
    }
}
