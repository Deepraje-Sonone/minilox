package com.minilox.interpreter;

/** Thrown by `return` to unwind out of nested blocks and loops. Not an error. */
class ReturnSignal extends RuntimeException {

    final Object value;

    ReturnSignal(Object value) {
        super(null, null, false, false);   // no message, no cause, no stack trace
        this.value = value;
    }
}