package org.example;

import java.util.regex.Pattern;

public class KeywordToken extends Token{
    public KeywordToken(){
        pattern = Pattern.compile("main|num|text|begin|end|skip|halt|print|<input|=|(|)|,|if|then|else|not|sqrt|or|and|eq|grt|add|sub|mul|div|num|void|\\{|\\}");
    }
}