package de.uni_passau.fim.se2.sa.readability.utils;

import weka.classifiers.Evaluation;
import weka.core.Instances;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class ClassifyTest {

    private File testCSV;

    /**
     * Sets up a small dummy CSV file compatible with Weka.
     * Includes numeric attributes and a nominal class label ("Y"/"N").
     */
    @BeforeEach
    public void setup() throws IOException {
        testCSV = File.createTempFile("weka_test_data", ".csv");

        // Minimal readable CSV header + 2 examples
        String csvContent = """
    Feature1,Feature2,Feature3,Truth
    1.1,0.1,0.1,Y
    1.2,0.2,0.2,N
    1.3,0.3,0.3,Y
    1.4,0.4,0.4,N
    1.5,0.5,0.5,Y
    1.6,0.6,0.6,N
    1.7,0.7,0.7,Y
    1.8,0.8,0.8,N
    1.9,0.9,0.9,Y
    2.0,1.0,1.0,N
    """;

        Files.writeString(testCSV.toPath(), csvContent);
    }

    /**
     * Tests whether the dataset loads successfully and the class index is set correctly.
     */
    @Test
    public void testDatasetLoading() throws IOException {
        Instances dataset = Classify.loadDataset(testCSV);

        assertNotNull(dataset, "Dataset should not be null");
        assertEquals(4, dataset.numAttributes(), "Dataset should have 4 columns");
        assertEquals(3, dataset.classIndex(), "Class attribute should be last column (index 3)");
        assertEquals("Truth", dataset.classAttribute().name(), "Class attribute should be named 'Truth'");
    }

    /**
     * Tests training and 10-fold cross-validation on a minimal dataset using Logistic Regression.
     */
    @Test
    public void testTrainingAndEvaluation() throws Exception {
        Instances dataset = Classify.loadDataset(testCSV);
        Evaluation eval = Classify.trainAndEvaluate(dataset);

        assertNotNull(eval, "Evaluation object should not be null");
        assertTrue(eval.pctCorrect() >= 0.0 && eval.pctCorrect() <= 100.0, "Accuracy should be a valid percentage");
        assertTrue(eval.weightedFMeasure() >= 0.0, "F1 score should be non-negative");
    }
}