
package com.minilox.interpreter;

import com.minilox.ast.Expr;
import com.minilox.ast.Stmt;
import com.minilox.error.RuntimeError;
import com.minilox.lexer.Token;
import com.minilox.lexer.TokenType;

import java.util.List;
import java.util.function.Consumer;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private final Consumer<String> output;

    // Phase 5: stores variables in the current scope.
    private Environment environment = new Environment();

    public Interpreter() {
        this(System.out::println);
    }

    public Interpreter(Consumer<String> output) {
        this.output = output;
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

    // ---------- Literal and grouping expressions ----------

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression());
    }

    // ---------- Unary expressions ----------

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right());
        Token op = expr.operator();

        if (op.type() == TokenType.BANG) {
            return !isTruthy(right);
        }

        if (right instanceof Double d) {
            return -d;
        }

        throw new RuntimeError(op.line(), "Operand must be a number.");
    }

    // ---------- Logical expressions ----------

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left());

        if (expr.operator().type() == TokenType.OR) {
            if (isTruthy(left)) {
                return left;
            }
        } else {
            if (!isTruthy(left)) {
                return left;
            }
        }

        return evaluate(expr.right());
    }

    // ---------- Binary expressions ----------

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

            default -> throw new IllegalStateException(
                    "Unexpected binary operator: " + op.type());
        };
    }

    // ---------- Variables and scope (Phase 5) ----------

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name());
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value());

        environment.assign(expr.name(), value);

        // Assignment is an expression, so it returns the assigned value.
        return value;
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
            // Always restore the previous scope.
            this.environment = previous;
        }
    }

    // ---------- Statements ----------

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

    // ---------- Stubs: replaced in later phases ----------

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        throw notImplemented(expr.paren().line(), "Function calls", 7);
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        throw notImplemented(0, "if statements", 6);
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        throw notImplemented(0, "Loops", 6);
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        throw notImplemented(stmt.name().line(), "Function declarations", 7);
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        throw notImplemented(stmt.keyword().line(), "return", 7);
    }

    private RuntimeError notImplemented(int line, String feature, int phase) {
        return new RuntimeError(
                line,
                feature + " are not implemented yet (Phase " + phase + ").");
    }

    // ---------- Helper methods ----------

    private Object add(Token op, Object left, Object right) {
        if (left instanceof Double l && right instanceof Double r) {
            return l + r;
        }

        if (left instanceof String || right instanceof String) {
            return stringify(left) + stringify(right);
        }

        throw new RuntimeError(
                op.line(),
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
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean b) {
            return b;
        }

        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }

        if (a == null) {
            return false;
        }

        if (a instanceof Double x && b instanceof Double y) {
            return x.doubleValue() == y.doubleValue();
        }

        return a.equals(b);
    }

    public static String stringify(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof Double d) {
            if (d == Math.rint(d) && Math.abs(d) < 1e15) {
                return String.valueOf(d.longValue());
            }

            return d.toString();
        }

        return value.toString();
    }
}
