package org.example;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;


public class XMLWriter {


    private ArrayList<Token> tokens;
    private FileWriter fileWriter;
    private Deque<StringBuilder> tagStack;

    public XMLWriter() {
        this(null, null);
    }

    public XMLWriter(ArrayList<Token> tokens) {
        this(tokens, null);
    }

    public XMLWriter(FileWriter fileWriter) {
        this(null, fileWriter);
    }

    public XMLWriter(ArrayList<Token> tokens, FileWriter fileWriter) {
        this.tokens = (tokens != null) ? tokens : new ArrayList<>();
        if (fileWriter != null) {
            this.fileWriter = fileWriter;
        } else {
            try {
                this.fileWriter = new FileWriter("src/main/resources/output.txt");
            } catch (IOException e) {
                System.err.println("Error creating FileWriter: " + e.getMessage());
            }
        }
        tagStack = new ArrayDeque<>();
    }

    public boolean writeTokens(){
        var outStr = new StringBuilder();
        outStr.append("<TOKENSTREAM>\n");
        for (var token : tokens) {
            outStr.append("<TOK>" + "\n");
            outStr.append("<ID>");
            outStr.append(token.id);
            outStr.append("</ID>" + "\n");
            outStr.append("<CLASS>");
            outStr.append(token.type);
            outStr.append("</CLASS>" + "\n");
            outStr.append("<WORD>");
            outStr.append(token.data);
            outStr.append("</WORD>" + "\n");
            outStr.append("</TOK>" + "\n");
        }
        outStr.append("</TOKENSTREAM>\n");
        System.out.println(outStr);
        try {
            // fileWriter = new ter("src/main/resources/output.txt");
            fileWriter.write(outStr.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}