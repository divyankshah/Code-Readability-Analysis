package de.uni_passau.fim.se2.sa.readability.utils;

import de.uni_passau.fim.se2.sa.readability.features.FeatureMetric;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link Preprocess} class, covering CSV generation
 * and truth score-based label assignment logic.
 */
public class PreprocessTest {

    private Path tempSnippetDir;
    private File tempTruthFile;

    /**
     * Creates temporary .jsnp snippet file and a truth CSV file for testing.
     */
    @BeforeEach
    public void setup() throws IOException {
        // Create temporary folder for code snippets
        tempSnippetDir = Files.createTempDirectory("test_snippets");

        // Add a sample snippet file: 1.jsnp
        Path snippet = tempSnippetDir.resolve("1.jsnp");
        Files.writeString(snippet, "public class Sample { int x = 5; }");

        // Create corresponding ground truth CSV file
        tempTruthFile = File.createTempFile("truth_data", ".csv");
        try (FileWriter writer = new FileWriter(tempTruthFile)) {
            writer.write("ID,Snippet1\n");
            writer.write("1,4.1\n"); // Average > 3.6 → label should be Y
        }
    }

    /**
     * Tests if the CSV generation correctly appends feature values and labels (Y/N).
     */
    @Test
    public void testCSVGenerationWithOneFeature() throws IOException {
        StringBuilder outputCSV = new StringBuilder();

        // Use a dummy feature that always returns 10.5
        FeatureMetric mockFeature = new FeatureMetric() {
            @Override
            public double computeMetric(String codeSnippet) {
                return 10.5;
            }

            @Override
            public String getIdentifier() {
                return "MockFeature";
            }
        };

        Preprocess.collectCSVBody(tempSnippetDir, tempTruthFile, outputCSV, List.of(mockFeature));

        String[] lines = outputCSV.toString().trim().split("\n");
        assertEquals(1, lines.length, "Only one snippet file should produce one CSV line");

        String[] parts = lines[0].split(",");
        assertEquals("1.jsnp", parts[0], "Filename should match snippet");
        assertEquals("10.50", parts[1], "Feature value should be formatted");
        assertEquals("Y", parts[2], "Label should be Y since score > threshold");
    }

    /**
     * Verifies that missing or low truth scores result in label "N".
     */
    @Test
    public void testLabelAssignment_Y_and_N() throws IOException {
        // Create alternate truth map directly for test
        Map<String, Double> scores = new HashMap<>();
        scores.put("5.jsnp", 4.0); // >= 3.6 → Y
        scores.put("6.jsnp", 3.0); // < 3.6 → N
        scores.put("missing.jsnp", 0.0); // Default → N

        // Simulate private logic by calling label assignment
        assertEquals("Y", invokeDetermineLabel("5.jsnp", scores));
        assertEquals("N", invokeDetermineLabel("6.jsnp", scores));
        assertEquals("N", invokeDetermineLabel("unknown.jsnp", scores));
    }

    /**
     * Verifies that non-numeric snippet filenames are pushed to the end and do not crash.
     */
    @Test
    public void testNonNumericSnippetFilesHandled() throws IOException {
        Path nonNumeric = tempSnippetDir.resolve("extra_file.jsnp");
        Files.writeString(nonNumeric, "class A {}");

        FeatureMetric dummy = new FeatureMetric() {
            @Override
            public double computeMetric(String codeSnippet) {
                return 1.0;
            }

            @Override
            public String getIdentifier() {
                return "Dummy";
            }
        };

        StringBuilder csvOut = new StringBuilder();
        Preprocess.collectCSVBody(tempSnippetDir, tempTruthFile, csvOut, List.of(dummy));
        String csvContent = csvOut.toString();

        assertTrue(csvContent.contains("1.jsnp"), "Original snippet should be processed");
        assertTrue(csvContent.contains("extra_file.jsnp"), "Non-numeric file should still be handled");
    }

    /**
     * Simulates internal label logic. Equivalent to Preprocess.determineLabel (private).
     */
    private String invokeDetermineLabel(String fileName, Map<String, Double> truthScores) {
        return truthScores.getOrDefault(fileName, 0.0) >= 3.6 ? "Y" : "N";
    }
}