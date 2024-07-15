import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.Stopwatch;

import java.util.ArrayList;

public class BoostingAlgorithm {


    // Stores the clustering object used for clustering data points
    private Clustering clustering;

    // Stores the weights assigned to each data point
    private double[] weights;

    // Stores the modified inputs after clustering
    private int[][] trainingInputs;

    // Stores the labels of the training data
    private int[] trainingLabels;

    // Stores the weak learner used in boosting
    private ArrayList<WeakLearner> weakLearners;

    // input size
    private final int inputSize;

    // input size
    private final int nLocations;

    // create the clusters and initialize your data structures
    public BoostingAlgorithm(int[][] input, int[] labels, Point2D[] locations, int k) {
        validate(input);
        validate(labels);
        validate(locations);
        validate(input, labels);

        int n = input.length;
        inputSize = n;

        nLocations = locations.length;
        clustering = new Clustering(locations, k);
        trainingInputs = new int[n][k];
        trainingLabels = labels.clone();

        weights = new double[inputSize];
        weakLearners = new ArrayList<>();

        for (int i = 0; i < inputSize; i++)
            weights[i] = 1.0 / n;

        for (int i = 0; i < n; i++)
            // trainingInputs[i] = input[i].clone();
            trainingInputs[i] = clustering.reduceDimensions(input[i]);
    }

    // null validate objects
    private void validate(Object object) {
        if (object == null)
            throw new IllegalArgumentException("argument cannot be null");
    }

    //  validate training data
    private void validate(int[][] input, int[] labels) {
        int n = input.length;
        if (n != labels.length)
            throw new IllegalArgumentException("number of labels is invalid");

        for (int i = 0; i < labels.length; i++) {
            boolean isValidLabel = labels[i] == 0 || labels[i] == 1;
            if (!isValidLabel)
                throw new IllegalArgumentException("invalid label");
        }
    }

    // return the current weight of the ith point
    public double weightOf(int i) {
        return weights[i];
    }

    // apply one step of the boosting algorithm
    public void iterate() {
        WeakLearner learner = new WeakLearner(trainingInputs, weights, trainingLabels);

        // Update weights based on classification results
        double totalWeight = 0.0;
        for (int i = 0; i < inputSize; i++) {
            int prediction = learner.predict(trainingInputs[i]);
            if (prediction != trainingLabels[i])
                weights[i] *= 2.0;
            totalWeight += weights[i];
        }

        // re-normalize weights
        for (int i = 0; i < inputSize; i++)
            weights[i] /= totalWeight;


        // store the weak learner for future sampling
        weakLearners.add(learner);
    }

    // prediction of the learners for a new sample based on majority vote
    public int predict(int[] sample) {
        validate(sample);
        if (nLocations != sample.length)
            throw new IllegalArgumentException("invalid number of samples");
        int[] reducedSample = clustering.reduceDimensions(sample);
        // count each vote from weak learners
        int zeroVotes = 0, oneVotes = 0;

        for (WeakLearner learner : weakLearners) {
            if (learner.predict(reducedSample) == 0) zeroVotes++;
            else oneVotes++;
        }
        if (oneVotes > zeroVotes)
            return 1;
        return 0;
    }

    // unit testing (required)
    public static void main(String[] args) {

        // read in the terms from a file
        DataSet training = new DataSet(args[0]);
        DataSet testing = new DataSet(args[1]);
        int k = Integer.parseInt(args[2]);
        int iteration = Integer.parseInt(args[3]);

        int[][] trainingInput = training.getInput();
        int[][] testingInput = testing.getInput();
        int[] trainingLabels = training.getLabels();
        int[] testingLabels = testing.getLabels();
        Point2D[] trainingLocations = training.getLocations();

        // train the model

        Stopwatch watch = new Stopwatch();
        BoostingAlgorithm model = new BoostingAlgorithm(trainingInput, trainingLabels,
                                                        trainingLocations, k);

        StdOut.println(model.weightOf(2));
        for (int t = 0; t < iteration; t++)
            model.iterate();
        double testAccuracy = 0;
        for (int i = 0; i < testing.getN(); i++)
            if (model.predict(testingInput[i]) == testingLabels[i])
                testAccuracy += 1;
        testAccuracy /= testing.getN();


        StdOut.println("elapsted time " + watch.elapsedTime());
        StdOut.println("Test accuracy of model: " + testAccuracy);
    }
}

