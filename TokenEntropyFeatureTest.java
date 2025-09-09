package de.uni_passau.fim.se2.sa.readability.features;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenEntropyFeatureTest {

    private final TokenEntropyFeature feature = new TokenEntropyFeature();

    @Test
    public void testEmptySnippet() {
        String code = "";
        double entropy = feature.computeMetric(code);
        assertEquals(0.0, entropy, 0.0001, "Empty input should yield zero entropy.");
    }

    @Test
    public void testSingleTokenSnippet() {
        String code = "int";
        double entropy = feature.computeMetric(code);
        assertEquals(0.0, entropy, 0.0001, "Single token input should have zero entropy.");
    }

    @Test
    public void testIdenticalTokensSnippet() {
        String code = "int a = 1;\nint a = 1;";
        double entropy = feature.computeMetric(code);
        // All tokens are repeated → low entropy
        assertTrue(entropy < 2.0, "Repeated token input should result in low entropy.");
    }

    @Test
    public void testUniqueTokensSnippet() {
        String code = "void dummy() {\n" +
                "  int x = 1;\n" +
                "  double y = 3.14;\n" +
                "  String str = \"abc\";\n" +
                "  boolean flag = true;\n" +
                "  char c = 'z';\n" +
                "}";

        double entropy = feature.computeMetric(code);
        System.out.println("Entropy: " + entropy);

        assertTrue(entropy > 2.0, "Snippet with diverse tokens should result in higher entropy.");
    }

    @Test
    public void testSnippetWithWhitespaceTokens() {
        String code = "int     x   =  5;";
        double entropy = feature.computeMetric(code);
        // Whitespace tokens are also counted, increasing diversity
        assertTrue(entropy > 0.0, "Entropy should be > 0 even with redundant spaces.");
    }

    @Test
    public void testSyntaxErrorReturnsZero() {
        String code = "int x = ;"; // Syntax error
        double entropy = feature.computeMetric(code);
        assertEquals(0.0, entropy, 0.0001, "Invalid syntax should return entropy 0.0");
    }

    @Test
    public void testEntropyIsAlwaysNonNegative() {
        String code = "int x = 10;";
        double entropy = feature.computeMetric(code);
        assertTrue(entropy >= 0.0, "Entropy should never be negative.");
    }

    @Test
    public void testWhitespaceOnly() {
        String code = "   \n\t  ";
        double entropy = feature.computeMetric(code);
        assertEquals(0.0, entropy, 0.0001, "Only whitespace should result in zero entropy.");
    }
}