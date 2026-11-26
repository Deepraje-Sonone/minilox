
package com.minilox;

import com.minilox.ast.AstPrinter;
import com.minilox.ast.Stmt;
import com.minilox.error.Diagnostic;
import com.minilox.error.Diagnostic.Kind;
import com.minilox.error.LimitExceededException;
import com.minilox.error.MiniLoxException;
import com.minilox.error.RuntimeError;
import com.minilox.interpreter.Interpreter;
import com.minilox.interpreter.RunLimits;
import com.minilox.lexer.Lexer;
import com.minilox.lexer.Token;
import com.minilox.parser.Parser;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MiniLoxRunner {

    private final RunLimits limits;

    public MiniLoxRunner() {
        this(RunLimits.unlimited());
    }

    public MiniLoxRunner(int maxCallDepth) {
        this(RunLimits.unlimited().withMaxCallDepth(maxCallDepth));
    }

    public MiniLoxRunner(RunLimits limits) {
        this.limits = limits;
    }

    public RunResult run(String source, Consumer<String> output) {
        return guarded(() -> {
            checkSourceLength(source);

            Parsed parsed = parse(source);

            if (!parsed.errors().isEmpty()) {
                return RunResult.failed(parsed.errors());
            }

            Consumer<String> limitedOutput = new LimitedOutput(
                    output,
                    limits.maxOutputLines(),
                    limits.maxOutputChars()
            );

            new Interpreter(limitedOutput, limits)
                    .interpret(parsed.statements());

            return RunResult.success();
        });
    }

    public RunResult dumpAst(String source, Consumer<String> output) {
        return guarded(() -> {
            checkSourceLength(source);

            Parsed parsed = parse(source);

            if (!parsed.errors().isEmpty()) {
                return RunResult.failed(parsed.errors());
            }

            output.accept(new AstPrinter().printProgram(parsed.statements()));

            return RunResult.success();
        });
    }

    private void checkSourceLength(String source) {
        if (source.length() > limits.maxSourceChars()) {
            throw new LimitExceededException(
                    "Source code is too long (limit: "
                            + limits.maxSourceChars()
                            + " characters)."
            );
        }
    }

    private record Parsed(
            List<Stmt> statements,
            List<Diagnostic> errors
    ) {}

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

    private RunResult guarded(Supplier<RunResult> action) {
        try {
            return action.get();

        } catch (RuntimeError e) {
            return RunResult.failed(
                    List.of(Diagnostic.of(Kind.RUNTIME, e))
            );

        } catch (LimitExceededException e) {
            return RunResult.failed(
                    List.of(Diagnostic.of(Kind.LIMIT, e))
            );

        } catch (MiniLoxException e) {
            return RunResult.failed(
                    List.of(Diagnostic.of(Kind.SYNTAX, e))
            );

        } catch (StackOverflowError e) {
            return RunResult.failed(List.of(
                    new Diagnostic(
                            Kind.LIMIT,
                            0,
                            "Program is nested too deeply (stack exhausted)."
                    )
            ));

        } catch (RuntimeException e) {
            return RunResult.failed(List.of(
                    new Diagnostic(
                            Kind.INTERNAL,
                            0,
                            "Internal error: " + e.getClass().getSimpleName()
                    )
            ));
        }
    }

    private static final class LimitedOutput implements Consumer<String> {

        private final Consumer<String> delegate;
        private final int maxLines;
        private final int maxChars;

        private int lines = 0;
        private long chars = 0;

        private LimitedOutput(
                Consumer<String> delegate,
                int maxLines,
                int maxChars
        ) {
            this.delegate = delegate;
            this.maxLines = maxLines;
            this.maxChars = maxChars;
        }

        @Override
        public void accept(String line) {
            lines++;
            chars += line.length() + 1L;

            if (lines > maxLines || chars > maxChars) {
                throw new LimitExceededException(
                        "Output limit exceeded ("
                                + maxLines + " lines or "
                                + maxChars + " characters)."
                );
            }

            delegate.accept(line);
        }
    }
}
