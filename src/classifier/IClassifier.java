package classifier;

import model.ProcessedRecord;
import java.util.List;

/**
 * Interface for machine learning classifiers.
 * Defines the contract that all classifiers must implement.
 */
public interface IClassifier {

    /**
     * Trains the classifier using the provided training data.
     *
     * @param trainingData list of processed training records
     */
    void train(List<ProcessedRecord> trainingData);

    /**
     * Predicts the class label for a single feature vector.
     *
     * @param features normalized feature vector
     * @return predicted class label
     */
    String predict(double[] features);

    /**
     * Predicts class labels for multiple feature vectors.
     *
     * @param featuresList list of normalized feature vectors
     * @return list of predicted class labels
     */
    List<String> predict(List<double[]> featuresList);

    /**
     * Returns the name of this classifier for display purposes.
     *
     * @return classifier name
     */
    String getName();
}
