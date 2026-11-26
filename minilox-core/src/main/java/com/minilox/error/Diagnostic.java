package com.minilox.error;

public record Diagnostic(Kind kind, int line, String message) {

    public enum Kind {
        SYNTAX, RUNTIME, LIMIT, INTERNAL
    }

    public static Diagnostic of(Kind kind, MiniLoxException e) {
        return new Diagnostic(kind, e.getLine(), e.getMessage());
    }

    public String format() {
        if (line > 0) {
            return "[line " + line + "] Error: " + message;
        }

        return "Error: " + message;
    }
}