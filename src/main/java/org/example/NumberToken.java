package org.example;

import java.util.regex.Pattern;

public class NumberToken extends Token{
    public NumberToken(){
        pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
        type= "NumberToken";
    }
}