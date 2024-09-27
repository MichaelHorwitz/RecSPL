package org.example;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Parser {
    public Queue<Token> inputStream;
    public ArrayList<Rule> rules;
    Parser(){
        inputStream  = new LinkedList<>();
        rules = new ArrayList<>();
    }
    public void loadRules(String fileName){
        Path filePath = Paths.get(fileName);

        try {
            // Read all lines from the file
            List<String> lines = Files.readAllLines(filePath);
            // Print each line
            for (String line : lines) {
                String [] halves = line.split(":");
                List<String> firstSet = new ArrayList<>();
                String[] outTokens = halves[1].split(" ");
                for(String s: outTokens){
                    firstSet.add(s);
                }
                Rule rule = new Rule(halves[0], firstSet);
                rules.add(rule);
            }
        } catch (IOException e) {
            // Handle the exception
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }
    public SyntaxNode matchToken(){
        Token currentToken = inputStream.peek();
        for(Rule rule: rules){
            // Check if the inpute token is in the rule first set
            for(String s: rule.firstSet){
                if(s.equals(currentToken.data)){
                    inputStream.poll();
                    SyntaxNode node = new SyntaxNode();
                    node.token = currentToken;
                    node.token.data = rule.inputToken;
                    return node;
                }
            }
            if(rule.inputToken.equals(currentToken.type)){
                for(String s: rule.firstSet){
                    if(s.equals(currentToken.data)){
                        inputStream.poll();
                        SyntaxNode node = new SyntaxNode();
                        node.token = currentToken;
                        return node;
                    }
                }
            }
        }
        System.err.println("Error: Token does not match any rule.");
        return null;
    }
}
