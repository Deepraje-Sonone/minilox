package com.minilox.interpreter;

import org.junit.jupiter.api.Test;

import static com.minilox.TestSupport.run;
import static com.minilox.TestSupport.runtimeError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FunctionsTest {

    @Test
    void simpleCall() {
        assertEquals("hi", run("fn greet() { print \"hi\"; } greet();"));
    }

    @Test
    void parametersReceiveArguments() {
        assertEquals("5", run("fn add(a, b) { print a + b; } add(2, 3);"));
    }

    @Test
    void returnGivesBackAValue() {
        assertEquals("5", run("fn add(a, b) { return a + b; } print add(2, 3);"));
    }

    @Test
    void functionWithoutReturnGivesNull() {
        assertEquals("null", run("fn f() {} print f();"));
    }

    @Test
    void bareReturnStopsTheFunction() {
        assertEquals("null", run("fn f() { return; print \"no\"; } print f();"));
    }

    @Test
    void returnCanEscapeFromNestedLoopsAndBlocks() {
        String source = """
                fn firstEven(limit) {
                  for (let i = 1; i <= limit; i = i + 1) {
                    if (i % 2 == 0) return i;
                  }
                  return null;
                }
                print firstEven(10);
                """;
        assertEquals("2", run(source));
    }

    @Test
    void factorialByRecursion() {
        String source = """
                fn fact(n) {
                  if (n <= 1) return 1;
                  return n * fact(n - 1);
                }
                print fact(10);
                """;
        assertEquals("3628800", run(source));
    }

    @Test
    void fibonacciByRecursion() {
        String source = """
                fn fib(n) {
                  if (n <= 1) return n;
                  return fib(n - 1) + fib(n - 2);
                }
                print fib(10);
                """;
        assertEquals("55", run(source));
    }

    @Test
    void functionsAreValues() {
        assertEquals("1", run("fn f() { return 1; } let g = f; print g();"));
        assertEquals("<fn f>", run("fn f() {} print f;"));
    }

    @Test
    void functionsCanCallOtherFunctions() {
        String source = """
                fn double(x) { return x * 2; }
                fn quad(x) { return double(double(x)); }
                print quad(3);
                """;
        assertEquals("12", run(source));
    }

    @Test
    void parametersAreLocalToTheCall() {
        String source = """
                let x = "global";
                fn f(x) { x = "changed"; return x; }
                print f("arg");
                print x;
                """;
        assertEquals("changed\nglobal", run(source));
    }

    @Test
    void functionsCanModifyGlobals() {
        assertEquals("2", run("let count = 0; fn inc() { count = count + 1; } inc(); inc(); print count;"));
    }

    @Test
    void closuresRememberTheirOwnEnvironment() {
        String source = """
                fn makeCounter() {
                  let count = 0;
                  fn increment() {
                    count = count + 1;
                    return count;
                  }
                  return increment;
                }
                let a = makeCounter();
                let b = makeCounter();
                print a();
                print a();
                print b();
                """;
        assertEquals("1\n2\n1", run(source));
    }

    @Test
    void mutualRecursion() {
        String source = """
                fn isEven(n) { if (n == 0) return true; return isOdd(n - 1); }
                fn isOdd(n) { if (n == 0) return false; return isEven(n - 1); }
                print isEven(10);
                """;
        assertEquals("true", run(source));
    }

    @Test
    void functionsCanBePassedAsArguments() {
        String source = """
                fn apply(f, x) { return f(x); }
                fn square(n) { return n * n; }
                print apply(square, 7);
                """;
        assertEquals("49", run(source));
    }

    @Test
    void nativeClockFunction() {
        assertEquals("true", run("print clock() > 0;"));
    }

    @Test
    void wrongNumberOfArgumentsIsAnError() {
        var error = runtimeError("fn f(a) {}\nf(1, 2);");
        assertEquals("Expected 1 arguments but got 2.", error.getMessage());
        assertEquals(2, error.getLine());
    }

    @Test
    void callingANonFunctionIsAnError() {
        assertEquals("Can only call functions.", runtimeError("let x = 3; x();").getMessage());
    }

    @Test
    void runawayRecursionIsStoppedCleanly() {
        var error = runtimeError("fn f() { f(); } f();");
        assertTrue(error.getMessage().startsWith("Stack overflow"));
    }
}