package de.uni_passau.fim.se2.sa.readability.subcommands;

import com.google.common.base.Charsets;
import com.google.common.io.Files; // Used by the main class for file extension check and writing
import de.uni_passau.fim.se2.sa.readability.features.FeatureMetric;
import de.uni_passau.fim.se2.sa.readability.features.HalsteadVolumeFeature;
import de.uni_passau.fim.se2.sa.readability.features.NumberLinesFeature;
// We cannot mock Preprocess.collectCSVBody without Mockito, so we must rely on its actual behavior.
// This means we'll need a proper test setup for Preprocess.
// import de.uni_passau.fim.se2.sa.readability.utils.Preprocess; // Not needed if we don't mock it

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubcommandPreprocessTest {

    private SubcommandPreprocess preprocessCommand;
    private CommandLine commandLine;

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @TempDir
    Path tempDir; // JUnit 5 automatically creates and cleans up a temporary directory

    private Path sourceDirPath;
    private Path truthFilePath;
    private Path targetFilePath;
    private Path nonCsvTargetFilePath;
    private Path nonExistentSourcePath;
    private Path nonExistentTruthPath;
    private Path nonExistentTargetDirPath;


    @BeforeEach
    void setUp() throws IOException {
        // Redirect System.out and System.err to capture output for assertions
        System.setErr(new PrintStream(errContent));
        System.setOut(new PrintStream(outContent));

        preprocessCommand = new SubcommandPreprocess();
        commandLine = new CommandLine(preprocessCommand);

        // Create temporary files and directories for tests
        sourceDirPath = tempDir.resolve("source");
        java.nio.file.Files.createDirectory(sourceDirPath);
        java.nio.file.Files.writeString(sourceDirPath.resolve("snippet1.jsnp"), "public class MyClass { /* code */ }");
        java.nio.file.Files.writeString(sourceDirPath.resolve("snippet2.jsnp"), "public void anotherMethod() { /* code */ }");


        truthFilePath = tempDir.resolve("ground_truth.csv");
        // Create a dummy ground truth file with content that matches the snippets.
        // The numbers don't matter much for this test, as long as Preprocess can parse them.
        java.nio.file.Files.writeString(truthFilePath, "File,Rating\nsnippet1.jsnp,4.5\nsnippet2.jsnp,2.1\n");

        targetFilePath = tempDir.resolve("output.csv");
        nonCsvTargetFilePath = tempDir.resolve("output.txt");

        // Paths for error cases
        nonExistentSourcePath = tempDir.resolve("non_existent_source");
        nonExistentTruthPath = tempDir.resolve("non_existent_truth.csv");
        nonExistentTargetDirPath = tempDir.resolve("non_existent_target_dir").resolve("file.csv");
    }

    @AfterEach
    void tearDown() {
        // Restore original System.out and System.err
        System.setErr(originalErr);
        System.setOut(originalOut);
    }

    // --- Test Option Setters ---

    @Test
    void testSetSourceDirectory_valid() {
        preprocessCommand.setSourceDirectory(sourceDirPath.toFile());

    }

    @Test
    void testSetSourceDirectory_nonExistent() {
        ParameterException e = assertThrows(ParameterException.class, () ->
                preprocessCommand.setSourceDirectory(nonExistentSourcePath.toFile()));
        assertTrue(e.getMessage().contains("Source directory does not exist."), "Should throw for non-existent source.");
    }

    @Test
    void testSetSourceDirectory_notDirectory() throws IOException {
        Path tempFile = tempDir.resolve("a_file.txt");
        java.nio.file.Files.createFile(tempFile);
        ParameterException e = assertThrows(ParameterException.class, () ->
                preprocessCommand.setSourceDirectory(tempFile.toFile()));
        assertTrue(e.getMessage().contains("Source directory does not exist."), "Should throw for a file passed as source.");
    }

    @Test
    void testSetTruth_valid() {
        preprocessCommand.setTruth(truthFilePath.toFile());

    }

    @Test
    void testSetTruth_nonExistent() {
        ParameterException e = assertThrows(ParameterException.class, () ->
                preprocessCommand.setTruth(nonExistentTruthPath.toFile()));
        assertTrue(e.getMessage().contains("Truth file does not exist."), "Should throw for non-existent truth file.");
    }

    @Test
    void testSetTruth_isDirectory() throws IOException {
        Path tempDirForTruth = tempDir.resolve("a_dir_for_truth");
        java.nio.file.Files.createDirectory(tempDirForTruth);
        ParameterException e = assertThrows(ParameterException.class, () ->
                preprocessCommand.setTruth(tempDirForTruth.toFile()));
        assertTrue(e.getMessage().contains("Truth file does not exist."), "Should throw for a directory passed as truth file.");
    }

    @Test
    void testSetTargetFile_valid() throws IOException {
        // Ensure parent directory exists for target file
        java.nio.file.Files.createDirectories(targetFilePath.getParent());
        preprocessCommand.setTargetFile(targetFilePath.toFile());

    }

    @Test
    void testSetTargetFile_nonCsvSuffix() {
        ParameterException e = assertThrows(ParameterException.class, () ->
                preprocessCommand.setTargetFile(nonCsvTargetFilePath.toFile()));
        assertTrue(e.getMessage().contains("Target file must end with a .csv suffix"), "Should throw for non-.csv suffix.");
    }

    @Test
    void testSetTargetFile_targetDirectoryDoesNotExist() {
        // This path's parent doesn't exist
        ParameterException e = assertThrows(ParameterException.class, () ->
                preprocessCommand.setTargetFile(nonExistentTargetDirPath.toFile()));
        assertTrue(e.getMessage().contains("Target directory does not exist."), "Should throw if target's parent directory doesn't exist.");
    }

    // --- Test FeatureConverter ---

    @Test
    void testFeatureConverter_validMetrics() {
        FeatureConverter converter = new FeatureConverter();
        assertTrue(converter.convert("lines") instanceof NumberLinesFeature);
        assertTrue(converter.convert("h_volume") instanceof HalsteadVolumeFeature);
        // Assuming TokenEntropyFeature and CyclomaticComplexityFeature exist and extend FeatureMetric
        assertTrue(converter.convert("token_entropy") instanceof FeatureMetric);
        assertTrue(converter.convert("cyclomatic_complexity") instanceof FeatureMetric);
    }

    @Test
    void testFeatureConverter_invalidMetric() {
        FeatureConverter converter = new FeatureConverter();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                converter.convert("invalid_metric"));
        assertTrue(e.getMessage().contains("The metric 'invalid_metric' is not a valid option."), "Should throw for invalid metric name.");
    }

    // --- Test Call Method (without Mockito, relies on actual Preprocess behavior) ---

    @Test
    void testCall_successfulExecution() throws IOException {
        // Set up command line arguments
        String[] args = {
                "-s", sourceDirPath.toAbsolutePath().toString(),
                "-g", truthFilePath.toAbsolutePath().toString(),
                "-t", targetFilePath.toAbsolutePath().toString(),
                "LINES", "H_VOLUME"
        };

        // This executes the command as it would from the command line
        int exitCode = commandLine.execute(args);

        assertEquals(0, exitCode, "Call should return 0 on success.");

        // Read the generated CSV file and verify its content
        String generatedCsv = Files.toString(targetFilePath.toFile(), Charsets.UTF_8);
        assertTrue(generatedCsv.startsWith("File,NumberLines,HalsteadVolume,Truth"), "CSV header should be correct.");
        // We cannot easily assert the *exact* values for features without mocking
        // the FeatureMetrics or Preprocess, but we can check format.
        assertTrue(generatedCsv.contains("snippet1.jsnp,"), "Generated CSV should contain data for snippet1.");
        assertTrue(generatedCsv.contains("snippet2.jsnp,"), "Generated CSV should contain data for snippet2.");
        assertTrue(generatedCsv.contains(",4.5"), "Generated CSV should contain truth value for snippet1.");
        assertTrue(generatedCsv.contains(",2.1"), "Generated CSV should contain truth value for snippet2.");


        // Verify output to console (which should be the full CSV content)
        String consoleOutput = outContent.toString();
        assertTrue(consoleOutput.startsWith("File,NumberLines,HalsteadVolume,Truth"), "Console should print the CSV header.");
        assertTrue(consoleOutput.contains("snippet1.jsnp"), "Console output should contain snippet data.");
        assertTrue(consoleOutput.contains("snippet2.jsnp"), "Console output should contain snippet data.");
    }

    @Test
    void testCall_IOExceptionDuringPreprocessing() throws IOException {
        // Set up command line arguments pointing to a source directory with unparseable content
        Path badSourceDir = tempDir.resolve("bad_source");
        java.nio.file.Files.createDirectory(badSourceDir);
        // Create a malformed .jsnp file that should cause a ParseException in FeatureMetric.computeMetric
        java.nio.file.Files.writeString(badSourceDir.resolve("malformed.jsnp"), "public class Bad { int x = ; }");


        String[] args = {
                "-s", badSourceDir.toAbsolutePath().toString(), // Use the bad source directory
                "-g", truthFilePath.toAbsolutePath().toString(),
                "-t", targetFilePath.toAbsolutePath().toString(),
                "LINES", "H_VOLUME" // These metrics might throw ParseException for bad syntax
        };

        // Execute the command
        int exitCode = commandLine.execute(args);

        assertEquals(1, exitCode, "Call should return 1 on IOException from parsing or feature computation.");
        String consoleOutput = outContent.toString();
        assertTrue(consoleOutput.contains("Encountered error while parsing input files:"),
                "Console should print an error message about parsing input files.");
        // The specific error message after "parsing input files:" will depend on the exact ParseException
        // or other IOExceptions thrown by FeatureMetric's computeMetric or Preprocess.
        assertFalse(targetFilePath.toFile().exists(), "Target file should not be created if error occurs before writing.");
    }

    @Test
    void testCall_IOExceptionDuringWriting() throws IOException {
        // Setup command line arguments
        String[] args = {
                "-s", sourceDirPath.toAbsolutePath().toString(),
                "-g", truthFilePath.toAbsolutePath().toString(),
                "-t", targetFilePath.toAbsolutePath().toString(),
                "LINES"
        };

        // Simulate a write error by making the target file path point to a directory
        // This will cause Files.newWriter() to throw an IOException when it tries to write.
        java.nio.file.Files.createDirectory(targetFilePath); // Create a directory instead of a file

        // Execute the command
        int exitCode = commandLine.execute(args);

        // SubcommandPreprocess catches and prints the write error but returns 0.
        assertEquals(0, exitCode, "Call should return 0 despite write error, as it's caught internally.");
        String consoleOutput = outContent.toString(); // System.out because it's caught and printed there
        assertTrue(consoleOutput.contains("Is a directory") || consoleOutput.contains("Error writing to file"),
                "Should print an IOException message for writing to a directory.");
    }
}