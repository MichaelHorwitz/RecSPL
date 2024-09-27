package org.example;

import java.util.regex.Pattern;

public class RuleToken extends Token{
    public RuleToken(){
        pattern = Pattern.compile(".+");
        type= "TextToken";
    }
}