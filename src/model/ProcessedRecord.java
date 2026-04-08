package model;

public class ProcessedRecord {

    private final double[] features;   // encoded + normalized feature vector
    private final String   label;      // original category string (class label)

    public ProcessedRecord(double[] features, String label) {
        if (features == null || features.length == 0) {
            throw new IllegalArgumentException("features must not be null or empty");
        }
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label must not be null or blank");
        }
        this.features = features.clone();  // defensive copy
        this.label    = label;
    }

    /** @return defensive copy of the feature vector */
    public double[] getFeatures() {
        return features.clone();
    }

    /** @return class label (target variable) */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ProcessedRecord{features=[");
        for (int i = 0; i < features.length; i++) {
            sb.append(String.format("%.4f", features[i]));
            if (i < features.length - 1) sb.append(", ");
        }
        sb.append("], label='").append(label).append("'}");
        return sb.toString();
    }
}