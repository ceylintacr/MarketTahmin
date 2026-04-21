package classifier;

import model.UserRecord;
import java.util.List;

/**
 * Interface for machine learning classifiers.
 * Defines the contract that all classifiers must implement.
 */
public interface IClassifier {

    /**
     * Trains the classifier using the provided raw user training data.
     *
     * @param trainingData list of raw UserRecord instances
     */
    void train(List<UserRecord> trainingData);

    /**
     * Predicts the class label for a single UserRecord.
     *
     * @param user the user record to predict
     * @return predicted class label
     */
    String predict(UserRecord user);

    /**
     * Predicts class labels for multiple UserRecords.
     *
     * @param users list of user records
     * @return list of predicted class labels
     */
    List<String> predict(List<UserRecord> users);

    /**
     * Returns the name of this classifier for display purposes.
     *
     * @return classifier name
     */
    String getName();
}