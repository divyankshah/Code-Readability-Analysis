package de.uni_passau.fim.se2.sa.readability.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HalsteadVolumeFeatureTest {
    private HalsteadVolumeFeature feature;

    @BeforeEach
    void setUp() {
        feature = new HalsteadVolumeFeature();
    }

    @Test
    void testEmptySnippet() {
        // A minimal class declaration typically has no operators/operands for Halstead,
        // or a very small, fixed set if class/method declarations themselves count.
        // Assuming your setup results in 0.0 for truly empty or minimal structures.
        String code = "class A {}";
        double volume = feature.computeMetric(code);
        assertEquals(0.0, volume, 0.01, "Empty code should result in zero Halstead Volume");
    }

    @Test
    void testSingleOperatorAndOperand() {
        String snippet = """
        class Dummy {
            void method() {
                int a = 1;
                int b = a + 2;
            }
        }
        """;
        double volume = feature.computeMetric(snippet);
        // Based on the manual calculation above (N1=3, n1=2, N2=7, n2=6 => N=10, n=8 => Volume=30)
        // Adjust this expected value if your visitors yield different counts for this snippet.
        assertEquals(30.0, volume, 0.01, "Specific snippet should yield a predictable Halstead Volume");
    }

    @Test
    void testMultipleStatements() {
        String code = "class A { void f() { int a = 1 + 2; int b = a * 3; } }";
        double volume = feature.computeMetric(code);
        assertTrue(volume > 0, "Volume should reflect number of operators and operands");
        // You could add a specific assertEquals here if you manually calculate expected for this.
    }

    @Test
    void testControlStructures() {
        String code = "class A { void f() { for(int i = 0; i < 10; i++) { if (i % 2 == 0) {} } } }";
        double volume = feature.computeMetric(code);
        assertTrue(volume > 0, "Loop and if statements should contribute to Halstead Volume");
        // Add a specific assertEquals here if you manually calculate expected for this.
    }

    @Test
    void testLogicalConditions() {
        String code = "class A { void f() { if (a && b || c) {} } }";
        double volume = feature.computeMetric(code);
        assertTrue(volume > 0, "Logical operators should be counted as distinct operators");
        // Add a specific assertEquals here if you manually calculate expected for this.
    }
}