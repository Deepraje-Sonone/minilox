
package com.minilox;

import com.minilox.error.Diagnostic;
import com.minilox.error.Diagnostic.Kind;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MiniLoxRunnerTest {

    private final MiniLoxRunner runner = new MiniLoxRunner();
    private final List<String> out = new ArrayList<>();

    @Test
    void successfulRun() {
        RunResult result = runner.run("print 1 + 1;", out::add);

        assertTrue(result.ok());
        assertEquals(0, result.exitCode());
        assertEquals(List.of("2"), out);
    }

    @Test
    void allLexerErrorsAreReportedAsSyntaxErrors() {
        RunResult result = runner.run(
                "let a = @;\nlet b = #;",
                out::add
        );

        assertEquals(2, result.errors().size());
        assertEquals(Kind.SYNTAX, result.errors().get(0).kind());
        assertEquals(1, result.errors().get(0).line());
        assertEquals(2, result.errors().get(1).line());
        assertEquals(65, result.exitCode());
        assertTrue(out.isEmpty());
    }

    @Test
    void parseErrorsAreSyntaxDiagnostics() {
        RunResult result = runner.run("print (1 + 2;", out::add);

        assertEquals(1, result.errors().size());

        Diagnostic error = result.errors().get(0);

        assertEquals(Kind.SYNTAX, error.kind());
        assertEquals(1, error.line());
        assertTrue(error.message().contains("Expect ')'"));
    }

    @Test
    void syntaxErrorsStopTheProgramFromRunning() {
        runner.run(
                "print \"a\";\nprint ;",
                out::add
        );

        assertTrue(out.isEmpty());
    }

    @Test
    void runtimeErrorKeepsEarlierOutput() {
        RunResult result = runner.run(
                "print \"before\";\n" +
                "print 1 / 0;\n" +
                "print \"after\";",
                out::add
        );

        assertEquals(List.of("before"), out);
        assertEquals(1, result.errors().size());

        Diagnostic error = result.errors().get(0);

        assertEquals(Kind.RUNTIME, error.kind());
        assertEquals(2, error.line());
        assertEquals("Division by zero.", error.message());
        assertEquals(70, result.exitCode());
    }

    @Test
    void callDepthLimitIsConfigurable() {
        RunResult result = new MiniLoxRunner(50)
                .run("fn f() { f(); } f();", out::add);

        assertEquals(Kind.RUNTIME, result.errors().get(0).kind());
        assertTrue(result.errors().get(0).message().contains("(50)"));
    }

    @Test
    void diagnosticFormatting() {
        assertEquals(
                "[line 3] Error: Boom",
                new Diagnostic(Kind.RUNTIME, 3, "Boom").format()
        );

        assertEquals(
                "Error: Boom",
                new Diagnostic(Kind.LIMIT, 0, "Boom").format()
        );
    }

    @Test
    void dumpAstPrintsTheTree() {
        RunResult result = runner.dumpAst(
                "print 1 + 2;",
                out::add
        );

        assertTrue(result.ok());
        assertEquals(List.of("(print (+ 1 2))"), out);
    }

    @Test
    void absurdlyDeepNestingBecomesALimitError() {
        String source = "print " +
                "(".repeat(100_000) +
                "1" +
                ")".repeat(100_000) +
                ";";

        RunResult result = runner.run(source, out::add);

        assertEquals(1, result.errors().size());
        assertEquals(Kind.LIMIT, result.errors().get(0).kind());
    }

    @Test
    void internalErrorsDoNotLeakDetails() {
        RunResult result = runner.run(
                "print 1;",
                line -> {
                    throw new IllegalStateException("secret details");
                }
        );

        Diagnostic error = result.errors().get(0);

        assertEquals(Kind.INTERNAL, error.kind());
        assertTrue(error.message().contains("IllegalStateException"));
        assertFalse(error.message().contains("secret"));
    }

    @Test
    void lexerErrorsPreventParserErrorsFromBeingReported() {
        RunResult result = runner.run(
                "print @;\nprint ;",
                out::add
        );

        assertEquals(1, result.errors().size());

        Diagnostic error = result.errors().get(0);

        assertEquals(Kind.SYNTAX, error.kind());
        assertEquals(1, error.line());
        assertTrue(error.message().contains("@"));

        assertTrue(out.isEmpty());
    }
}
