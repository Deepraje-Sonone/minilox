
package com.minilox.interpreter;

import com.minilox.ast.Expr;
import com.minilox.ast.Stmt;
import com.minilox.error.RuntimeError;
import com.minilox.lexer.Token;
import com.minilox.lexer.TokenType;

import java.util.List;
import java.util.function.Consumer;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment();

    private final Consumer<String> output;

    public Interpreter() {
        this(System.out::println);
    }

    public Interpreter(Consumer<String> output) {
        this.output = output;
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            throw error;
        }
    }

    private void execute(Stmt statement) {
        statement.accept(this);
    }

    private Object evaluate(Expr expression) {
        return expression.accept(this);
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

    return switch (expr.operator().type()) {
        case BANG -> !isTruthy(right);
        case MINUS -> {
            if (!(right instanceof Double number)) {
                throw new RuntimeError(expr.operator().line(),
                        "Operand must be a number.");
            }
            yield -number;
        }
        default -> null;
    };
}

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left());
        Object right = evaluate(expr.right());

        return switch (expr.operator().type()) {
            case MINUS -> requireNumber(expr.operator(), left)
                    - requireNumber(expr.operator(), right);

            case SLASH -> {
                double divisor = requireNumber(expr.operator(), right);
                if (divisor == 0) {
                    throw new RuntimeError(expr.operator().line(),
                            "Division by zero.");
                }
                yield requireNumber(expr.operator(), left) / divisor;
            }

            case STAR -> requireNumber(expr.operator(), left)
                    * requireNumber(expr.operator(), right);

            case PERCENT -> {
            double divisor = requireNumber(expr.operator(), right);
             if (divisor == 0) {
            throw new RuntimeError(expr.operator().line(),
                "Modulo by zero.");
             }
             yield requireNumber(expr.operator(), left) % divisor;
            }

            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }

                if (left instanceof String || right instanceof String) {
                    yield stringify(left) + stringify(right);
                }

                throw new RuntimeError(expr.operator().line(),
                        "Operands must be two numbers or at least one string.");
            }

            case GREATER -> requireNumber(expr.operator(), left)
                    > requireNumber(expr.operator(), right);

            case GREATER_EQUAL -> requireNumber(expr.operator(), left)
                    >= requireNumber(expr.operator(), right);

            case LESS -> requireNumber(expr.operator(), left)
                    < requireNumber(expr.operator(), right);

            case LESS_EQUAL -> requireNumber(expr.operator(), left)
                    <= requireNumber(expr.operator(), right);

            case BANG_EQUAL -> !isEqual(left, right);

            case EQUAL_EQUAL -> isEqual(left, right);

            default -> null;
        };
    }

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
            this.environment = previous;
        }
    }

    // ---------- control flow (Phase 6) ----------

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

    // ---------- remaining stubs: Phase 7 ----------

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        throw notImplemented(expr.paren().line(), "Function calls");
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        throw notImplemented(stmt.name().line(), "Function declarations");
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        throw notImplemented(stmt.keyword().line(), "Return statements");
    }

    private RuntimeError notImplemented(int line, String feature) {
        return new RuntimeError(line, feature + " are not implemented yet.");
    }

    // ---------- helpers ----------

    private double requireNumber(Token operator, Object value) {
        if (value instanceof Double number) {
            return number;
        }

        throw new RuntimeError(operator.line(),
                "Operands must be numbers.");
    }

    private boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean bool) {
            return bool;
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

        return a.equals(b);
    }

    private String stringify(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof Double number) {
            String text = number.toString();

            if (text.endsWith(".0")) {
                return text.substring(0, text.length() - 2);
            }

            return text;
        }

        return value.toString();
    }
}
