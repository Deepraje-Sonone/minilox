package com.minilox.error;

public class RuntimeError extends MiniLoxException {

    public RuntimeError(int line, String message) {
        super(line, message);
    }
}