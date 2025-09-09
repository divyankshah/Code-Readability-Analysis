package de.uni_passau.fim.se2.sa.readability.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link OperandVisitor} class to ensure that operands
 * such as variables, literals, and identifiers are correctly counted from Java source code.
 */
public class OperandVisitorTest {

    private OperandVisitor visitor;

    @BeforeEach
    public void initializeVisitor() {
        visitor = new OperandVisitor();
    }

    /**
     * Verifies that the visitor correctly counts a null literal and basic variable names.
     */
    @Test
    public void testNullLiteralAndVariableOperands() {
        String source = "public class Sample { void run() { Object item = null; } }";
        runVisitorOnSource(source, visitor);

        Map<String, Integer> operands = visitor.getOperandsPerMethod();
        assertEquals(1, operands.get("null"), "Expected one 'null' literal");
        assertEquals(1, operands.get("Object"), "Expected one use of 'Object'");
        assertEquals(1, operands.get("item"), "Expected one use of 'item'");
    }

    /**
     * Tests whether method parameters and their usage within a method body are accurately tracked.
     */
    @Test
    public void testMethodParameterUsage() {
        String source = "public class Sample { void compute(int a, String b) { int result = a + b.length(); } }";
        runVisitorOnSource(source, visitor);

        Map<String, Integer> operands = visitor.getOperandsPerMethod();

        assertEquals(2, operands.get("a"), "'a' should be counted in declaration and usage");
        assertEquals(2, operands.get("b"), "'b' should be counted in declaration and in 'b.length()'");
        assertEquals(1, operands.get("result"), "'result' is assigned once");
        assertEquals(1, operands.get("length"), "'length' method used once");
    }

    /**
     * Helper function to parse Java code and run a given visitor on its AST.
     *
     * @param code    the Java code to parse
     * @param visitor the AST visitor to apply
     */
    private void runVisitorOnSource(String code, VoidVisitor<Void> visitor) {
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> result = parser.parse(code);
        result.getResult().ifPresent(cu -> cu.accept(visitor, null));
    }
}