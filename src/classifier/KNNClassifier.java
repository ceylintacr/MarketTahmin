package classifier;

import data.PreProcessor;
import model.ProcessedRecord;
import model.UserRecord;
import java.util.*;

public class KNNClassifier extends BaseAlgorithm {

    private List<ProcessedRecord> trainingData;
    private PreProcessor preProcessor;
    private int k;

    public KNNClassifier(int k, PreProcessor preProcessor) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        if (preProcessor == null) {
            throw new IllegalArgumentException("PreProcessor cannot be null");
        }
        this.k = k;
        this.preProcessor = preProcessor;
    }

    @Override
    public void train(List<UserRecord> rawTrainingData) {
        if (rawTrainingData == null || rawTrainingData.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty");
        }
        
        // 1. DATA LEAKAGE FIX: Önce sadece eğitim verisiyle modeli (scaling, encoding) FIT et.
        this.preProcessor.fit(rawTrainingData);
        
        // 2. Eğitim verilerini TRANSFORM et ve sınıflandırıcıya kaydet.
        this.trainingData = new ArrayList<>();
        for (UserRecord user : rawTrainingData) {
            double[] features = this.preProcessor.transform(user);
            this.trainingData.add(new ProcessedRecord(features, user.getCategory()));
        }
        
        System.out.println("[KNNClassifier] Trained on " + trainingData.size() + " records with k=" + k);
    }

    @Override
    public String predict(UserRecord user) {
        if (trainingData == null || preProcessor == null) {
            throw new IllegalStateException("Classifier must be trained before prediction");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // Test verisini veya tekil müşteriyi mevcut eğitim istatistikleriyle TRANSFORM et.
        double[] features = preProcessor.transform(user);

        List<Neighbor> neighbors = new ArrayList<>();
        for (ProcessedRecord record : trainingData) {
            double distance = euclideanDistance(features, record.getFeatures());
            neighbors.add(new Neighbor(distance, record.getLabel()));
        }

        neighbors.sort(Comparator.comparingDouble(n -> n.distance));

        Map<String, Integer> labelCounts = new HashMap<>();
        for (int i = 0; i < Math.min(k, neighbors.size()); i++) {
            String label = neighbors.get(i).label;
            labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
        }

        return labelCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    @Override
    public List<String> predict(List<UserRecord> users) {
        List<String> predictions = new ArrayList<>();
        for (UserRecord user : users) {
            predictions.add(predict(user));
        }
        return predictions;
    }

    @Override
    public String getName() {
        return "K-Nearest Neighbors (k=" + k + ")";
    }

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

    private static class Neighbor {
        final double distance;
        final String label;

        Neighbor(double distance, String label) {
            this.distance = distance;
            this.label = label;
        }
    }

    public void setK(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        this.k = k;
    }

    public int getK() {
        return k;
    }
}