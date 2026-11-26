package com.minilox;

import com.minilox.error.Diagnostic;

import java.util.List;

public record RunResult(List<Diagnostic> errors) {

    public static RunResult success() {
        return new RunResult(List.of());
    }

    public static RunResult failed(List<Diagnostic> errors) {
        return new RunResult(List.copyOf(errors));
    }

    public boolean ok() {
        return errors.isEmpty();
    }

    /**
     * Process exit code:
     * 0  = success
     * 65 = syntax errors
     * 70 = runtime, limit or internal errors
     */
    public int exitCode() {
        if (ok()) {
            return 0;
        }

        return switch (errors.get(0).kind()) {
            case SYNTAX -> 65;
            case RUNTIME, LIMIT, INTERNAL -> 70;
        };
    }
}