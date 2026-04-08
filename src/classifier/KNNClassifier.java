package classifier;

import model.ProcessedRecord;
import java.util.*;

/**
 * K-Nearest Neighbors classifier implementation.
 * Uses Euclidean distance to find the k nearest neighbors and majority voting for classification.
 */
public class KNNClassifier implements IClassifier {

    private List<ProcessedRecord> trainingData;
    private int k;

    /**
     * Creates a KNN classifier with default k=3.
     */
    public KNNClassifier() {
        this.k = 3;
    }

    /**
     * Creates a KNN classifier with specified k value.
     *
     * @param k number of nearest neighbors to consider
     */
    public KNNClassifier(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        this.k = k;
    }

    @Override
    public void train(List<ProcessedRecord> trainingData) {
        if (trainingData == null || trainingData.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty");
        }
        this.trainingData = new ArrayList<>(trainingData);
        System.out.println("[KNNClassifier] Trained on " + trainingData.size() + " records with k=" + k);
    }

    @Override
    public String predict(double[] features) {
        if (trainingData == null) {
            throw new IllegalStateException("Classifier must be trained before prediction");
        }
        if (features == null || features.length == 0) {
            throw new IllegalArgumentException("Features cannot be null or empty");
        }

        // Calculate distances to all training points
        List<Neighbor> neighbors = new ArrayList<>();
        for (ProcessedRecord record : trainingData) {
            double distance = euclideanDistance(features, record.getFeatures());
            neighbors.add(new Neighbor(distance, record.getLabel()));
        }

        // Sort by distance (ascending)
        neighbors.sort(Comparator.comparingDouble(n -> n.distance));

        // Get k nearest neighbors
        Map<String, Integer> labelCounts = new HashMap<>();
        for (int i = 0; i < Math.min(k, neighbors.size()); i++) {
            String label = neighbors.get(i).label;
            labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
        }

        // Return the most frequent label
        return labelCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    @Override
    public List<String> predict(List<double[]> featuresList) {
        List<String> predictions = new ArrayList<>();
        for (double[] features : featuresList) {
            predictions.add(predict(features));
        }
        return predictions;
    }

    @Override
    public String getName() {
        return "K-Nearest Neighbors (k=" + k + ")";
    }

    /**
     * Calculates Euclidean distance between two feature vectors.
     */
    private double euclideanDistance(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Feature vectors must have same length");
        }

        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    /**
     * Helper class to store neighbor information.
     */
    private static class Neighbor {
        final double distance;
        final String label;

        Neighbor(double distance, String label) {
            this.distance = distance;
            this.label = label;
        }
    }

    /**
     * Sets the k value for this classifier.
     *
     * @param k new k value
     */
    public void setK(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        this.k = k;
    }

    /**
     * Gets the current k value.
     *
     * @return k value
     */
    public int getK() {
        return k;
    }
}
