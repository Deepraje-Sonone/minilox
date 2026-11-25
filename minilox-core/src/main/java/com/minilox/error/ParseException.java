package com.minilox.error;

public class ParseException extends MiniLoxException {

    public ParseException(int line, String message) {
        super(line, message);
    }
}