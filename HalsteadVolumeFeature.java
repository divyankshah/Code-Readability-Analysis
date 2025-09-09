package de.uni_passau.fim.se2.sa.readability.features;

import com.github.javaparser.ParseException; // Import for general Java parsing exceptions
import com.github.javaparser.ParseProblemException; // Import for specific parsing issues that occur during parsing
import com.github.javaparser.ast.body.BodyDeclaration; // Represents a method, constructor, or field declaration in the AST
import de.uni_passau.fim.se2.sa.readability.utils.OperatorVisitor; // Custom visitor to count operators
import de.uni_passau.fim.se2.sa.readability.utils.OperandVisitor; // Custom visitor to count operands

import java.util.Map; // Used to store the counts of operators and operands

import static de.uni_passau.fim.se2.sa.readability.utils.Parser.parseJavaSnippet; // Static import for the parsing utility method

/**
 * Implements the calculation of the **Halstead Volume** metric for a given Java code snippet.
 * Halstead Volume (V) is a software metric derived from the counts of operators and operands
 * within a program. It's defined by the formula: $V = N \times \log_2(n)$, where $N$ is the
 * program length and $n$ is the program vocabulary. This class extends {@link FeatureMetric},
 * indicating its role as a specific readability feature within a larger analysis framework.
 */
public class HalsteadVolumeFeature extends FeatureMetric {

    /**
     * Computes the Halstead Volume for the provided Java code snippet.
     * This process involves several key steps: parsing the code into an Abstract Syntax Tree (AST),
     * using custom visitors to identify and count both total and unique operators and operands,
     * and finally, applying the Halstead formulas to derive the volume.
     *
     * @param codeSnippet The Java method or code segment provided as a String.
     * @return The calculated Halstead Volume as a {@code double}. Returns 0.0 if a parsing error occurs
     * or if the program vocabulary ($n$) is zero, which would lead to an undefined logarithm.
     */
    @Override
    public double computeMetric(String codeSnippet) {
        try {
            // Step 1: Parse the incoming code snippet into an Abstract Syntax Tree (AST) node.
            // The 'parseJavaSnippet' method, expected to be in the 'Parser' utility class,
            // converts the raw code string into a 'BodyDeclaration' object. This object
            // represents a high-level structure like a method or class, allowing for structured
            // traversal and analysis of its components.
            BodyDeclaration<?> bodyDeclaration = parseJavaSnippet(codeSnippet);

            // Step 2: Initialize dedicated visitors for counting operators and operands.
            // 'OperatorVisitor' is responsible for identifying and tallying various types of operators,
            // while 'OperandVisitor' focuses on counting identifiers and literal values.
            OperatorVisitor operatorVisitor = new OperatorVisitor();
            OperandVisitor operandVisitor = new OperandVisitor();

            // Step 3: Initiate the AST traversal for both visitors.
            // The 'accept' method on the 'bodyDeclaration' triggers the visitors to walk
            // through the AST. Each visitor applies its specific logic to the nodes it's
            // designed to handle, collecting the necessary counts (operators and operands)
            // as it traverses. The 'null' argument is an optional parameter that these
            // particular visitors do not utilize.
            bodyDeclaration.accept(operatorVisitor, null);
            bodyDeclaration.accept(operandVisitor, null);

            // Step 4: Retrieve the collected counts from the visitors.
            // Ensure you're getting operator counts from the operatorVisitor
            // and operand counts from the operandVisitor.
            Map<OperatorVisitor.OperatorType, Integer> operatorsPerMethod = operatorVisitor.getOperatorsPerMethod();
            Map<String, Integer> operandsPerMethod = operandVisitor.getOperandsPerMethod();

            // Step 5: Calculate N1 (Total Operators) and N2 (Total Operands).
            // N1 represents the sum of all occurrences of all operator types found in the code.
            // N2 represents the sum of all occurrences of all individual operands found in the code.
            int N1 = operatorsPerMethod.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
            int N2 = operandsPerMethod.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            // Step 6: Calculate n1 (Unique Operators) and n2 (Unique Operands).
            // n1 is the count of distinct operator types identified.
            // This is simply the size of the 'operatorsPerMethod' map.
            // n2 is the count of distinct operand identifiers or literals identified.
            // This is the size of the 'operandsPerMethod' map.
            int n1 = operatorsPerMethod.size();
            int n2 = operandsPerMethod.size();

            // --- IMPORTANT: Debugging Output for 15.jsnp ---
            // This section is crucial for diagnosing the remaining discrepancy.
            // It prints the exact Halstead variables calculated by your visitors.
            System.out.println("\n--- Halstead Volume Debugging Output for snippet ---");
            System.out.println("Expected Halstead variables from test for 15.jsnp:");
            System.out.println("  num operands (N2) = 82");
            System.out.println("  vocabulary operands (n2) = 32");
            System.out.println("  num operators (N1) = 16");
            System.out.println("  vocabulary operators (n1) = 3");
            System.out.println("-------------------------------------------------");
            System.out.println("Your Calculated Halstead Variables:");
            System.out.println("  N1 (Total Operators): " + N1);
            System.out.println("  n1 (Unique Operators): " + n1);
            System.out.println("  N2 (Total Operands): " + N2);
            System.out.println("  n2 (Unique Operands): " + n2);
            System.out.println("-------------------------------------------------\n");

            // Step 7: Calculate Program Length (N) and Program Vocabulary (n).
            // Program Length (N) is the combined total count of all operators and all operands.
            // Program Vocabulary (n) is the combined count of all unique operators and all unique operands.
            int N = N1 + N2;
            int n = n1 + n2;

            // Step 8: Handle the edge case where the program vocabulary 'n' is zero.
            // This scenario typically occurs for empty code snippets or those containing no
            // identifiable operators or operands. Since the logarithm of zero is undefined,
            // returning 0.0 prevents a runtime error and provides a meaningful default for
            // uncomputable cases.
            if (n == 0) {
                return 0.0;
            }

            // Step 9: Calculate Halstead Volume (V) using the formula V = N * log2(n).
            // 'Math.log(n)' computes the natural logarithm (ln). To convert this to
            // log base 2, we use the change of base formula: $\log_b(x) = \frac{\log_d(x)}{\log_d(b)}$.
            // In this specific calculation, $d=e$ (natural log) and $b=2$.
            double volume = N * (Math.log(n) / Math.log(2));

            // Print the final calculated volume for debugging
            System.out.println("Calculated Volume: " + volume);
            System.out.println("Expected Volume: 503");


            return volume;

        } catch (ParseProblemException | ParseException e) {
            // Step 10: Implement robust error handling for parsing failures.
            // If the 'codeSnippet' cannot be successfully parsed by JavaParser (e.g., due to syntax errors,
            // incomplete code, or malformed snippets), these exceptions are caught.
            // The full stack trace is printed to the standard error stream for in-depth debugging.
            // Additionally, a concise, user-friendly error message is printed to 'System.err'.
            // Returning 0.0 indicates that the metric could not be computed for this specific snippet.
            System.err.println("Error parsing code snippet for Halstead Volume: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Returns the unique string identifier for this specific feature metric.
     * This identifier is critical for consistent labeling, particularly when
     * generating output data, such as column headers in a CSV file.
     *
     * @return The string identifier "HalsteadVolume".
     */
    @Override
    public String getIdentifier() {
        return "HalsteadVolume";
    }
}