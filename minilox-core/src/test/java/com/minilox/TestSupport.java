package com.minilox;

import com.minilox.ast.Stmt;
import com.minilox.error.RuntimeError;
import com.minilox.interpreter.Interpreter;
import com.minilox.lexer.Lexer;
import com.minilox.parser.Parser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TestSupport {

    private TestSupport() { }

    /** Runs MiniLox source and returns everything it printed, one line per print. */
    public static String run(String source) {
        Parser parser = new Parser(new Lexer(source).scanTokens());
        List<Stmt> statements = parser.parse();
        assertTrue(parser.getErrors().isEmpty(),
                () -> "Unexpected parse errors: " + parser.getErrors());

        List<String> lines = new ArrayList<>();
        new Interpreter(lines::add).interpret(statements);
        return String.join("\n", lines);
    }

    /** Runs source that is expected to fail at runtime and returns the error. */
    public static RuntimeError runtimeError(String source) {
        return assertThrows(RuntimeError.class, () -> run(source));
    }
}