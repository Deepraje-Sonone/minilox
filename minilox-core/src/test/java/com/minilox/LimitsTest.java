
package com.minilox;

import com.minilox.error.Diagnostic;
import com.minilox.error.Diagnostic.Kind;
import com.minilox.interpreter.RunLimits;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LimitsTest {

    @Test
    void stopsInfiniteLoopAtStepLimit() {
        RunLimits limits = RunLimits.unlimited().withMaxSteps(100);
        MiniLoxRunner runner = new MiniLoxRunner(limits);

        RunResult result = runner.run(
                "while (true) { print 1; }",
                line -> {}
        );

        assertFalse(result.ok());
        assertEquals(Kind.LIMIT, result.errors().get(0).kind());
        assertTrue(result.errors().get(0).message().contains("Step limit"));
    }

    @Test
    void stopsExecutionAfterTimeLimit() {
        RunLimits limits = RunLimits.unlimited()
                .withTimeoutMillis(100);

        MiniLoxRunner runner = new MiniLoxRunner(limits);

        RunResult result = runner.run(
                "while (true) { print 1; }",
                line -> {}
        );

        assertFalse(result.ok());
        assertEquals(Kind.LIMIT, result.errors().get(0).kind());
    }

    @Test
    void stopsDeepRecursionAtCallDepthLimit() {
        RunLimits limits = RunLimits.unlimited()
                .withMaxCallDepth(10);

        MiniLoxRunner runner = new MiniLoxRunner(limits);

        String source = """
                fn recurse() {
                    recurse();
                }
                recurse();
                """;

        RunResult result = runner.run(source, line -> {});

        assertFalse(result.ok());
        assertEquals(Kind.RUNTIME, result.errors().get(0).kind());
        assertTrue(result.errors().get(0).message().contains("maximum call depth"));
    }

    @Test
    void limitsNumberOfOutputLines() {
        RunLimits limits = RunLimits.unlimited()
                .withMaxOutputLines(3);

        MiniLoxRunner runner = new MiniLoxRunner(limits);

        List<String> output = new ArrayList<>();

        RunResult result = runner.run(
                "print 1; print 2; print 3; print 4;",
                output::add
        );

        assertFalse(result.ok());
        assertEquals(Kind.LIMIT, result.errors().get(0).kind());
        assertEquals(List.of("1", "2", "3"), output);
    }

    @Test
    void limitsOutputCharacters() {
        RunLimits limits = RunLimits.unlimited()
                .withMaxOutputChars(5);

        MiniLoxRunner runner = new MiniLoxRunner(limits);

        RunResult result = runner.run(
                "print \"12345\";",
                line -> {}
        );

        assertFalse(result.ok());
        assertEquals(Kind.LIMIT, result.errors().get(0).kind());
    }

    @Test
    void limitsStringLength() {
        RunLimits limits = RunLimits.unlimited()
                .withMaxStringLength(5);

        MiniLoxRunner runner = new MiniLoxRunner(limits);

        RunResult result = runner.run(
                "print \"123456\";",
                line -> {}
        );

        assertFalse(result.ok());
        assertEquals(Kind.LIMIT, result.errors().get(0).kind());
    }

    @Test
    void limitsSourceLength() {
        RunLimits limits = RunLimits.unlimited()
                .withMaxSourceChars(10);

        MiniLoxRunner runner = new MiniLoxRunner(limits);

        RunResult result = runner.run(
                "print 123456789;",
                line -> {}
        );

        assertFalse(result.ok());
        assertEquals(Kind.LIMIT, result.errors().get(0).kind());
    }

    @Test
    void acceptsProgramWithinLimits() {
        RunLimits limits = RunLimits.playground();

        MiniLoxRunner runner = new MiniLoxRunner(limits);

        List<String> output = new ArrayList<>();

        RunResult result = runner.run(
                "print 10 + 20;",
                output::add
        );

        assertTrue(result.ok());
        assertEquals(List.of("30"), output);
    }
}
