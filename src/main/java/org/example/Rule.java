package org.example;

public class Rule {
    private enum RuleType {
        Follow, First
    }
    public RuleType type;
    public Token inputToken;
    public Token outputToken;
    public Rule(Token inputToken, Token outputToken, RuleType type) {
        this.inputToken = inputToken;
        this.outputToken = outputToken;
        this.type = type;
    }
}
