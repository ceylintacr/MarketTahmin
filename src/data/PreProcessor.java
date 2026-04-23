package data;

import java.util.*;
import model.UserRecord;

public class PreProcessor {
    private Map<String, Integer> cityIndex = new HashMap<>();
    
    // Fiyat için Min-Max Değişkenleri
    private double minScore = Double.MAX_VALUE, maxScore = -Double.MAX_VALUE, scoreRange = 1.0;
    
    // Yaş için Min-Max Değişkenleri
    private double minAge = Double.MAX_VALUE, maxAge = -Double.MAX_VALUE, ageRange = 1.0;
    
    private int cityCount = 0;
    private boolean isFitted = false;

    /**
     * FIT: SADECE TRAIN VERİSİ İLE ÇALIŞIR.
     */
    public void fit(List<UserRecord> trainData) {
        cityIndex.clear();
        minScore = Double.MAX_VALUE; maxScore = -Double.MAX_VALUE;
        minAge = Double.MAX_VALUE; maxAge = -Double.MAX_VALUE;

        for (UserRecord r : trainData) {
            cityIndex.putIfAbsent(r.getCity(), cityIndex.size());
            
            double score = r.getSpendingScore();
            minScore = Math.min(minScore, score);
            maxScore = Math.max(maxScore, score);
            
            double age = r.getAge();
            minAge = Math.min(minAge, age);
            maxAge = Math.max(maxAge, age);
        }
        
        scoreRange = (maxScore - minScore == 0) ? 1 : (maxScore - minScore);
        ageRange = (maxAge - minAge == 0) ? 1 : (maxAge - minAge);
        
        cityCount = cityIndex.size();
        isFitted = true;
    }

    /**
     * TRANSFORM: TRAIN VE TEST VERİLERİNİ DÖNÜŞTÜRÜR.
     */
    public double[] transform(UserRecord user) {
        if (!isFitted) {
            throw new IllegalStateException("PreProcessor fit edilmedi! Önce fit() çağrılmalı.");
        }

        double genderVal = (user.getGender().equalsIgnoreCase("E") || user.getGender().equalsIgnoreCase("Male")) ? 1.0 : 0.0;
        
        double normScore = (user.getSpendingScore() - minScore) / scoreRange;
        double normAge = (user.getAge() - minAge) / ageRange;

        double[] features = new double[3 + cityCount];
        features[0] = genderVal;
        features[1] = normScore;
        features[2] = normAge;

        if (cityIndex.containsKey(user.getCity())) {
            features[3 + cityIndex.get(user.getCity())] = 1.0;
        }
        
        return features;
    }
}