package com.minilox.interpreter;

import com.minilox.ast.Stmt;
import com.minilox.error.RuntimeError;
import com.minilox.lexer.Lexer;
import com.minilox.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    /** Runs the source and returns everything it printed, one line per print. */
    private String run(String source) {
        Parser parser = new Parser(new Lexer(source).scanTokens());
        List<Stmt> statements = parser.parse();
        assertTrue(parser.getErrors().isEmpty(),
                () -> "Unexpected parse errors: " + parser.getErrors());

        List<String> lines = new ArrayList<>();
        new Interpreter(lines::add).interpret(statements);
        return String.join("\n", lines);
    }

    private RuntimeError runtimeError(String source) {
        return assertThrows(RuntimeError.class, () -> run(source));
    }

    @Test
    void arithmetic() {
        assertEquals("14", run("print 2 + 3 * 4;"));
        assertEquals("20", run("print (2 + 3) * 4;"));
        assertEquals("2.5", run("print 10 / 4;"));
        assertEquals("1", run("print 10 % 3;"));
        assertEquals("4", run("print 7 - 2 - 1;"));
        assertEquals("-3", run("print -5 + 2;"));
    }

    @Test
    void wholeNumbersPrintWithoutDecimalPoint() {
        assertEquals("2", run("print 4 / 2;"));
        assertEquals("1", run("print 0.5 + 0.5;"));
        assertEquals("3", run("print 3.0;"));
    }

    @Test
    void stringConcatenation() {
        assertEquals("Hello, World", run("print \"Hello, \" + \"World\";"));
        assertEquals("n = 5", run("print \"n = \" + 5;"));
        assertEquals("5 items", run("print 5 + \" items\";"));
        assertEquals("ok: true", run("print \"ok: \" + true;"));
        assertEquals("xnull", run("print \"x\" + null;"));
        assertEquals("pi ~ 3.14", run("print \"pi ~ \" + 3.14;"));
    }

    @Test
    void comparisonOperators() {
        assertEquals("true", run("print 3 > 2;"));
        assertEquals("false", run("print 2 >= 3;"));
        assertEquals("true", run("print 1 < 2;"));
        assertEquals("true", run("print 2 <= 2;"));
    }

    @Test
    void equalityOperators() {
        assertEquals("true", run("print 1 == 1;"));
        assertEquals("true", run("print \"a\" == \"a\";"));
        assertEquals("true", run("print null == null;"));
        assertEquals("false", run("print 1 == \"1\";"));
        assertEquals("true", run("print 1 != 2;"));
        assertEquals("false", run("print null == false;"));
    }

    @Test
    void truthiness() {
        assertEquals("true", run("print !null;"));
        assertEquals("false", run("print !0;"));
        assertEquals("false", run("print !\"\";"));
        assertEquals("true", run("print !false;"));
        assertEquals("true", run("print !!1;"));
    }

    @Test
    void logicalOperatorsReturnOperands() {
        assertEquals("default", run("print null or \"default\";"));
        assertEquals("b", run("print \"a\" and \"b\";"));
        assertEquals("a", run("print \"a\" or \"b\";"));
        assertEquals("null", run("print null and \"b\";"));
    }

    @Test
    void logicalOperatorsShortCircuit() {
        assertEquals("false", run("print false and 1 / 0;"));
        assertEquals("true", run("print true or 1 / 0;"));
    }

    @Test
    void unaryMinus() {
        assertEquals("3", run("print --3;"));
    }

    @Test
    void statementsRunInOrder() {
        assertEquals("1\n2", run("print 1; print 2;"));
        assertEquals("a\nb", run("print \"a\"; 1 + 2; print \"b\";"));
    }

    @Test
    void divisionByZero() {
        assertEquals("Division by zero.", runtimeError("print 1 / 0;").getMessage());
    }

    @Test
    void moduloByZero() {
        assertEquals("Modulo by zero.", runtimeError("print 5 % 0;").getMessage());
    }

    @Test
    void typeErrorsAreRuntimeErrors() {
        assertEquals("Operands must be numbers.", runtimeError("print \"a\" - 1;").getMessage());
        assertEquals("Operands must be numbers.", runtimeError("print \"a\" * 2;").getMessage());
        assertEquals("Operands must be numbers.", runtimeError("print 1 < \"2\";").getMessage());
        assertEquals("Operand must be a number.", runtimeError("print -\"a\";").getMessage());
        assertEquals("Operands must be two numbers or at least one string.",
                runtimeError("print true + 1;").getMessage());
    }

    @Test
    void errorsCarryTheLineNumber() {
        assertEquals(2, runtimeError("print 1;\nprint 2 / 0;").getLine());
    }

    @Test
void stringConcatenationIsLeftAssociativeWithStringFirst() {
    assertEquals("a12", run("print \"a\" + 1 + 2;"));
}

@Test
void stringConcatenationIsLeftAssociativeWithStringLast() {
    assertEquals("3a", run("print 1 + 2 + \"a\";"));
}
@Test
void functionWithoutParameters() {
    assertEquals("Hello", run(
            "fn greet() { print \"Hello\"; } greet();"
    ));
}

@Test
void functionWithParameters() {
    assertEquals("15", run(
            "fn add(a, b) { print a + b; } add(10, 5);"
    ));
}

@Test
void functionReturnsValue() {
    assertEquals("15", run(
            "fn add(a, b) { return a + b; } print add(10, 5);"
    ));
}
@Test
void functionReturnsNullByDefault() {
    assertEquals("null", run(
            "fn greet() {} print greet();"
    ));
}

}