package evaluation;

import classifier.IClassifier;
import model.UserRecord;
import java.util.*;
import java.util.function.Supplier; // EKLENDİ

/**
 * Evaluates classifier performance using various metrics.
 */
public class Evaluator {

    public EvaluationResult evaluate(IClassifier classifier, List<UserRecord> testData) {
        if (classifier == null) {
            throw new IllegalArgumentException("Classifier cannot be null");
        }
        if (testData == null || testData.isEmpty()) {
            throw new IllegalArgumentException("Test data cannot be null or empty");
        }

        List<String> trueLabels = new ArrayList<>();
        List<String> predictedLabels = new ArrayList<>();

        for (UserRecord record : testData) {
            trueLabels.add(record.getCategory());
            predictedLabels.add(classifier.predict(record));
        }

        return calculateMetrics(trueLabels, predictedLabels);
    }

    /**
     * FIX 9: IClassifier nesnesi yerine Supplier<IClassifier> alıyoruz.
     * Bu sayede her fold için "classifierSupplier.get()" diyerek TERTEMİZ bir model üreteceğiz.
     */
    public EvaluationResult crossValidate(Supplier<IClassifier> classifierSupplier, List<UserRecord> data, int k) {
        if (k <= 1) {
            throw new IllegalArgumentException("k must be greater than 1");
        }
        if (data == null || data.size() < k) {
            throw new IllegalArgumentException("Not enough data for " + k + "-fold cross-validation");
        }

        List<UserRecord> shuffledData = new ArrayList<>(data);
        Collections.shuffle(shuffledData);

        int foldSize = data.size() / k;
        List<EvaluationResult> foldResults = new ArrayList<>();

        for (int i = 0; i < k; i++) {
            List<UserRecord> testFold = new ArrayList<>();
            List<UserRecord> trainFold = new ArrayList<>();

            for (int j = 0; j < data.size(); j++) {
                if (j >= i * foldSize && j < (i + 1) * foldSize) {
                    testFold.add(shuffledData.get(j));
                } else {
                    trainFold.add(shuffledData.get(j));
                }
            }

            // FIX 9: Her fold'da YENİ bir classifier ve YENİ bir PreProcessor nesnesi yaratılır.
            IClassifier classifier = classifierSupplier.get(); 
            
            classifier.egit(trainFold);
            EvaluationResult result = evaluate(classifier, testFold);
            foldResults.add(result);
        }

        return averageResults(foldResults);
    }

    private EvaluationResult calculateMetrics(List<String> trueLabels, List<String> predictedLabels) {
        if (trueLabels.size() != predictedLabels.size()) {
            throw new IllegalArgumentException("True and predicted labels must have same size");
        }

        Map<String, Map<String, Integer>> confusionMatrix = new HashMap<>();
        Set<String> allLabels = new HashSet<>();
        allLabels.addAll(trueLabels);
        allLabels.addAll(predictedLabels);

        for (String trueLabel : allLabels) {
            confusionMatrix.put(trueLabel, new HashMap<>());
            for (String predLabel : allLabels) {
                confusionMatrix.get(trueLabel).put(predLabel, 0);
            }
        }

        for (int i = 0; i < trueLabels.size(); i++) {
            String trueLabel = trueLabels.get(i);
            String predLabel = predictedLabels.get(i);
            confusionMatrix.get(trueLabel).put(predLabel,
                confusionMatrix.get(trueLabel).get(predLabel) + 1);
        }

        int total = trueLabels.size();
        int correct = 0;
        Map<String, Double> precision = new HashMap<>();
        Map<String, Double> recall = new HashMap<>();
        Map<String, Double> f1Score = new HashMap<>();

        for (String label : allLabels) {
            int truePositives = confusionMatrix.get(label).get(label);
            int falsePositives = 0;
            int falseNegatives = 0;

            for (String otherLabel : allLabels) {
                if (!otherLabel.equals(label)) {
                    falsePositives += confusionMatrix.get(otherLabel).get(label);
                }
            }

            for (String otherLabel : allLabels) {
                if (!otherLabel.equals(label)) {
                    falseNegatives += confusionMatrix.get(label).get(otherLabel);
                }
            }

            correct += truePositives;

            double prec = (truePositives + falsePositives) == 0 ? 0.0 :
                (double) truePositives / (truePositives + falsePositives);
            double rec = (truePositives + falseNegatives) == 0 ? 0.0 :
                (double) truePositives / (truePositives + falseNegatives);
            double f1 = (prec + rec) == 0 ? 0.0 : 2 * prec * rec / (prec + rec);

            precision.put(label, prec);
            recall.put(label, rec);
            f1Score.put(label, f1);
        }

        double accuracy = (double) correct / total;

        return new EvaluationResult(accuracy, precision, recall, f1Score, confusionMatrix);
    }

    private EvaluationResult averageResults(List<EvaluationResult> results) {
        if (results.isEmpty()) {
            throw new IllegalArgumentException("Results list cannot be empty");
        }

        double avgAccuracy = results.stream().mapToDouble(r -> r.accuracy).average().orElse(0.0);

        Set<String> allLabels = new HashSet<>();
        for (EvaluationResult result : results) {
            allLabels.addAll(result.precision.keySet());
        }

        Map<String, Double> avgPrecision = new HashMap<>();
        Map<String, Double> avgRecall = new HashMap<>();
        Map<String, Double> avgF1Score = new HashMap<>();

        for (String label : allLabels) {
            double prec = results.stream()
                .mapToDouble(r -> r.precision.getOrDefault(label, 0.0))
                .average().orElse(0.0);
            double rec = results.stream()
                .mapToDouble(r -> r.recall.getOrDefault(label, 0.0))
                .average().orElse(0.0);
            double f1 = results.stream()
                .mapToDouble(r -> r.f1Score.getOrDefault(label, 0.0))
                .average().orElse(0.0);

            avgPrecision.put(label, prec);
            avgRecall.put(label, rec);
            avgF1Score.put(label, f1);
        }

        Map<String, Map<String, Integer>> confusionMatrix = results.get(results.size() - 1).confusionMatrix;

        return new EvaluationResult(avgAccuracy, avgPrecision, avgRecall, avgF1Score, confusionMatrix);
    }

    public static class EvaluationResult {
        public final double accuracy;
        public final Map<String, Double> precision;
        public final Map<String, Double> recall;
        public final Map<String, Double> f1Score;
        public final Map<String, Map<String, Integer>> confusionMatrix;

        public EvaluationResult(double accuracy, Map<String, Double> precision,
                              Map<String, Double> recall, Map<String, Double> f1Score,
                              Map<String, Map<String, Integer>> confusionMatrix) {
            this.accuracy = accuracy;
            this.precision = new HashMap<>(precision);
            this.recall = new HashMap<>(recall);
            this.f1Score = new HashMap<>(f1Score);
            this.confusionMatrix = deepCopyConfusionMatrix(confusionMatrix);
        }

        private Map<String, Map<String, Integer>> deepCopyConfusionMatrix(
                Map<String, Map<String, Integer>> original) {
            Map<String, Map<String, Integer>> copy = new HashMap<>();
            for (Map.Entry<String, Map<String, Integer>> entry : original.entrySet()) {
                copy.put(entry.getKey(), new HashMap<>(entry.getValue()));
            }
            return copy;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Accuracy: %.4f%n", accuracy));
            sb.append("Per-class metrics:\n");

            for (String label : precision.keySet()) {
                sb.append(String.format("  %s - Precision: %.4f, Recall: %.4f, F1: %.4f%n",
                    label,
                    precision.get(label),
                    recall.get(label),
                    f1Score.get(label)));
            }
            return sb.toString();
        }
    }
}