
package com.minilox.error;

public class LexerException extends MiniLoxException {

    public LexerException(int line, String message) {
        super(line, message);
    }
}
