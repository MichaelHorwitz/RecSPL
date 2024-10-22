import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;
import java.util.ArrayList;
public class RecSPLMain {
    public static void main(String[] args) {
        final String boldPurple = "\033[1;35m";  // Bold Purple
        final String boldGreen = "\033[1;32m";   // Bold Green
        
        RecSPLLexer lexer = new RecSPLLexer(); // Create a lexer instance
        try {
            // Lexical Analysis
            boolean lex = lexer.readFile("input.txt"); // Replace with your input file
            if (lex) {  // Lexer didn't give any error
                lexer.Tokenize();
                System.out.println(boldGreen + " 1. Lexer has tokenized the input successfully. Check LexerOutput.txt");
                System.out.println();

                // Syntax Analysis
                Parser parser = new Parser(lexer.tokens); // Create parser instance
                boolean parseSuccess = parser.PROG(); // Parse the tokens using the PROG rule

                if (parseSuccess) {  // Parsed successfully
                    System.out.println(boldGreen+ "2. Program parsed successfully!!!!");
                    System.out.println();

                    // Print the parse tree to XML
                    try (FileWriter writer = new FileWriter("SyntaxTree.xml")) { // Create XML writer for the parse tree
                        parser.printParseTree(parser.headNode, writer);
                        System.out.println(boldPurple+ "Parse tree has been written to SyntaxTree.xml");
                    } catch (IOException e) {
                        System.out.println("An error occurred while writing to file: " + e.getMessage());
                    }
                } else {
                    System.out.println("Parsing Error: The program could not be parsed.");
                }

                SymbolTable symbolTable = new SymbolTable();
            symbolTable.generateFromTree(parser.headNode);
               // symbolTable.printTable();
               
             //  SymbolTable2 TC = new SymbolTable2(parser.headNode);
               // TC.addSymbols();
            } else {
                System.out.println("Lexical Error: Unable to tokenize the input.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while reading the input file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}
