import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.princeton.cs.algs4.StdOut;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class WeakLearner {

    // Stores the predictive value used for comparisons.
    private int value;

    // Stores the dimension index used for predictions.
    private int dimension;

    // Stores the predicted sign (0 or 1).
    private int signP;
    // Dimensionality of the input data.
    private int dp;


    // Inner class to pair weight and label for easier management of data points.
    private static class Pairing {
        // Weight of the data point.
        double weight;
        // Label of the data point.
        int label;

        // Constructor for Pair.
        private Pairing(double weight, int label) {
            this.weight = weight;
            this.label = label;
        }
    }

    // Initializes the model with input data, weights, and labels.
    // input:Matrix of input data.
    // weights: Array of weights corresponding to each input vector.
    // labels:  Array of labels corresponding to each input vector.
    // throw IllegalArgumentException if any argument is null or dimensions mismatch.
    public WeakLearner(int[][] input, double[] weights, int[] labels) {
        validateInput(input, weights, labels);
        dp = input[0].length;
        calculateWeights(input, weights, labels);
    }

    // Calculates weights for each dimension and predicts the best split.
    private void calculateWeights(int[][] input, double[] weights, int[] labels) {

        int numPoints = input.length;
        double weightSum = 0;
        double redWeightSum = 0;

        // Calculate total weight and weights for red labels.
        for (int i = 0; i < numPoints; i++) {
            if (labels[i] == 1)
                redWeightSum += weights[i];
            weightSum += weights[i];
        }

        double blackWeightSum = (weightSum - redWeightSum);
        double bestWeight = 0;

        for (int k = 0; k < dp; k++) {

            // Create a map to store weights and labels for
            // each unique value in the current dimension.
            TreeMap<Integer, LinkedList<Pairing>> tableSet =
                    createTableSet(input, weights, labels, k);

            double correctBlackWeight = 0;
            double correctRedWeight = 0;

            RedBlackBST<Double, Integer> redScore = new RedBlackBST<>();
            RedBlackBST<Double, Integer> blackScore = new RedBlackBST<>();

            // Iterate over each unique value and calculate the weighted
            // average for each label.
            for (int tableInput : tableSet.keySet()) {

                List<Pairing> pointsLabelsWeights = tableSet.get(tableInput);
                for (Pairing pair : pointsLabelsWeights) {
                    if (pair.label == 0) correctBlackWeight += pair.weight;
                    else if (pair.label == 1) correctRedWeight += pair.weight;
                    else throw new
                                IllegalArgumentException("label wrong");
                }

                // Calculate and store the weighted averages
                // for each label in RedBlackBST.
                double averageBlackWeight =
                        (correctBlackWeight + (redWeightSum - correctRedWeight))
                                / weightSum;

                double averageRedWeight = (correctRedWeight +
                        (blackWeightSum - correctBlackWeight))
                        / weightSum;

                redScore.put(averageBlackWeight, tableInput);
                blackScore.put(averageRedWeight, tableInput);
            }

            // Find the maximum percentage of correctly
            // classified samples for each label.
            double good1 = blackScore.max();
            double good0 = redScore.max();

            // Update prediction parameters if a new maximum percentage is found.
            if (good1 >= bestWeight) {
                signP = 1;
                bestWeight = good1;
                value = blackScore.get(good1);
                this.dimension = k;
            }
            if (good0 >= bestWeight) {
                signP = 0;
                bestWeight = good0;
                value = redScore.get(good0);
                this.dimension = k;
            }
        }
    }

    // Create a map to store weights and labels for each unique
    // value in the current dimension.
    private TreeMap<Integer, LinkedList<Pairing>>
    createTableSet(int[][] input, double[] weights,
                   int[] labels, int k) {


        int nPoints = input.length;
        TreeMap<Integer, LinkedList<Pairing>> tableSet = new TreeMap<>();
        for (int i = 0; i < nPoints; ++i) {
            int transaction = input[i][k];
            List<Pairing> indexTransaction = tableSet.get(transaction);

            if (indexTransaction == null) {

                LinkedList<Pairing> nullTransaction = new LinkedList<>();
                Pairing pair = new Pairing(weights[i], labels[i]);
                nullTransaction.add(pair);
                tableSet.put(input[i][k], nullTransaction);
                continue;
            }
            indexTransaction.add(new Pairing(weights[i], labels[i]));
        }
        return tableSet;
    }

    // Validate input data and throw exceptions if invalid.
    private void validateInput(int[][] input, double[] weights, int[] labels) {
        if (input == null || weights == null || labels == null)
            throw new IllegalArgumentException("argums not null");
        int nPoints = input.length;
        if (weights.length != nPoints || labels.length != nPoints)
            throw new IllegalArgumentException("Wrong argums");
    }

    /*
     Predicts the label for a given sample vector.
     sample: The sample vector to predict.
     return: The predicted label (0 or 1).
     throws: IllegalArgumentException if the sample is null or in the wrong format.
     */
    public int predict(int[] sample) {
        if (sample == null || sample.length != dp)
            throw new IllegalArgumentException("sample invalid");

        if (sample[dimension] <= value)
            return signP;

        return flipSign(signP);
    }

    // Flip the sign.
    private int flipSign(int sign) {
        if (sign != 1 && sign != 0)
            throw new IllegalArgumentException("wrong sign");

        if (sign == 0) {
            return 1;
        }
        else {
            return 0;
        }
    }


    // Returns the dimension index used for predictions.
    // return The dimension index.
    public int dimensionPredictor() {
        return dimension;
    }


    // Returns the predictive value used for comparisons.
    // return The predictive value
    public int valuePredictor() {
        return value;
    }


    // Returns the predicted sign (0 or 1).
    public int signPredictor() {
        return signP;
    }

    // Main method for testing.
    public static void main(String[] args) {
        In datafile = new In(args[0]);

        int n = datafile.readInt();
        int k = datafile.readInt();

        int[][] input = new int[n][k];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < k; j++) {
                input[i][j] = datafile.readInt();
            }
        }

        int[] labels = new int[n];
        for (int i = 0; i < n; i++) {
            labels[i] = datafile.readInt();
        }


        double[] weights = new double[n];
        for (int i = 0; i < n; i++) {
            weights[i] = datafile.readDouble();
        }


        WeakLearner weakLearner = new WeakLearner(input, weights, labels);
        // Create a sample array for prediction
        // Assuming sample length is the same as input length
        int[] sample = new int[k];
        for (int i = 0; i < k; i++) {

            // Using the first row of the input matrix as the sample
            sample[i] = input[0][i];
        }

        // Predict using the sample array
        int prediction = weakLearner.predict(sample);
        StdOut.println("Prediction: " + prediction);

        StdOut.printf("vp = %d, dp = %d, sp = %d\n", weakLearner.valuePredictor(),
                      weakLearner.dimensionPredictor(), weakLearner.signPredictor());
    }
}
