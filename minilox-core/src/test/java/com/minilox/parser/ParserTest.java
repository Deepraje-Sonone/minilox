package com.minilox.parser;

import com.minilox.ast.AstPrinter;
import com.minilox.ast.Stmt;
import com.minilox.lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private Parser parserFor(String source) {
        return new Parser(new Lexer(source).scanTokens());
    }

    /** Parses source, asserts there were no errors, returns the tree as text. */
    private String ast(String source) {
        Parser parser = parserFor(source);
        List<Stmt> statements = parser.parse();
        assertTrue(parser.getErrors().isEmpty(),
                () -> "Unexpected parse errors: " + parser.getErrors());
        return new AstPrinter().printProgram(statements);
    }

    @Test
    void multiplicationBindsTighterThanAddition() {
        assertEquals("(print (+ 2 (* 3 4)))", ast("print 2 + 3 * 4;"));
    }

    @Test
    void subtractionIsLeftAssociative() {
        assertEquals("(print (- (- 10 4) 3))", ast("print 10 - 4 - 3;"));
    }

    @Test
    void parenthesesOverridePrecedence() {
        assertEquals("(print (* (group (+ 2 3)) 4))", ast("print (2 + 3) * 4;"));
    }

    @Test
    void unaryOperators() {
        assertEquals("(print (* (- 2) 3))", ast("print -2 * 3;"));
        assertEquals("(print (== (! true) false))", ast("print !true == false;"));
    }

    @Test
    void andBindsTighterThanOr() {
        assertEquals("(print (or a (and b c)))", ast("print a or b and c;"));
    }

    @Test
    void comparisonBindsTighterThanEquality() {
        assertEquals("(print (== (< 1 2) true))", ast("print 1 < 2 == true;"));
    }

    @Test
    void assignmentIsRightAssociative() {
        assertEquals("(expr (= a (= b 3)))", ast("a = b = 3;"));
    }

    @Test
    void letDeclarations() {
        assertEquals("(let x 5)", ast("let x = 5;"));
        assertEquals("(let y)", ast("let y;"));
    }

    @Test
    void ifElse() {
        assertEquals("(if a (print 1) (print 2))", ast("if (a) print 1; else print 2;"));
    }

    @Test
    void elseBindsToNearestIf() {
        assertEquals("(if a (if b (print 1) (print 2)))",
                ast("if (a) if (b) print 1; else print 2;"));
    }

    @Test
    void forLoopIsDesugaredToWhile() {
        assertEquals(
                "(block (let i 0) (while (< i 3) (block (print i) (expr (= i (+ i 1))))))",
                ast("for (let i = 0; i < 3; i = i + 1) print i;"));
    }

    @Test
    void functionDeclarationAndCall() {
        assertEquals(
                "(fn add (a b) (return (+ a b)))\n(print (call add 1 2))",
                ast("fn add(a, b) { return a + b; }\nprint add(1, 2);"));
    }

    @Test
    void blockStatement() {
        assertEquals("(block (print 1) (print 2))", ast("{ print 1; print 2; }"));
    }

    @Test
    void missingSemicolonIsReported() {
        Parser parser = parserFor("print 1");
        parser.parse();
        assertEquals(1, parser.getErrors().size());
        assertTrue(parser.getErrors().get(0).getMessage().contains("Expect ';' after value."));
    }

    @Test
    void invalidAssignmentTargetIsReported() {
        Parser parser = parserFor("1 = 2;");
        parser.parse();
        assertEquals(1, parser.getErrors().size());
        assertTrue(parser.getErrors().get(0).getMessage().contains("Invalid assignment target."));
    }

    @Test
    void unclosedBlockIsReported() {
        Parser parser = parserFor("{ print 1;");
        parser.parse();
        assertEquals(1, parser.getErrors().size());
        assertTrue(parser.getErrors().get(0).getMessage().contains("Expect '}' after block."));
    }

    @Test
    void recoveryReportsMultipleErrors() {
        Parser parser = parserFor("print 1 +;\nprint 2 +;");
        parser.parse();
        assertEquals(2, parser.getErrors().size());
        assertEquals(1, parser.getErrors().get(0).getLine());
        assertEquals(2, parser.getErrors().get(1).getLine());
    }
}