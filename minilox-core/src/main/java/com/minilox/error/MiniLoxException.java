
package com.minilox.error;

public class MiniLoxException extends RuntimeException {

    private final int line;

    public MiniLoxException(int line, String message) {
        super(message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public String report() {
        return "[line " + line + "] Error: " + getMessage();
    }
}
