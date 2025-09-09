package de.uni_passau.fim.se2.sa.readability.features;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class CyclomaticComplexityFeatureTest {

    private CyclomaticComplexityFeature feature;

    @BeforeEach
    void setUp() {
        feature = new CyclomaticComplexityFeature();
    }

    @Test
    void testEmptySnippet() {
        String code = "class A {}";
        double complexity = feature.computeMetric(code);
        assertEquals(1.0, complexity, "Empty class should have complexity 1");
    }

    @Test
    void testSimpleMethod() {
        String code = "class A { void f() { int x = 0; } }";
        double complexity = feature.computeMetric(code);
        assertEquals(1.0, complexity, "Simple method should have complexity 1");
    }

    @Test
    void testIfElse() {
        String code = "class A { void f() { if (true) {} else {} } }";
        double complexity = feature.computeMetric(code);
        assertEquals(2.0, complexity, "if-else adds one decision point");
    }

    @Test
    void testMultipleBranches() {
        String code = "class A { void f() { if (true) {} for(;;) {} while(true) {} } }";
        double complexity = feature.computeMetric(code);
        assertEquals(4.0, complexity, "Each control structure adds a decision point");
    }

    @Test
    void testNestedBranches() {
        String code = "class A { void f() { if (true) { for(int i=0;i<10;i++) { while(true) {} } } } }";
        double complexity = feature.computeMetric(code);
        assertEquals(4.0, complexity, "Nested structures add their own complexity");
    }

    @Test
    void testLogicalOperators() {
        String code = "class A { void f() { if (a && b || c) {} } }";
        double complexity = feature.computeMetric(code);
        assertEquals(4.0, complexity, "Each && or || adds a decision point");
    }

}
