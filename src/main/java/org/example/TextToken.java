package org.example;

import java.util.regex.Pattern;

public class TextToken extends Token{
    public TextToken(){
        pattern = Pattern.compile("\"[A-Z][a-z]{0,7}\"");
        type= "TextToken";
    }
}