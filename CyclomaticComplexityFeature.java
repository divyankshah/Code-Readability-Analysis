package de.uni_passau.fim.se2.sa.readability.features;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.body.BodyDeclaration;
import de.uni_passau.fim.se2.sa.readability.utils.CyclomaticComplexityVisitor;
import de.uni_passau.fim.se2.sa.readability.utils.Parser;

/**
 * Computes the Cyclomatic Complexity for a given Java code snippet.
 * Cyclomatic Complexity is a quantitative measure of the number of
 * decision paths in a code block. A higher number implies more complex logic.
 * It starts at 1 and increases with each:
 * - Conditional or loop construct (if, for, while, do-while)
 * - Case in switch statements
 * - Catch block
 * - Logical connectors (&&, ||)
 * - Ternary operator (?:)
 */
public class CyclomaticComplexityFeature extends FeatureMetric {

    /**
     * Calculates the cyclomatic complexity based on the code snippet provided.
     * If the snippet cannot be parsed, or is null, a default value of 1.0 is returned.
     *
     * @param codeSnippet Java method body or code fragment as a string
     * @return Cyclomatic complexity score as a double
     */
    @Override
    public double computeMetric(String codeSnippet) {
        // Return base complexity if input is null
        if (codeSnippet == null || codeSnippet.trim().isEmpty()) {
            return 1.0;
        }

        try {
            // Parse the snippet into a JavaParser BodyDeclaration
            BodyDeclaration<?> ast = Parser.parseJavaSnippet(codeSnippet);

            // Initialize the visitor that tracks decision points
            CyclomaticComplexityVisitor complexityVisitor = new CyclomaticComplexityVisitor();
            ast.accept(complexityVisitor, null);

            // Return the computed complexity
            return complexityVisitor.getComplexity();
        } catch (ParseException e) {
            // In case of invalid Java code, fallback to default complexity
            return 1.0;
        }
    }

    /**
     * Identifier used when exporting this metric to output (e.g., CSV).
     *
     * @return String label "CyclomaticComplexity"
     */
    @Override
    public String getIdentifier() {
        return "CyclomaticComplexity";
    }
}