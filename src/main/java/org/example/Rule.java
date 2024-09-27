package org.example;

import java.util.List;

public class Rule {
    public List<String> firstSet;
    public String inputToken;
    public Rule(String inputToken, List<String> firstSet){
        this.inputToken = inputToken;
        this.firstSet = firstSet;
    }
}
