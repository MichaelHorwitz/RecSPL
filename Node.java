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
    @Override
    public String toString() {
        // Return the name of the node along with its child values
        StringBuilder sb = new StringBuilder();
        sb.append(NodeName).append(": ");
        for (Node child : childNodes) {
            sb.append(child.toString()).append(" "); // Recursively add child node strings
        }
        return sb.toString().trim(); // Remove any trailing spaces
    }

    
}
