package org.example;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        FileWriter writer = null;
        try {
            Lexer lexer = new Lexer();
            writer = new FileWriter("output.txt");  // Open output file for writing

            if (lexer.readFile("/mnt/c/Users/Sello/Documents/RecSPL/src/main/java/org/example/input.txt")) {  // Input file to read tokens from
                lexer.processTokens(); // Process tokens from the file
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();  // Ensure the writer is closed
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
