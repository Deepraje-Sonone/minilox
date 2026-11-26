package com.minilox.interpreter;

import java.util.List;

public interface MiniLoxCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}