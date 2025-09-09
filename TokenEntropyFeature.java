package de.uni_passau.fim.se2.sa.readability.features;
import com.github.javaparser.JavaToken;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.stmt.BlockStmt;
import de.uni_passau.fim.se2.sa.readability.utils.Parser;

import java.util.HashMap;
import java.util.Map;

public class TokenEntropyFeature extends FeatureMetric {

    /**
     * Computes the entropy metric based on the tokens of the given code snippet.
     * Since we are interested in the readability of code as perceived by a human, tokens also include whitespaces and the like.
     *
     * @return token entropy of the given code snippet.
     */
    @Override
    public double computeMetric(String codeSnippet) {
        TokenRange range;
        try {
            range = Parser.parseJavaSnippet(codeSnippet).getTokenRange().orElse(null);
        } catch (Exception e) {
            return 0.0;
        }

        if (range == null) return 0.0;

        Map<String, Integer> tokenCounts = new HashMap<>();
        int totalTokens = 0;

        for (JavaToken token : range) {
            String text = token.getText();
            tokenCounts.put(text, tokenCounts.getOrDefault(text, 0) + 1);
            totalTokens++;
        }

        if (totalTokens == 0) return 0.0;

        double entropy = 0.0;
        for (int freq : tokenCounts.values()) {
            double p = (double) freq / totalTokens;
            entropy -= p * (Math.log(p) / Math.log(2));
        }

        return entropy;

    }
    @Override
    public String getIdentifier() {
        return "TokenEntropy";
    }
}
