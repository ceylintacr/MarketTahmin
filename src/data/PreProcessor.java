package data;

import java.util.*;
import model.UserRecord;
import model.ProcessedRecord;

public class PreProcessor {
    private Map<String, Integer> cityIndex = new HashMap<>();
    
    // Fiyat ve Yaş için Min-Max Değişkenleri
    private double minPrice = Double.MAX_VALUE, maxPrice = Double.MIN_VALUE, priceRange = 1.0;
    private double minAge = Double.MAX_VALUE, maxAge = Double.MIN_VALUE, ageRange = 1.0;
    
    private int cityCount = 0;
    private boolean isFitted = false;

    public List<ProcessedRecord> process(List<UserRecord> data) {
        List<ProcessedRecord> result = new ArrayList<>();

        // 1. Aşama: Şehirleri indeksle ve Fiyat & Yaş için Min-Max değerlerini bul
        for (UserRecord r : data) {
            cityIndex.putIfAbsent(r.getCity(), cityIndex.size());
            
            // Fiyat sınırları
            double price = r.getTotalPrice();
            minPrice = Math.min(minPrice, price);
            maxPrice = Math.max(maxPrice, price);

            // Yaş sınırları
            double age = r.getAge();
            minAge = Math.min(minAge, age);
            maxAge = Math.max(maxAge, age);
        }
        
        // Aralıkları hesapla (Sıfıra bölünme hatasını önle)
        priceRange = (maxPrice - minPrice == 0) ? 1 : (maxPrice - minPrice);
        ageRange = (maxAge - minAge == 0) ? 1 : (maxAge - minAge);
        
        cityCount = cityIndex.size();
        isFitted = true;

        // 2. Aşama: Verileri dönüştür
        for (UserRecord r : data) {
            result.add(new ProcessedRecord(
                transformSingleRecord(r.getGender(), r.getCity(), r.getTotalPrice(), r.getAge()), 
                r.getCategory()
            ));
        }
        return result;
    }

    // Metot imzasına 'age' parametresi eklendi
    public double[] transformSingleRecord(String gender, String city, double price, int age) {
        if (!isFitted) throw new IllegalStateException("PreProcessor fit edilmedi! Önce process() çağrılmalı.");

        // Cinsiyeti sayısala çevir (Erkek=1.0, Kadın=0.0)
        double genderVal = (gender.equalsIgnoreCase("E") || gender.equalsIgnoreCase("Male")) ? 1.0 : 0.0;
        
        // Fiyatı Min-Max formülü ile normalize et [0-1] aralığına çek
        double normPrice = (price - minPrice) / priceRange;

        // Yaşı Min-Max formülü ile normalize et [0-1] aralığına çek
        double normAge = (age - minAge) / ageRange;

        // Feature vektörü: [gender, normPrice, normAge, one-hot cities...]
        // Boyut yaş eklendiği için (3 + cityCount) oldu
        double[] features = new double[3 + cityCount];
        features[0] = genderVal;
        features[1] = normPrice;
        features[2] = normAge; // Normalize edilmiş yaş vektöre eklendi

        // Şehir bilgisini One-Hot Encoding ile vektöre yerleştir
        if (cityIndex.containsKey(city)) {
            features[3 + cityIndex.get(city)] = 1.0;
        }
        
        return features;
    }
}