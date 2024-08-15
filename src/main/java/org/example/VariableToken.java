package org.example;

import java.util.regex.Pattern;

public class VariableToken extends Token{
    public VariableToken(){
        pattern = Pattern.compile("V_[a-z]([a-z]|[0-9])*");
    }
}