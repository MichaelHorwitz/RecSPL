package org.example;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
   
    public static final String RED = "\033[0;31m";
    public static final String YELLOW = "\033[0;33m";
    public static void main(String[] args) {
        // String inputFilePath = "src/main/resources/input.txt";
        // String outputFilePath = "src/main/resources/output.txt";
    
        // try (FileWriter writer = new FileWriter(outputFilePath)) {
        //     Lexer lexer = new Lexer();
    
        //     if (lexer.readFile(inputFilePath)) {
        //         boolean noErrors = lexer.processTokens();
    
               
        //         if (noErrors) {
        //             List<Token> allTokens = lexer.getTokens();
    
        //             if (allTokens != null && !allTokens.isEmpty()) {
        //                 var xWriter = new XMLWriter(new ArrayList<>(allTokens));
        //                 xWriter.writeTokens();
        //                 System.out.println(YELLOW+" Tokens have been successfully written to the output file.");
        //             } /*else {
        //                 System.err.println("No tokens were retrieved from the input file.");
        //             }/* */
        //         } else {
        //             System.err.println(RED+ "Lexical errors were found. The tokens were not written to the output file.");
        //         }
        //     } else {
        //         System.err.println("Failed to read the input file: " + inputFilePath);
        //     }
        // } catch (IOException e) {
        //     System.err.println("An I/O error occurred while processing the files: " + e.getMessage());
        //     e.printStackTrace();
        // } catch (Exception e) {
        //     System.err.println("An unexpected error occurred: " + e.getMessage());
        //     e.printStackTrace();
        // }
        Parser parser = new Parser();
        KeywordToken kt = new KeywordToken();
        kt.data = "main";
        parser.inputStream.add(kt);
        parser.loadRules("src/main/resources/first sets.txt");
        SyntaxNode node = parser.matchToken();
        System.out.println(node.token.data);

    }
    

}
