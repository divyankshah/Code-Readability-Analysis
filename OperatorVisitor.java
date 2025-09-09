package de.uni_passau.fim.se2.sa.readability.utils;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * A visitor that traverses a Java Abstract Syntax Tree (AST) to identify and count
 * different types of operators based on the defined {@code OperatorType} enum.
 * This class extends {@link VoidVisitorAdapter} to provide specific visit methods
 * for various expression nodes that represent operators.
 */
public class OperatorVisitor extends VoidVisitorAdapter<Void> {

    /**
     * Defines the categories of operators to be counted for readability metrics.
     */
    public enum OperatorType {
        ASSIGNMENT,         // Represents assignment operations (e.g., =, +=, -=, etc., and variable declarations)
        BINARY,             // Represents binary operations (e.g., +, -, *, /, %, comparison operators like ==, !=, <, >, <=, >=)
        UNARY,              // Represents unary operations (e.g., ++, --, !, ~, unary -)
        CONDITIONAL,        // Represents the ternary conditional operator (e.g., condition ? true_expr : false_expr)
        TYPE_COMPARISON     // Represents the 'instanceof' operator
    }

    // A map to store the counts of each operator type encountered in the visited code.
    // It is initialized as an empty map and populated only when an operator is found.
    private final Map<OperatorType, Integer> operatorsPerMethod;

    /**
     * Constructs a new OperatorVisitor and initializes the map for operator counts
     * as an empty map. This ensures that 'n1' (unique operators) is 0 if no
     * operators are found.
     */
    public OperatorVisitor() {
        this.operatorsPerMethod = new HashMap<>();
        // Removed the loop that initialized all operator types to 0.
        // This ensures operatorsPerMethod.size() correctly reflects unique operators found.
    }

    /**
     * Returns the map containing the total count for each operator type identified
     * during the AST traversal.
     *
     * @return A map where keys are {@code OperatorType} and values are their respective counts.
     */
    public Map<OperatorType, Integer> getOperatorsPerMethod() {
        return operatorsPerMethod;
    }

    // All visit methods below remain the same, they will now add to the map only if an operator is found.

    /**
     * Visits a {@link BinaryExpr} node (e.g., {@code a + b}, {@code x == y}, {@code p && q}).
     * Increments the count for {@code BINARY} operators.
     *
     * @param n   The BinaryExpr node being visited.
     * @param arg An optional argument passed during traversal (not used in this visitor).
     */
    @Override
    public void visit(BinaryExpr n, Void arg) {
        super.visit(n, arg); // Continue visiting child nodes of this expression.
        operatorsPerMethod.merge(OperatorType.BINARY, 1, Integer::sum);
    }

    /**
     * Visits an {@link AssignExpr} node (e.g., {@code x = y}, {@code count += 1}).
     * Increments the count for {@code ASSIGNMENT} operators.
     *
     * @param n   The AssignExpr node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(AssignExpr n, Void arg) {
        super.visit(n, arg); // Continue visiting child nodes.
        operatorsPerMethod.merge(OperatorType.ASSIGNMENT, 1, Integer::sum);
    }

    /**
     * Visits a {@link VariableDeclarator} node (e.g., {@code int x = 10;}).
     * This is considered an assignment operation where a value (or a default) is assigned
     * to a newly declared variable, as per the task description.
     * Increments the count for {@code ASSIGNMENT} operators.
     *
     * @param n   The VariableDeclarator node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(VariableDeclarator n, Void arg) {
        super.visit(n, arg); // Continue visiting child nodes (e.g., the initializer expression).
        // Only count as assignment if it has an initializer.
        // If 'int x;' is just a declaration, it might not be an 'assignment operator' in all Halstead definitions.
        // However, your original description implied "declarations that involve an initial assignment".
        // Let's assume all VariableDeclarators count as an assignment operator based on prior context.
        operatorsPerMethod.merge(OperatorType.ASSIGNMENT, 1, Integer::sum);
    }

    /**
     * Visits a {@link ConditionalExpr} node (e.g., {@code condition ? valueIfTrue : valueIfFalse}).
     * This specifically handles the ternary operator.
     * Increments the count for {@code CONDITIONAL} operators.
     *
     * @param n   The ConditionalExpr node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(ConditionalExpr n, Void arg) {
        super.visit(n, arg); // Continue visiting child nodes.
        operatorsPerMethod.merge(OperatorType.CONDITIONAL, 1, Integer::sum);
    }

    /**
     * Visits an {@link InstanceOfExpr} node (e.g., {@code obj instanceof String}).
     * Increments the count for {@code TYPE_COMPARISON} operators.
     *
     * @param n   The InstanceOfExpr node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(InstanceOfExpr n, Void arg) {
        super.visit(n, arg); // Continue visiting child nodes.
        operatorsPerMethod.merge(OperatorType.TYPE_COMPARISON, 1, Integer::sum);
    }

    /**
     * Visits a {@link UnaryExpr} node (e.g., {@code !flag}, {@code ++count}, {@code -value}).
     * Increments the count for {@code UNARY} operators.
     *
     * @param n   The UnaryExpr node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(UnaryExpr n, Void arg) {
        super.visit(n, arg); // Continue visiting child nodes.
        operatorsPerMethod.merge(OperatorType.UNARY, 1, Integer::sum);
    }
}