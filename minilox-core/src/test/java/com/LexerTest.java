
package com.minilox.lexer;

import com.minilox.error.LexerException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.minilox.lexer.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    private List<Token> lex(String source) {
        return new Lexer(source).scanTokens();
    }

    private List<TokenType> types(String source) {
        return lex(source).stream().map(Token::type).toList();
    }

    @Test
    void emptySourceGivesOnlyEof() {
        assertEquals(List.of(EOF), types(""));
    }

    @Test
    void singleCharacterTokens() {
        assertEquals(
                List.of(LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
                        COMMA, SEMICOLON, PLUS, MINUS, STAR, SLASH, PERCENT, EOF),
                types("(){},;+-*/%"));
    }

    @Test
    void oneOrTwoCharacterOperators() {
        assertEquals(
                List.of(BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, LESS, LESS_EQUAL,
                        GREATER, GREATER_EQUAL, EOF),
                types("! != = == < <= > >="));
    }

    @Test
    void keywordsVersusIdentifiers() {
        assertEquals(
                List.of(LET, IDENTIFIER, IDENTIFIER, EOF),
                types("let x letter"));
    }

    @Test
    void numbersAreParsedAsDoubles() {
        List<Token> tokens = lex("42 3.14");

        assertEquals(42.0, tokens.get(0).literal());
        assertEquals(3.14, tokens.get(1).literal());
    }

    @Test
    void stringLiteralKeepsContentWithoutQuotes() {
        Token token = lex("\"hello world\"").get(0);

        assertEquals(STRING, token.type());
        assertEquals("hello world", token.literal());
        assertEquals("\"hello world\"", token.lexeme());
    }

    @Test
    void commentsAreSkipped() {
        assertEquals(
                List.of(PRINT, NUMBER, SEMICOLON, EOF),
                types("print 1; // ignore me"));
    }

    @Test
    void lineNumbersAreTracked() {
        List<Token> tokens = lex("let a = 1;\nlet b = 2;\n\nprint b;");

        assertEquals(1, tokens.get(0).line());
        assertEquals(2, tokens.get(5).line());
        assertEquals(4, tokens.get(10).line());
    }

    @Test
    void unexpectedCharacterThrowsWithLine() {
        LexerException exception = assertThrows(
                LexerException.class,
                () -> lex("let x = 1;\nlet y = @;"));

        assertEquals(2, exception.getLine());
        assertTrue(exception.getMessage().contains("@"));
    }

    @Test
    void unterminatedStringThrows() {
        LexerException exception = assertThrows(
                LexerException.class,
                () -> lex("print \"oops;"));

        assertEquals(1, exception.getLine());
    }

    @Test
    void fullStatement() {
        assertEquals(
                List.of(LET, IDENTIFIER, EQUAL, NUMBER, PLUS, NUMBER,
                        STAR, NUMBER, SEMICOLON, EOF),
                types("let x = 1 + 2 * 3;"));
    }

    @Test
    void operatorsWorkWithoutSpaces() {
        assertEquals(
                List.of(IDENTIFIER, GREATER_EQUAL, IDENTIFIER, EOF),
                types("a>=b"));
    }

    @Test
    void identifierCanContainNumbers() {
        assertEquals(List.of(IDENTIFIER, EOF), types("x_1"));
    }
}
