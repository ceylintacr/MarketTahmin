package classifier;

import data.PreProcessor;
import model.ProcessedRecord;
import model.UserRecord;
import java.util.*;

public class DecisionTreeClassifier extends BaseAlgorithm {

    private Node root;
    private PreProcessor preProcessor;
    private int maxDepth;

    public DecisionTreeClassifier(PreProcessor preProcessor, int maxDepth) {
        if (preProcessor == null) {
            throw new IllegalArgumentException("PreProcessor cannot be null");
        }
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("maxDepth must be positive");
        }
        this.preProcessor = preProcessor;
        this.maxDepth = maxDepth;
    }

    @Override
    public void train(List<UserRecord> rawTrainingData) {
        if (rawTrainingData == null || rawTrainingData.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty");
        }

        // 1. DATA LEAKAGE FIX: Sadece train verisiyle FIT et
        this.preProcessor.fit(rawTrainingData);
        
        // 2. Train verisini TRANSFORM et ve model ağacını kur
        List<ProcessedRecord> processedData = new ArrayList<>();
        for (UserRecord user : rawTrainingData) {
            double[] features = this.preProcessor.transform(user);
            processedData.add(new ProcessedRecord(features, user.getCategory()));
        }
        
        root = buildTree(processedData, 0);
    }

    @Override
    public String predict(UserRecord user) {
        if (root == null || preProcessor == null) {
            throw new IllegalStateException("Classifier must be trained before prediction");
        }
        
        // Yeni gelen kullanıcıyı (test) transform et.
        double[] features = preProcessor.transform(user);
        return predictRecursive(root, features);
    }

    @Override
    public List<String> predict(List<UserRecord> users) {
        List<String> results = new ArrayList<>();
        for (UserRecord user : users) {
            results.add(predict(user));
        }
        return results;
    }

    @Override
    public String getName() {
        return "Decision Tree (Gini)";
    }

    private String predictRecursive(Node node, double[] features) {
        if (node.label != null)
            return node.label;

        if (features[node.featureIndex] < node.threshold)
            return predictRecursive(node.left, features);
        else
            return predictRecursive(node.right, features);
    }

    // ================= TREE =================

    private Node buildTree(List<ProcessedRecord> data, int depth) {
        if (data.isEmpty())
            return null;

        if (depth >= maxDepth || isPure(data)) {
            return new Node(getMajority(data));
        }

        int featureCount = data.get(0).getFeatures().length;
        double bestGini = Double.MAX_VALUE;
        int bestFeature = -1;
        double bestThreshold = 0;

        for (int i = 0; i < featureCount; i++) {
            List<Double> values = new ArrayList<>();
            for (ProcessedRecord r : data) {
                values.add(r.getFeatures()[i]);
            }
            Collections.sort(values);

            for (int j = 1; j < values.size(); j++) {
                if (values.get(j).equals(values.get(j - 1))) continue;

                double threshold = (values.get(j - 1) + values.get(j)) / 2;
                double giniScore = calculateSplitScore(data, i, threshold);

                if (giniScore < bestGini) {
                    bestGini = giniScore;
                    bestFeature = i;
                    bestThreshold = threshold;
                }
            }
        }

        if (bestFeature == -1) {
            return new Node(getMajority(data));
        }

        List<ProcessedRecord> left = new ArrayList<>();
        List<ProcessedRecord> right = new ArrayList<>();

        for (ProcessedRecord r : data) {
            if (r.getFeatures()[bestFeature] < bestThreshold)
                left.add(r);
            else
                right.add(r);
        }

        if (left.isEmpty() || right.isEmpty()) {
            return new Node(getMajority(data));
        }

        Node node = new Node(bestFeature, bestThreshold);
        node.left = buildTree(left, depth + 1);
        node.right = buildTree(right, depth + 1);

        return node;
    }

    // ================= GINI HESAPLAMASI =================

    private double calculateSplitScore(List<ProcessedRecord> data, int feature, double threshold) {
        Map<String, Integer> left = new HashMap<>();
        Map<String, Integer> right = new HashMap<>();
        int leftCount = 0;
        int rightCount = 0;

        for (ProcessedRecord r : data) {
            if (r.getFeatures()[feature] < threshold) {
                left.put(r.getLabel(), left.getOrDefault(r.getLabel(), 0) + 1);
                leftCount++;
            } else {
                right.put(r.getLabel(), right.getOrDefault(r.getLabel(), 0) + 1);
                rightCount++;
            }
        }

        int total = data.size();
        double leftWeight = (double) leftCount / total;
        double rightWeight = (double) rightCount / total;

        return (leftWeight * gini(left)) + (rightWeight * gini(right));
    }

    private double gini(Map<String, Integer> map) {
        int total = map.values().stream().mapToInt(i -> i).sum();
        if (total == 0) return 0;

        double sum = 0;
        for (int count : map.values()) {
            double p = (double) count / total;
            sum += p * p;
        }

        return 1 - sum; 
    }

    private boolean isPure(List<ProcessedRecord> data) {
        String first = data.get(0).getLabel();
        for (ProcessedRecord r : data) {
            if (!r.getLabel().equals(first))
                return false;
        }
        return true;
    }

    private String getMajority(List<ProcessedRecord> data) {
        Map<String, Integer> map = new HashMap<>();
        for (ProcessedRecord r : data) {
            map.put(r.getLabel(), map.getOrDefault(r.getLabel(), 0) + 1);
        }
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();
    }

    static class Node {
        int featureIndex;
        double threshold;
        Node left, right;
        String label;

        Node(String label) {
            this.label = label;
        }

        Node(int featureIndex, double threshold) {
            this.featureIndex = featureIndex;
            this.threshold = threshold;
        }
    }
}