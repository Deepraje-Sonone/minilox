package com.minilox.ast;

import com.minilox.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    public String print(Stmt stmt) {
        return stmt.accept(this);
    }

    public String printProgram(List<Stmt> statements) {
        return statements.stream()
                .map(stmt -> stmt.accept(this))
                .collect(Collectors.joining("\n"));
    }

    // ---------- expressions ----------

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return "(= " + expr.name().lexeme() + " " + expr.value().accept(this) + ")";
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator().lexeme(), expr.left(), expr.right());
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        List<Expr> all = new ArrayList<>();
        all.add(expr.callee());
        all.addAll(expr.arguments());
        return parenthesize("call", all.toArray(new Expr[0]));
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression());
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        Object value = expr.value();
        if (value == null) return "null";
        if (value instanceof String s) return "\"" + s + "\"";
        if (value instanceof Double d && d % 1 == 0) return String.valueOf(d.longValue());
        return value.toString();
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator().lexeme(), expr.left(), expr.right());
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator().lexeme(), expr.right());
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name().lexeme();
    }

    // ---------- statements ----------

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        StringBuilder sb = new StringBuilder("(block");
        for (Stmt s : stmt.statements()) {
            sb.append(" ").append(s.accept(this));
        }
        return sb.append(")").toString();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return parenthesize("expr", stmt.expression());
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        String params = stmt.params().stream()
                .map(Token::lexeme)
                .collect(Collectors.joining(" "));
        StringBuilder sb = new StringBuilder("(fn " + stmt.name().lexeme() + " (" + params + ")");
        for (Stmt s : stmt.body()) {
            sb.append(" ").append(s.accept(this));
        }
        return sb.append(")").toString();
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        String result = "(if " + stmt.condition().accept(this)
                + " " + stmt.thenBranch().accept(this);
        if (stmt.elseBranch() != null) {
            result += " " + stmt.elseBranch().accept(this);
        }
        return result + ")";
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("print", stmt.expression());
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value() == null) return "(return)";
        return parenthesize("return", stmt.value());
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        String init = stmt.initializer() == null ? "" : " " + stmt.initializer().accept(this);
        return "(let " + stmt.name().lexeme() + init + ")";
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return "(while " + stmt.condition().accept(this) + " " + stmt.body().accept(this) + ")";
    }

    // ---------- helper ----------

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(name);
        for (Expr e : exprs) {
            sb.append(" ").append(e.accept(this));
        }
        sb.append(")");
        return sb.toString();
    }
}