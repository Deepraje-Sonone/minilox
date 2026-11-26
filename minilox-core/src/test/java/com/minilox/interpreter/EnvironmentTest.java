package com.minilox.interpreter;

import com.minilox.error.RuntimeError;
import com.minilox.lexer.Token;
import com.minilox.lexer.TokenType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentTest {

    private Token name(String lexeme) {
        return new Token(TokenType.IDENTIFIER, lexeme, null, 1);
    }

    @Test
    void definedVariableCanBeRead() {
        Environment env = new Environment();
        env.define("a", 1.0);
        assertEquals(1.0, env.get(name("a")));
    }

    @Test
    void innerScopeCanReadOuterVariables() {
        Environment outer = new Environment();
        outer.define("a", 1.0);
        Environment inner = new Environment(outer);
        assertEquals(1.0, inner.get(name("a")));
    }

    @Test
    void assignUpdatesTheScopeThatOwnsTheVariable() {
        Environment outer = new Environment();
        outer.define("a", 1.0);
        Environment inner = new Environment(outer);

        inner.assign(name("a"), 2.0);

        assertEquals(2.0, outer.get(name("a")));
    }

    @Test
    void shadowingDoesNotChangeTheOuterVariable() {
        Environment outer = new Environment();
        outer.define("a", 1.0);
        Environment inner = new Environment(outer);
        inner.define("a", 2.0);

        assertEquals(2.0, inner.get(name("a")));
        assertEquals(1.0, outer.get(name("a")));
    }

    @Test
    void undefinedVariableThrows() {
        assertThrows(RuntimeError.class, () -> new Environment().get(name("nope")));
    }

    @Test
    void nullIsAValidValue() {
        Environment env = new Environment();
        env.define("a", null);
        assertNull(env.get(name("a")));
    }
}