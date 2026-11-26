package com.minilox.interpreter;

import com.minilox.ast.Stmt;

import java.util.List;

public class MiniLoxFunction implements MiniLoxCallable {

    private final Stmt.Function declaration;
    private final Environment closure;   // where the function was DEFINED

    public MiniLoxFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return declaration.params().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params().size(); i++) {
            environment.define(declaration.params().get(i).lexeme(), arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body(), environment);
        } catch (ReturnSignal signal) {
            return signal.value;
        }
        return null;   // fell off the end: implicit null
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name().lexeme() + ">";
    }
}