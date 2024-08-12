package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;


public class Lexer {
    private Scanner scan;
    private int currentLine;
    private List<Token> tokens;

    public boolean readFile(String filePath) throws FileNotFoundException {
      
       scan = new Scanner(new FileInputStream(filePath));
        currentLine = 1;
        tokens = new ArrayList<>();
        return true; // Successfully opened file
    }
    

    public boolean processTokens() {
        int runningId = 1;
        boolean noErrors = true;
        
        while (scan.hasNextLine()) {
            String line = scan.nextLine().trim(); // Trim whitespace to avoid issues
            int currentPosition = 0;
            
            while (currentPosition < line.length()) {
                String remainingLine = line.substring(currentPosition);
                Matcher textMatcher = new TextToken().pattern.matcher(remainingLine);
                Matcher keywordMatcher = new KeywordToken().pattern.matcher(remainingLine);
                Matcher variableMatcher = new VariableToken().pattern.matcher(remainingLine);
                Matcher numberMatcher = new NumberToken().pattern.matcher(remainingLine);
                Matcher functionMatcher = new FunctionToken().pattern.matcher(remainingLine);
                boolean tokenFound = false;
                
                if (textMatcher.lookingAt()) {
                    TextToken textToken = new TextToken();
                    textToken.data = textMatcher.group();
                    textToken.line = currentLine;
                    textToken.id = runningId++;
                    tokens.add(textToken);
                    currentPosition += textToken.data.length();
                   // System.out.println("Found TextToken: " + textToken.data);
                    tokenFound = true;
                } else if (keywordMatcher.lookingAt()) {
                    KeywordToken keywordToken = new KeywordToken();
                    keywordToken.data = keywordMatcher.group();
                    keywordToken.line = currentLine;
                    keywordToken.id = runningId++;
                    tokens.add(keywordToken);
                    currentPosition += keywordToken.data.length();
                   // System.out.println("Found KeywordToken: " + keywordToken.data);
                    tokenFound = true;
                } else if (variableMatcher.lookingAt()) {
                    VariableToken variableToken = new VariableToken();
                    variableToken.data = variableMatcher.group();
                    variableToken.line = currentLine;
                    variableToken.id = runningId++;
                    tokens.add(variableToken);
                    currentPosition += variableToken.data.length();
                   // System.out.println("Found VariableToken: " + variableToken.data);
                    tokenFound = true;
                } else if (numberMatcher.lookingAt()) {
                    NumberToken numberToken = new NumberToken();
                    numberToken.data = numberMatcher.group();
                    numberToken.line = currentLine;
                    numberToken.id = runningId++;
                    tokens.add(numberToken);
                    currentPosition += numberToken.data.length();
                   // System.out.println("Found NumberToken: " + numberToken.data);
                    tokenFound = true;
                } else if (functionMatcher.lookingAt()) {
                    FunctionToken functionToken = new FunctionToken();
                    functionToken.data = functionMatcher.group();
                    functionToken.line = currentLine;
                    functionToken.id = runningId++;
                    tokens.add(functionToken);
                    currentPosition += functionToken.data.length();
                   // System.out.println("Found FunctionToken: " + functionToken.data);
                    tokenFound = true;
                } 
                // If no valid token is found, report lexical error and set noErrors to false
                if (!tokenFound) {
                    char currentChar = remainingLine.charAt(0);
                    if (!Character.isWhitespace(currentChar)) {
                       // System.err.println("Lexical Error on line " + currentLine + 
                                          // ", Invalid token at position " + currentPosition + ": " + currentChar);
                        noErrors = false;
                    }
                    currentPosition++;
                }
            }
            currentLine++;
        }
        scan.close();
        return noErrors; // Return true if no lexical errors were found, false otherwise
    }
    
    public List<Token> getTokens() {
        return tokens;
    }
    

    

}
