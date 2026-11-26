package com.minilox.interpreter;

import org.junit.jupiter.api.Test;

import static com.minilox.TestSupport.run;
import static com.minilox.TestSupport.runtimeError;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VariablesAndScopeTest {

    @Test
    void declareAndRead() {
        assertEquals("1", run("let a = 1; print a;"));
    }

    @Test
    void uninitializedVariableIsNull() {
        assertEquals("null", run("let a; print a;"));
    }

    @Test
    void assignmentChangesTheValue() {
        assertEquals("2", run("let a = 1; a = 2; print a;"));
    }

    @Test
    void assignmentIsAnExpression() {
        assertEquals("5", run("let a; print a = 5;"));
        assertEquals("6", run("let a; let b; a = b = 3; print a + b;"));
    }

    @Test
    void initializerCanUseEarlierVariables() {
        assertEquals("6", run("let a = 2; let b = a * 3; print b;"));
    }

    @Test
    void readingAnUndefinedVariableFails() {
        assertEquals("Undefined variable 'x'.", runtimeError("print x;").getMessage());
    }

    @Test
    void assigningToAnUndefinedVariableFails() {
        assertEquals("Undefined variable 'x'.", runtimeError("x = 1;").getMessage());
    }

    @Test
    void innerBlockShadowsOuterVariable() {
        assertEquals("inner\nouter",
                run("let a = \"outer\"; { let a = \"inner\"; print a; } print a;"));
    }

    @Test
    void blockCanModifyOuterVariable() {
        assertEquals("2", run("let a = 1; { a = a + 1; } print a;"));
    }

    @Test
    void blockVariablesDoNotLeak() {
        var error = runtimeError("{ let b = 1; }\nprint b;");
        assertEquals("Undefined variable 'b'.", error.getMessage());
        assertEquals(2, error.getLine());
    }

    @Test
    void nestedBlocks() {
        assertEquals("6", run("let a = 1; { let b = 2; { let c = 3; print a + b + c; } }"));
    }

    @Test
    void redeclarationInTheSameScopeIsAllowed() {
        assertEquals("2", run("let a = 1; let a = 2; print a;"));
    }

    @Test
    void nullVariableIsNotUndefined() {
        assertEquals("null", run("let a = null; print a;"));
    }

    @Test
    void variableCannotBeUsedInItsOwnInitializer() {
        assertEquals("Undefined variable 'a'.", runtimeError("let a = a;").getMessage());
    }
}