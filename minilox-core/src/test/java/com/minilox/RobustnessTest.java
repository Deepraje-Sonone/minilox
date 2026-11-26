
package com.minilox;

import com.minilox.error.Diagnostic;
import com.minilox.error.Diagnostic.Kind;
import com.minilox.interpreter.RunLimits;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RobustnessTest {

    private static final String[] PIECES = {
            "let", "fn", "print", "if", "else", "while", "for", "return",
            "true", "false", "null", "and", "or", "x", "y", "f",
            "1", "2.5", "\"s\"",
            "(", ")", "{", "}", ",", ";",
            "+", "-", "*", "/", "%", "!", "!=", "=", "==",
            "<", "<=", ">", ">=", "@", "//"
    };

    @Test
    void randomTokenSoupNeverCrashesTheRunner() {

        Random random = new Random(12345);

        RunLimits limits = RunLimits.playground()
                .withMaxSteps(10_000)
                .withTimeoutMillis(200);

        MiniLoxRunner runner = new MiniLoxRunner(limits);

        for (int i = 0; i < 3000; i++) {

            String source = randomProgram(random);

            RunResult result = assertDoesNotThrow(
                    () -> runner.run(source, line -> {}),
                    "Runner threw on: " + source
            );

            for (Diagnostic error : result.errors()) {

                assertNotEquals(
                        Kind.INTERNAL,
                        error.kind(),
                        "Internal error on: " + source
                );
            }
        }
    }

    private String randomProgram(Random random) {

        StringBuilder sb = new StringBuilder();

        int length = 1 + random.nextInt(30);

        for (int i = 0; i < length; i++) {
            sb.append(PIECES[random.nextInt(PIECES.length)])
                    .append(' ');
        }

        return sb.toString();
    }
}
