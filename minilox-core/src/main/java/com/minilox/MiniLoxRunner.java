package com.minilox;

import com.minilox.ast.AstPrinter;
import com.minilox.ast.Stmt;
import com.minilox.error.Diagnostic;
import com.minilox.error.Diagnostic.Kind;
import com.minilox.error.MiniLoxException;
import com.minilox.error.RuntimeError;
import com.minilox.interpreter.Interpreter;
import com.minilox.lexer.Lexer;
import com.minilox.lexer.Token;
import com.minilox.parser.Parser;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MiniLoxRunner {

    private final int maxCallDepth;

    public MiniLoxRunner() {
        this(Interpreter.DEFAULT_MAX_CALL_DEPTH);
    }

    public MiniLoxRunner(int maxCallDepth) {
        this.maxCallDepth = maxCallDepth;
    }

    /**
     * Lex, parse and run the source.
     * Printed lines go to the supplied output consumer.
     */
    public RunResult run(String source, Consumer<String> output) {
        return guarded(() -> {
            Parsed parsed = parse(source);

            if (!parsed.errors().isEmpty()) {
                return RunResult.failed(parsed.errors());
            }

            new Interpreter(output, maxCallDepth)
                    .interpret(parsed.statements());

            return RunResult.success();
        });
    }

    /**
     * Lex and parse only, then print the syntax tree.
     */
    public RunResult dumpAst(String source, Consumer<String> output) {
        return guarded(() -> {
            Parsed parsed = parse(source);

            if (!parsed.errors().isEmpty()) {
                return RunResult.failed(parsed.errors());
            }

            output.accept(
                    new AstPrinter().printProgram(parsed.statements())
            );

            return RunResult.success();
        });
    }

    private record Parsed(
            List<Stmt> statements,
            List<Diagnostic> errors
    ) {
    }

    private Parsed parse(String source) {

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        if (!lexer.getErrors().isEmpty()) {
            return new Parsed(
                    List.of(),
                    syntaxDiagnostics(lexer.getErrors())
            );
        }

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        return new Parsed(
                statements,
                syntaxDiagnostics(parser.getErrors())
        );
    }

    private List<Diagnostic> syntaxDiagnostics(
            List<? extends MiniLoxException> errors
    ) {
        return errors.stream()
                .map(e -> Diagnostic.of(Kind.SYNTAX, e))
                .toList();
    }

    /**
     * Converts failures into RunResult objects.
     * No expected interpreter failure should escape this method.
     */
    private RunResult guarded(Supplier<RunResult> action) {

        try {
            return action.get();

        } catch (RuntimeError e) {

            return RunResult.failed(
                    List.of(Diagnostic.of(Kind.RUNTIME, e))
            );

        } catch (MiniLoxException e) {

            return RunResult.failed(
                    List.of(Diagnostic.of(Kind.SYNTAX, e))
            );

        } catch (StackOverflowError e) {

            return RunResult.failed(
                    List.of(new Diagnostic(
                            Kind.LIMIT,
                            0,
                            "Program is nested too deeply (stack exhausted)."
                    ))
            );

        } catch (RuntimeException e) {

            return RunResult.failed(
                    List.of(new Diagnostic(
                            Kind.INTERNAL,
                            0,
                            "Internal error: " +
                                    e.getClass().getSimpleName()
                    ))
            );
        }
    }
}