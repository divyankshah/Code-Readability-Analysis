package de.uni_passau.fim.se2.sa.readability.utils;

import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.io.File;
import java.io.IOException;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.core.converters.CSVLoader;
import java.util.Random;
public class Classify {

    /**
     * Loads the {@link Instances} dataset by parsing the CSV file specified via the cli.
     *
     * @param data the CSV file to load.
     * @return the {@link Instances} dataset ready to be classified.
     * @throws IOException if the CSV file specified via the cli could not be loaded.
     */
    public static Instances loadDataset(File data) throws IOException {
        CSVLoader loader = new CSVLoader();
        loader.setSource(data);
        Instances dataset = loader.getDataSet();
        dataset.setClassIndex(dataset.numAttributes() - 1); // Set class to last column (Truth)
        return dataset;
    }

    /**
     * Trains and evaluates the "logistic" classifier on the given dataset.
     * For the evaluation, we apply a 10-fold cross-validation using a start seed with a value of 1.
     *
     * @param dataset The dataset to train and evaluate the logistic classifier on.
     * @return the evaluation object hosting the evaluation results.
     * @throws Exception if the classifier could not be generated successfully.
     */
    public static Evaluation trainAndEvaluate(Instances dataset) throws Exception {
        Classifier classifier = new Logistic();
        Evaluation evaluation = new Evaluation(dataset);
        evaluation.crossValidateModel(classifier, dataset, 10, new Random(1));
        return evaluation;
    }
}
