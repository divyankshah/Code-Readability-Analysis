package de.uni_passau.fim.se2.sa.readability.utils;

import com.github.javaparser.ast.expr.*; // Imports for various expression types (literals, names)
import com.github.javaparser.ast.visitor.VoidVisitorAdapter; // Base class for AST visitors
import com.github.javaparser.ast.body.Parameter; // Specific AST node for method parameters

import java.util.*; // Imports for Map and HashMap

/**
 * A visitor that traverses a Java Abstract Syntax Tree (AST) to identify and count
 * different types of **operands** as defined for Halstead complexity metrics.
 * It extends {@link VoidVisitorAdapter} to provide specific visit methods for
 * AST nodes that directly represent operands, such as various literals and identifiers.
 * The core logic relies on incrementing a count for each operand encountered.
 */
public class OperandVisitor extends VoidVisitorAdapter<Void> {

    // A map to store the counts of each unique operand encountered during the AST traversal.
    // The key is the String representation of the operand (e.g., "x", "10", "true"),
    // and the value is the total number of times that operand appears in the visited code.
    private final Map<String, Integer> operandsPerMethod;

    /**
     * Constructs a new OperandVisitor and initializes the map to store operand counts.
     * The map is empty initially and will be populated as the visitor traverses the AST.
     */
    public OperandVisitor() {
        operandsPerMethod = new HashMap<>();
    }

    /**
     * Returns the map containing the total count for each unique operand identified
     * during the AST traversal. This map provides the 'n2' (unique operands) by its size
     * and 'N2' (total operands) by summing its values.
     *
     * @return A map where keys are unique operand strings and values are their respective total counts.
     */
    public Map<String, Integer> getOperandsPerMethod() {
        return operandsPerMethod;
    }

    /**
     * Visits a {@link SimpleName} node. A SimpleName typically represents a basic identifier
     * such as a variable name, a part of a method name, or a type name when used in various
     * expressions or declarations. According to Halstead's definition, names consisting of
     * a single identifier are considered operands.
     * The identifier's string value is extracted and its count is incremented.
     *
     * @param n   The SimpleName node being visited.
     * @param arg An optional argument passed during traversal (not used in this visitor).
     */
    @Override
    public void visit(SimpleName n, Void arg) {
        // Crucially, call super.visit first to ensure deeper traversal. This allows the visitor
        // to fully explore all children of the SimpleName's parent node, ensuring all embedded
        // operands are eventually caught by relevant visit methods.
        super.visit(n, arg);
        String name = n.getIdentifier(); // Get the actual string value of the identifier.
        // Add or increment the count for this operand in the map.
        operandsPerMethod.put(name, operandsPerMethod.getOrDefault(name, 0) + 1);
    }

    /**
     * Visits a {@link BooleanLiteralExpr} node (e.g., {@code true}, {@code false}).
     * Boolean literals are fundamental data values and are thus classified as operands.
     * The string representation of the boolean value is added to the operand counts.
     *
     * @param n   The BooleanLiteralExpr node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(BooleanLiteralExpr n, Void arg) {
        super.visit(n, arg); // Continue visiting children (though literals typically don't have complex children).
        String value = String.valueOf(n.getValue()); // Convert the boolean value (true/false) to its string form.
        operandsPerMethod.put(value, operandsPerMethod.getOrDefault(value, 0) + 1); // Increment count for "true" or "false".
    }

    /**
     * Visits a {@link CharLiteralExpr} node (e.g., {@code 'a'}, {@code '\n'}).
     * Character literals, representing single characters, are considered operands.
     * The raw string value of the character literal is added to the operand counts.
     *
     * @param n   The CharLiteralExpr node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(CharLiteralExpr n, Void arg) {
        super.visit(n, arg); // Continue traversal.
        String value = n.getValue(); // Get the raw string value of the character literal (e.g., "a", "\n").
        operandsPerMethod.put(value, operandsPerMethod.getOrDefault(value, 0) + 1); // Increment count.
    }

    /**
     * Visits a {@link DoubleLiteralExpr} node (e.g., {@code 3.14}, {@code 1.0e-5}).
     * Double-precision floating-point literals are quantitative values and are operands.
     * The exact string representation of the double value is used for counting.
     *
     * @param n   The DoubleLiteralExpr node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(DoubleLiteralExpr n, Void arg) {
        super.visit(n, arg); // Continue traversal.
        String value = n.getValue(); // Get the raw string value of the double literal (e.g., "3.14").
        operandsPerMethod.put(value, operandsPerMethod.getOrDefault(value, 0) + 1); // Increment count.
    }

    /**
     * Visits an {@link IntegerLiteralExpr} node (e.g., {@code 10}, {@code 0xFF}).
     * Integer literals are fundamental numerical values and are counted as operands.
     * The string representation of the integer value is used for counting.
     *
     * @param n   The IntegerLiteralExpr node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(IntegerLiteralExpr n, Void arg) {
        super.visit(n, arg); // Continue traversal.
        String value = n.getValue(); // Get the raw string value of the integer literal (e.g., "10", "0xFF").
        operandsPerMethod.put(value, operandsPerMethod.getOrDefault(value, 0) + 1); // Increment count.
    }

    /**
     * Visits a {@link LongLiteralExpr} node (e.g., {@code 100L}, {@code 0xFFFFFFFFFFFFFFFFL}).
     * Long integer literals are counted as operands, similar to regular integers but with
     * their specific 'L' suffix often preserved in the string value.
     *
     * @param n   The LongLiteralExpr node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(LongLiteralExpr n, Void arg) {
        super.visit(n, arg); // Continue traversal.
        String value = n.getValue(); // Get the raw string value of the long literal (e.g., "100L").
        operandsPerMethod.put(value, operandsPerMethod.getOrDefault(value, 0) + 1); // Increment count.
    }

    /**
     * Visits a {@link StringLiteralExpr} node (e.g., {@code "hello"}, {@code "World"}).
     * String literals are considered operands. The actual string value (without enclosing quotes)
     * is used for counting.
     * Special handling: If the string's content is "null" (case-insensitive), it is
     * treated as the same operand as a {@link NullLiteralExpr} for consistency.
     *
     * @param n   The StringLiteralExpr node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(StringLiteralExpr n, Void arg) {
        super.visit(n, arg); // Continue traversal.
        String value = n.getValue(); // Get the raw string value (without surrounding quotes).
        if ("null".equalsIgnoreCase(value)) { // Check if the string's content is "null" (case-insensitive).
            operandsPerMethod.put("null", operandsPerMethod.getOrDefault("null", 0) + 1); // Treat as the 'null' operand.
        } else {
            operandsPerMethod.put(value, operandsPerMethod.getOrDefault(value, 0) + 1); // Otherwise, add the string's actual value.
        }
    }

    /**
     * Visits a {@link NullLiteralExpr} node (representing the {@code null} keyword).
     * The null literal is a specific value and is consistently counted as an operand.
     * It is mapped to the string "null" for unified counting with string literals containing "null".
     *
     * @param n   The NullLiteralExpr node being visited.
     * @param arg An optional argument passed during traversal.
     */
    @Override
    public void visit(NullLiteralExpr n, Void arg) {
        super.visit(n, arg); // Continue traversal.
        String value = "null"; // Use a consistent string "null" for this literal.
        operandsPerMethod.put(value, operandsPerMethod.getOrDefault(value, 0) + 1); // Increment count.
    }

    /**
     * Visits a {@link Parameter} node (e.g., {@code int x} in a method signature).
     * The name of a method parameter is a single identifier and functions as an operand
     * within the context of the method's definition and usage.
     *
     * @param n   The Parameter node being visited.
     * @param arg An optional argument passed during traversal.
     */
/*    @Override
    public void visit(Parameter n, Void arg) {
        super.visit(n, arg); // Continue traversal (e.g., to visit annotations on the parameter, though not counted as operands).
        String name = n.getNameAsString(); // Get the name of the parameter as a string.
        operandsPerMethod.put(name, operandsPerMethod.getOrDefault(name, 0) + 1); // Increment count.
    }*/
}