package de.uni_passau.fim.se2.sa.readability.features;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberLinesFeatureTest {

    private final NumberLinesFeature feature = new NumberLinesFeature();

    @Test
    public void testEmptySnippet() {
        String code = "";
        double expectedLines = 0;
        assertEquals(expectedLines, feature.computeMetric(code));
    }

    @Test
    public void testSingleLineSnippet() {
        String code = "int a = 10;";
        double expectedLines = 1;
        assertEquals(expectedLines, feature.computeMetric(code));
    }

    @Test
    public void testMultipleLinesSnippet() {
        String code = "int a = 10;\nint b = 20;\nreturn a + b;";
        double expectedLines = 3;
        assertEquals(expectedLines, feature.computeMetric(code));
    }

    @Test
    public void testSnippetWithEmptyLines() {
        String code = "int a = 10;\n\nint b = 20;\n\nreturn a + b;";
        double expectedLines = 5;
        assertEquals(expectedLines, feature.computeMetric(code));
    }

    @Test
    public void testSnippetWithComments() {
        String code = "// comment line\nint a = 10; // inline comment\n/* multi-line\n comment */\nreturn a;";
        double expectedLines = 5; // corrected
        assertEquals(expectedLines, feature.computeMetric(code));
    }

    @Test
    public void testSnippetWithTrailingNewline() {
        String code = "int a = 10;\n";
        double expectedLines = 1;
        assertEquals(expectedLines, feature.computeMetric(code));
    }

    @Test
    public void testWhitespaceOnlyLines() {
        String code = "   \n\t\nint a = 10;";
        double expectedLines = 3;
        assertEquals(expectedLines, feature.computeMetric(code));
    }

    @Test
    public void testWindowsStyleLineEndings() {
        String code = "int a = 10;\r\nint b = 20;\r\nreturn a + b;";
        double expectedLines = 3;
        assertEquals(expectedLines, feature.computeMetric(code));
    }
}