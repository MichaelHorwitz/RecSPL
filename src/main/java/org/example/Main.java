package org.example;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
   
    public static final String RED = "\033[0;31m";
     public static final String YELLOW = "\033[0;33m";
    public static void main(String[] args) {
        String inputFilePath = "/mnt/c/Users/Sello/Documents/RecSPL/src/main/java/org/example/input.txt";
        String outputFilePath = "/mnt/c/Users/Sello/Documents/RecSPL/src/main/java/org/example/output.txt";
    
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            Lexer lexer = new Lexer();
    
            if (lexer.readFile(inputFilePath)) {
                boolean noErrors = lexer.processTokens();
    
               
                if (noErrors) {
                    List<Token> allTokens = lexer.getTokens();
    
                    if (allTokens != null && !allTokens.isEmpty()) {
                        for (Token token : allTokens) {
                            writer.write(token.data + token.id + System.lineSeparator());
                        }
                        System.out.println(YELLOW+" Tokens have been successfully written to the output file.");
                    } /*else {
                        System.err.println("No tokens were retrieved from the input file.");
                    }/* */
                } else {
                    System.err.println(RED+ "Lexical errors were found. The tokens were not written to the output file.");
                }
            } else {
                System.err.println("Failed to read the input file: " + inputFilePath);
            }
        } catch (IOException e) {
            System.err.println("An I/O error occurred while processing the files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

}
