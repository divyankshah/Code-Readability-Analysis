package de.uni_passau.fim.se2.sa.readability.utils;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.expr.*;
public class CyclomaticComplexityVisitor extends VoidVisitorAdapter<Void> {

    private int complexity = 1;

    public int getComplexity() {
        return complexity;
    }
    @Override
    public void visit(IfStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    @Override
    public void visit(ForStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    @Override
    public void visit(ForEachStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    @Override
    public void visit(WhileStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    @Override
    public void visit(DoStmt n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    @Override
    public void visit(SwitchEntry n, Void arg) {
        if (!n.getLabels().isEmpty()) {
            complexity++;
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(CatchClause n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    @Override
    public void visit(ConditionalExpr n, Void arg) {
        complexity++;
        super.visit(n, arg);
    }

    @Override
    public void visit(BinaryExpr n, Void arg) {
        if (n.getOperator() == BinaryExpr.Operator.AND || n.getOperator() == BinaryExpr.Operator.OR) {
            complexity++;
        }
        super.visit(n, arg);
    }
}