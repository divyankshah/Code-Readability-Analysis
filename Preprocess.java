package de.uni_passau.fim.se2.sa.readability.utils;

import de.uni_passau.fim.se2.sa.readability.features.FeatureMetric;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;

public class Preprocess {

    /**
     * A value of 3.6 splits the Scalabrino Dataset into almost evenly balanced binary classes.
     */
    private static final double TRUTH_THRESHOLD = 3.6;

    /**
     * Traverses through each java snippet in the specified source directory and computes the specified list of feature metrics.
     * Each snippet is then saved together with its extracted feature values and the truth score as one row in the csv, resulting
     * in the scheme [File,NumberLines,TokenEntropy,HalsteadVolume,Truth].
     * <p>
     * The File column value corresponds to the respective file name.
     * All feature values are rounded to two decimal places.
     * The truth value corresponds to a String that is set to the value "Y" if the mean rater score of a given snippet is greater or equal
     * than the TRUTH_THRESHOLD. Otherwise, if the mean score is lower than the TRUTH_THRESHOLD the truth value String is set to "N".
     *
     * @param sourceDir      the directory containing java snippet (.jsnp) files.
     * @param truth          the ground truth csv file containing the human readability ratings of the code snippets.                       `
     * @param csv            the builder for the csv.
     * @param featureMetrics the list of specified features via the cli.
     * @throws IOException if the source directory or the truth file does not exist.
     */

        public static void collectCSVBody(Path sourceDir, File truth, StringBuilder csv, List<FeatureMetric> featureMetrics) throws IOException {
            Map<String, Double> averageScores = parseTruthScores(truth);

            // Find and sort .jsnp files numerically
            List<Path> snippetFiles = Files.walk(sourceDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".jsnp"))
                    .sorted(Comparator.comparingInt(p -> {
                        String name = p.getFileName().toString().replace(".jsnp", "");
                        try {
                            return Integer.parseInt(name);
                        } catch (NumberFormatException e) {
                            return Integer.MAX_VALUE; // Push unparseable names to the end
                        }
                    }))
                    .toList();

            DecimalFormat formatter = new DecimalFormat("0.00");
            formatter.setGroupingUsed(false);

            for (Path snippet : snippetFiles) {
                String fileName = snippet.getFileName().toString();
                try {
                    String code = Files.readString(snippet);

                    // Start the CSV row with the file name
                    StringBuilder row = new StringBuilder(fileName);

                    // Compute and append all feature values
                    for (FeatureMetric metric : featureMetrics) {
                        double value = metric.computeMetric(code);
                        row.append(",").append(formatter.format(value));
                    }

                    // Determine label (Y or N) based on truth score
                    String label = determineLabel(fileName, averageScores);
                    row.append(",").append(label);

                    csv.append(row).append("\n");

                } catch (IOException | RuntimeException e) {
                    System.err.println("Skipping file due to error: " + fileName);
                }
            }
        }

        /**
         * Reads the ground truth file and calculates average score per snippet.
         * @param truthFile CSV file containing rater scores
         * @return Map from filename to average score
         * @throws IOException if the file cannot be read
         */
        private static Map<String, Double> parseTruthScores(File truthFile) throws IOException {
            List<String> lines = Files.readAllLines(truthFile.toPath());
            Map<String, List<Double>> scoreMap = new HashMap<>();
            Map<String, Double> avgScores = new HashMap<>();

            if (lines.isEmpty()) return avgScores;

            // Parse header to map column indices to snippet names
            String[] header = lines.get(0).split(",");
            Map<Integer, String> indexToSnippet = new HashMap<>();

            for (int i = 0; i < header.length; i++) {
                String column = header[i].trim();
                if (column.startsWith("Snippet")) {
                    String num = column.substring("Snippet".length());
                    indexToSnippet.put(i, num + ".jsnp");
                }
            }

            // Collect all scores
            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i).split(",");
                for (Map.Entry<Integer, String> entry : indexToSnippet.entrySet()) {
                    int index = entry.getKey();
                    String snippet = entry.getValue();

                    if (index < values.length) {
                        try {
                            double score = Double.parseDouble(values[index].trim());
                            scoreMap.computeIfAbsent(snippet, k -> new ArrayList<>()).add(score);
                        } catch (NumberFormatException ignored) {
                            // Skip invalid number
                        }
                    }
                }
            }

            // Compute average score per snippet
            for (Map.Entry<String, List<Double>> entry : scoreMap.entrySet()) {
                List<Double> ratings = entry.getValue();
                if (!ratings.isEmpty()) {
                    double avg = ratings.stream().mapToDouble(d -> d).average().orElse(0.0);
                    avgScores.put(entry.getKey(), avg);
                }
            }

            return avgScores;
        }

        /**
         * Returns the binary label "Y" or "N" based on average score and threshold.
         */
        private static String determineLabel(String filename, Map<String, Double> truthScores) {
            return truthScores.getOrDefault(filename, 0.0) >= TRUTH_THRESHOLD ? "Y" : "N";
        }
    }

