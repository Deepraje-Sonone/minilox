package com.minilox.interpreter;

import org.junit.jupiter.api.Test;

import static com.minilox.TestSupport.run;
import static com.minilox.TestSupport.runtimeError;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ControlFlowTest {

    @Test
    void ifTakesTheThenBranchWhenTrue() {
        assertEquals("yes", run("if (true) print \"yes\"; else print \"no\";"));
    }

    @Test
    void ifTakesTheElseBranchWhenFalse() {
        assertEquals("no", run("if (false) print \"yes\"; else print \"no\";"));
    }

    @Test
    void ifWithoutElseDoesNothingWhenFalse() {
        assertEquals("done", run("if (false) print \"x\"; print \"done\";"));
    }

    @Test
    void conditionsUseMiniLoxTruthiness() {
        assertEquals("zero is truthy", run("if (0) print \"zero is truthy\";"));
        assertEquals("null is falsy",
                run("if (null) print \"no\"; else print \"null is falsy\";"));
        assertEquals("empty string is truthy", run("if (\"\") print \"empty string is truthy\";"));
    }

    @Test
    void elseIfChain() {
        String source = """
                let n = 5;
                if (n < 3) print "small";
                else if (n < 10) print "medium";
                else print "large";
                """;
        assertEquals("medium", run(source));
    }

    @Test
    void elseBelongsToTheNearestIf() {
        assertEquals("b", run("if (true) if (false) print \"a\"; else print \"b\";"));
    }

    @Test
    void whileLoop() {
        assertEquals("0\n1\n2", run("let i = 0; while (i < 3) { print i; i = i + 1; }"));
    }

    @Test
    void whileLoopThatNeverRuns() {
        assertEquals("ok", run("while (false) print \"x\"; print \"ok\";"));
    }

    @Test
    void forLoop() {
        assertEquals("0\n1\n2", run("for (let i = 0; i < 3; i = i + 1) print i;"));
    }

    @Test
    void forLoopVariableDoesNotLeakOutOfTheLoop() {
        var error = runtimeError("for (let i = 0; i < 2; i = i + 1) {}\nprint i;");
        assertEquals("Undefined variable 'i'.", error.getMessage());
    }

    @Test
    void forLoopWithoutInitializer() {
        assertEquals("0\n1", run("let i = 0; for (; i < 2; i = i + 1) print i;"));
    }

    @Test
    void forLoopWithoutIncrement() {
        assertEquals("0\n1", run("for (let i = 0; i < 2;) { print i; i = i + 1; }"));
    }

    @Test
    void nestedLoops() {
        String source = """
                for (let i = 1; i <= 2; i = i + 1) {
                  for (let j = 1; j <= 2; j = j + 1) {
                    print i * 10 + j;
                  }
                }
                """;
        assertEquals("11\n12\n21\n22", run(source));
    }

    @Test
    void accumulatingInALoop() {
        assertEquals("55",
                run("let s = 0; for (let i = 1; i <= 10; i = i + 1) s = s + i; print s;"));
    }

    @Test
    void errorInsideAConditionReportsTheLine() {
        var error = runtimeError("if (\"a\" > 1) print 1;");
        assertEquals("Operands must be numbers.", error.getMessage());
        assertEquals(1, error.getLine());
    }

    @Test
    void fizzBuzz() {
        String source = """
                for (let i = 1; i <= 15; i = i + 1) {
                  if (i % 15 == 0) print "FizzBuzz";
                  else if (i % 3 == 0) print "Fizz";
                  else if (i % 5 == 0) print "Buzz";
                  else print i;
                }
                """;
        String expected = String.join("\n",
                "1", "2", "Fizz", "4", "Buzz", "Fizz", "7", "8",
                "Fizz", "Buzz", "11", "Fizz", "13", "14", "FizzBuzz");
        assertEquals(expected, run(source));
    }
}