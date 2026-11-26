package com.minilox.error;

public class LimitExceededException extends MiniLoxException {

    public LimitExceededException(String message) {
        super(0, message);
    }
}