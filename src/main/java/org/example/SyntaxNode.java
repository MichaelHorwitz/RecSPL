package org.example;
import java.util.ArrayList;
public class SyntaxNode {
    Token token;
    ArrayList<Token> children;
    SyntaxNode(){
        token = null;
        children = new ArrayList<>();
    }
}
