package org.example;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;

public class Parser {
    Queue<Token> inputStream;
    Parser(){
        inputStream  = new LinkedList<>();
    }
    ArrayList<Rule> rules;
    public void loadRules(String fileName){
        
    }
}
