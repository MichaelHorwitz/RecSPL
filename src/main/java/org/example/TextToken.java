package org.example;

import java.util.regex.Pattern;

public class TextToken extends Token{
    public TextToken(){
        pattern = Pattern.compile("\"[A-Z][a-zA-Z0-9]{7}\"");
    }
}