
package com.minilox.interpreter;

import com.minilox.error.RuntimeError;
import com.minilox.lexer.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw undefined(name);
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme())) {
            values.put(name.lexeme(), value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw undefined(name);
    }

    private RuntimeError undefined(Token name) {
        return new RuntimeError(
                name.line(),
                "Undefined variable '" + name.lexeme() + "'.");
    }
}
