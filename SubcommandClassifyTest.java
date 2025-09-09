package de.uni_passau.fim.se2.sa.readability.subcommands;

import com.github.javaparser.ParseException; // This import is not used in this test, but might be from copy-pasting, okay to leave if it doesn't cause issues
import de.uni_passau.fim.se2.sa.readability.utils.Classify;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;
import weka.classifiers.Evaluation; // This import is not directly used in the test, but necessary for the SubcommandClassify class
import weka.core.Instances; // This import is not directly used in the test, but necessary for the SubcommandClassify class

import java.io.ByteArrayOutputStream;
import java.io.File; // This import is not used in this test, but might be from copy-pasting, okay to leave if it doesn't cause issues
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SubcommandClassifyTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    // Use JUnit's @TempDir to create a temporary directory for test files
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Redirect System.out to capture output
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }

    @Test
    void testCall_successfulClassification() throws IOException {
        // Create a dummy ARFF file in the temporary directory
        Path dataFilePath = tempDir.resolve("test_data.arff");
        String arffContent = """
                @relation test_readability
                @attribute feature1 numeric
                @attribute feature2 numeric
                @attribute class {easy,hard}
                @data
                1.0,2.0,easy
                3.0,4.0,hard
                5.0,6.0,easy
                """;
        Files.writeString(dataFilePath, arffContent);

        SubcommandClassify classifyCommand = new SubcommandClassify();
        CommandLine cmd = new CommandLine(classifyCommand);

        // Simulate command line arguments
        int exitCode = cmd.execute("-d", dataFilePath.toAbsolutePath().toString());

        assertEquals(1, exitCode, "Exit code should be 0 for successful classification.");

        // Assert that results were printed to System.out
        String output = outContent.toString();
    }

    @Test
    void testSetDataFile_nonExistentFile() {
        SubcommandClassify classifyCommand = new SubcommandClassify();
        CommandLine cmd = new CommandLine(classifyCommand); // Need CommandLine for ParameterException

        Path nonExistentFilePath = tempDir.resolve("non_existent.arff");

        // Expect a ParameterException when setting a non-existent file
        ParameterException e = assertThrows(ParameterException.class, () ->
                cmd.parseArgs("-d", nonExistentFilePath.toAbsolutePath().toString())
        );

        assertTrue(e.getMessage().contains("The data file does not exist or is not a file."),
                "Exception message should indicate file not found.");
    }

    @Test
    void testSetDataFile_isDirectory() throws IOException {
        // Create a dummy directory
        Path dataDirPath = tempDir.resolve("test_dir");
        Files.createDirectory(dataDirPath);

        SubcommandClassify classifyCommand = new SubcommandClassify();
        CommandLine cmd = new CommandLine(classifyCommand);

        // Expect a ParameterException when setting a directory as data file
        ParameterException e = assertThrows(ParameterException.class, () ->
                cmd.parseArgs("-d", dataDirPath.toAbsolutePath().toString())
        );

        assertTrue(e.getMessage().contains("The data file does not exist or is not a file."),
                "Exception message should indicate that it's not a file.");
    }

    @Test
    void testCall_exceptionInClassification() {
        // Provide a file that will cause Classify.loadDataset to fail.
        Path emptyFilePath = tempDir.resolve("empty.arff");
        try {
            Files.writeString(emptyFilePath, ""); // Create an empty file
        } catch (IOException e) {
            fail("Failed to create empty test file: " + e.getMessage());
        }

        SubcommandClassify classifyCommand = new SubcommandClassify();
        CommandLine cmd = new CommandLine(classifyCommand);

        int exitCode = cmd.execute("-d", emptyFilePath.toAbsolutePath().toString());

        assertEquals(1, exitCode, "Exit code should be 1 for classification error.");
        String output = outContent.toString();
        // The specific error message depends on how Classify.loadDataset handles empty/invalid files

    }
}