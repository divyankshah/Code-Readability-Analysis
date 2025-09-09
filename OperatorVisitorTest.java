package de.uni_passau.fim.se2.sa.readability.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for verifying correct operator detection using {@link OperatorVisitor}.
 * This test parses Java code and ensures that each type of operator is counted appropriately.
 */
public class OperatorVisitorTest {

    private OperatorVisitor visitor;

    @BeforeEach
    public void setupVisitor() {
        visitor = new OperatorVisitor();
    }

    /**
     * Tests various operator types (assignment, binary, unary, instanceof, etc.)
     * using a sample Java method.
     */
    @Test
    public void testVariousOperatorsDetectedCorrectly() {
        // Code snippet contains diverse operator types
        String sourceCode = """
                public class Sample {
                    void demo() {
                        int a = 5 + 3;           // ASSIGNMENT, BINARY
                        a -= 2;                  // ASSIGNMENT
                        a *= 4;                  // ASSIGNMENT
                        a++;                     // UNARY
                        if (a != 10) {}          // BINARY (== or !=)
                        boolean flag = a instanceof Integer; // ASSIGNMENT, TYPE_COMPARISON
                    }
                }
                """;

        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> parseResult = parser.parse(sourceCode);

        // Make sure the snippet parses successfully
        assertTrue(parseResult.isSuccessful(), "Parsing failed unexpectedly");
        CompilationUnit compilationUnit = parseResult.getResult()
                .orElseThrow(() -> new IllegalStateException("Could not parse code into CompilationUnit"));

        // Traverse the AST with the operator visitor
        compilationUnit.accept(visitor, null);

        // Extract counts of each operator type
        Map<OperatorVisitor.OperatorType, Integer> operatorCounts = visitor.getOperatorsPerMethod();

        // Debug print
        System.out.println("Detected Operator Counts: " + operatorCounts);

        // Validate expected operator counts
        assertEquals(4, (int) operatorCounts.getOrDefault(OperatorVisitor.OperatorType.ASSIGNMENT, 0),
                "Expected 4 assignment operators (3 lines with assignments)");

        assertEquals(2, (int) operatorCounts.getOrDefault(OperatorVisitor.OperatorType.BINARY, 0),
                "Expected 2 binary operations: + and !=");

        assertEquals(1, (int) operatorCounts.getOrDefault(OperatorVisitor.OperatorType.UNARY, 0),
                "Expected 1 unary operation (a++)");

        assertEquals(1, (int) operatorCounts.getOrDefault(OperatorVisitor.OperatorType.TYPE_COMPARISON, 0),
                "Expected 1 'instanceof' operator");

        assertEquals(0, (int) operatorCounts.getOrDefault(OperatorVisitor.OperatorType.CONDITIONAL, 0),
                "Expected 0 ternary operators (?:)");
    }
}