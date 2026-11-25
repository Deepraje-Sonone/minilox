
package com.minilox.lexer;

public enum TokenType {

    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, SEMICOLON, PLUS, MINUS, STAR, SLASH, PERCENT,

    // One or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals and names
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    LET, FN, PRINT, IF, ELSE, WHILE, FOR, RETURN,
    TRUE, FALSE, NULL, AND, OR,

    EOF
}
