package data;

import java.util.*;
import model.UserRecord;

public class PreProcessor {
    private Map<String, Integer> cityIndex = new HashMap<>();
    
    // Fiyat ve Yaş için Min-Max Değişkenleri
    private double minPrice = Double.MAX_VALUE, maxPrice = -Double.MAX_VALUE, priceRange = 1.0;
    private double minAge = Double.MAX_VALUE, maxAge = -Double.MAX_VALUE, ageRange = 1.0;
    
    private int cityCount = 0;
    private boolean isFitted = false;

    /**
     * FIT: SADECE TRAIN VERİSİ İLE ÇALIŞIR.
     * Verisetinin istatistiklerini (Min, Max, Şehir Listesi) öğrenir.
     */
    public void fit(List<UserRecord> trainData) {
        cityIndex.clear();
        minPrice = Double.MAX_VALUE; maxPrice = -Double.MAX_VALUE;
        minAge = Double.MAX_VALUE; maxAge = -Double.MAX_VALUE;

        // 1. Aşama: Şehirleri indeksle ve Fiyat & Yaş için Min-Max değerlerini bul
        for (UserRecord r : trainData) {
            cityIndex.putIfAbsent(r.getCity(), cityIndex.size());
            
            double price = r.getTotalPrice();
            minPrice = Math.min(minPrice, price);
            maxPrice = Math.max(maxPrice, price);

            double age = r.getAge();
            minAge = Math.min(minAge, age);
            maxAge = Math.max(maxAge, age);
        }
        
        // Aralıkları hesapla (Sıfıra bölünme hatasını önle)
        priceRange = (maxPrice - minPrice == 0) ? 1 : (maxPrice - minPrice);
        ageRange = (maxAge - minAge == 0) ? 1 : (maxAge - minAge);
        
        cityCount = cityIndex.size();
        isFitted = true;
    }

    /**
     * TRANSFORM: TRAIN VE TEST VERİLERİNİ DÖNÜŞTÜRÜR.
     * Fit işleminde öğrenilen parametreleri uygular.
     */
    public double[] transform(UserRecord user) {
        if (!isFitted) {
            throw new IllegalStateException("PreProcessor fit edilmedi! Önce fit() çağrılmalı.");
        }

        // Cinsiyeti sayısala çevir (Erkek=1.0, Kadın=0.0)
        double genderVal = (user.getGender().equalsIgnoreCase("E") || user.getGender().equalsIgnoreCase("Male")) ? 1.0 : 0.0;
        
        // Fiyatı Min-Max formülü ile normalize et
        double normPrice = (user.getTotalPrice() - minPrice) / priceRange;

        // Yaşı Min-Max formülü ile normalize et
        double normAge = (user.getAge() - minAge) / ageRange;

        // Feature vektörü: [gender, normPrice, normAge, one-hot cities...]
        double[] features = new double[3 + cityCount];
        features[0] = genderVal;
        features[1] = normPrice;
        features[2] = normAge; 

        // Şehir bilgisini One-Hot Encoding ile vektöre yerleştir
        // Eğer test verisinde daha önce (train'de) hiç görmediğimiz bir şehir varsa sıfır kalır (Out-Of-Vocabulary koruması)
        if (cityIndex.containsKey(user.getCity())) {
            features[3 + cityIndex.get(user.getCity())] = 1.0;
        }
        
        return features;
    }
}