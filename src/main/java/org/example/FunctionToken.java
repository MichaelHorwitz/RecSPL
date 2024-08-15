package org.example;

import java.util.regex.Pattern;

public class FunctionToken extends Token{
    public FunctionToken(){
        pattern = Pattern.compile("F_[a-z]([a-z]|[0-9])*");
        type= "FunctionToken";
    }
}