package com.minilox.interpreter;

import com.minilox.ast.Expr;
import com.minilox.ast.Stmt;
import com.minilox.error.RuntimeError;
import com.minilox.lexer.Token;
import com.minilox.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    public static final int DEFAULT_MAX_CALL_DEPTH = 200;

    private final Consumer<String> output;
    private final int maxCallDepth;

    private final Environment globals = new Environment();
    private Environment environment = globals;
    private int callDepth = 0;

    public Interpreter() {
        this(System.out::println);
    }

    public Interpreter(Consumer<String> output) {
        this(output, DEFAULT_MAX_CALL_DEPTH);
    }

    public Interpreter(Consumer<String> output, int maxCallDepth) {
        this.output = output;
        this.maxCallDepth = maxCallDepth;
        defineNativeFunctions();
    }

    public void interpret(List<Stmt> statements) {
        for (Stmt statement : statements) {
            execute(statement);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    // ---------- expressions ----------

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression());
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right());
        Token op = expr.operator();

        if (op.type() == TokenType.BANG) {
            return !isTruthy(right);
        }
        // otherwise it's MINUS
        if (right instanceof Double d) {
            return -d;
        }
        throw new RuntimeError(op.line(), "Operand must be a number.");
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left());

        if (expr.operator().type() == TokenType.OR) {
            if (isTruthy(left)) return left;      // short-circuit
        } else {
            if (!isTruthy(left)) return left;     // short-circuit (AND)
        }
        return evaluate(expr.right());
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left());
        Object right = evaluate(expr.right());
        Token op = expr.operator();

        return switch (op.type()) {
            case PLUS -> add(op, left, right);
            case MINUS -> {
                checkNumberOperands(op, left, right);
                yield asNumber(left) - asNumber(right);
            }
            case STAR -> {
                checkNumberOperands(op, left, right);
                yield asNumber(left) * asNumber(right);
            }
            case SLASH -> {
                checkNumberOperands(op, left, right);
                if (asNumber(right) == 0) {
                    throw new RuntimeError(op.line(), "Division by zero.");
                }
                yield asNumber(left) / asNumber(right);
            }
            case PERCENT -> {
                checkNumberOperands(op, left, right);
                if (asNumber(right) == 0) {
                    throw new RuntimeError(op.line(), "Modulo by zero.");
                }
                yield asNumber(left) % asNumber(right);
            }
            case GREATER -> {
                checkNumberOperands(op, left, right);
                yield asNumber(left) > asNumber(right);
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(op, left, right);
                yield asNumber(left) >= asNumber(right);
            }
            case LESS -> {
                checkNumberOperands(op, left, right);
                yield asNumber(left) < asNumber(right);
            }
            case LESS_EQUAL -> {
                checkNumberOperands(op, left, right);
                yield asNumber(left) <= asNumber(right);
            }
            case EQUAL_EQUAL -> isEqual(left, right);
            case BANG_EQUAL -> !isEqual(left, right);
            default -> throw new IllegalStateException("Unexpected binary operator: " + op.type());
        };
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name());
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value());
        environment.assign(expr.name(), value);
        return value;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee());

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments()) {
            arguments.add(evaluate(argument));
        }

        int line = expr.paren().line();
        if (!(callee instanceof MiniLoxCallable function)) {
            throw new RuntimeError(line, "Can only call functions.");
        }
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(line, "Expected " + function.arity()
                    + " arguments but got " + arguments.size() + ".");
        }
        if (callDepth >= maxCallDepth) {
            throw new RuntimeError(line,
                    "Stack overflow: maximum call depth (" + maxCallDepth + ") exceeded.");
        }

        callDepth++;
        try {
            return function.call(this, arguments);
        } finally {
            callDepth--;
        }
    }

    // ---------- statements ----------

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression());
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression());
        output.accept(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer() != null) {
            value = evaluate(stmt.initializer());
        }
        environment.define(stmt.name().lexeme(), value);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements(), new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment blockEnvironment) {
        Environment previous = this.environment;
        try {
            this.environment = blockEnvironment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;   // always restore, even after an error or return
        }
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition()))) {
            execute(stmt.thenBranch());
        } else if (stmt.elseBranch() != null) {
            execute(stmt.elseBranch());
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition()))) {
            execute(stmt.body());
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        MiniLoxFunction function = new MiniLoxFunction(stmt, environment);
        environment.define(stmt.name().lexeme(), function);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value() != null) {
            value = evaluate(stmt.value());
        }
        throw new ReturnSignal(value);
    }

    // ---------- native functions ----------

    private void defineNativeFunctions() {
        globals.define("clock", new MiniLoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    // ---------- helpers ----------

    private Object add(Token op, Object left, Object right) {
        if (left instanceof Double l && right instanceof Double r) {
            return l + r;
        }
        if (left instanceof String || right instanceof String) {
            return stringify(left) + stringify(right);
        }
        throw new RuntimeError(op.line(),
                "Operands must be two numbers or at least one string.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) {
            return;
        }
        throw new RuntimeError(operator.line(), "Operands must be numbers.");
    }

    private double asNumber(Object value) {
        return (Double) value;
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        if (a instanceof Double x && b instanceof Double y) {
            return x.doubleValue() == y.doubleValue();
        }
        return a.equals(b);
    }

    public static String stringify(Object value) {
        if (value == null) return "null";
        if (value instanceof Double d) {
            if (d == Math.rint(d) && Math.abs(d) < 1e15) {
                return String.valueOf(d.longValue());   // 4.0 prints as 4
            }
            return d.toString();
        }
        return value.toString();
    }
}