package data;

import java.util.*;
import model.UserRecord;

public class PreProcessor {
    private Map<String, Integer> brandIndex = new HashMap<>();
    
    // Fiyat için Min-Max Değişkenleri
    private double minPrice = Double.MAX_VALUE, maxPrice = -Double.MAX_VALUE, priceRange = 1.0;
    
    private int brandCount = 0;
    private boolean isFitted = false;

    /**
     * FIT: SADECE TRAIN VERİSİ İLE ÇALIŞIR.
     * Verisetinin istatistiklerini (Min, Max, Marka Listesi) öğrenir.
     */
    public void fit(List<UserRecord> trainData) {
        brandIndex.clear();
        minPrice = Double.MAX_VALUE; maxPrice = -Double.MAX_VALUE;

        // 1. Aşama: Markaları indeksle ve Fiyat için Min-Max değerlerini bul
        for (UserRecord r : trainData) {
            brandIndex.putIfAbsent(r.getBrand(), brandIndex.size());
            
            double price = r.getLineNetTotal();
            minPrice = Math.min(minPrice, price);
            maxPrice = Math.max(maxPrice, price);
        }
        
        // Aralıkları hesapla (Sıfıra bölünme hatasını önle)
        priceRange = (maxPrice - minPrice == 0) ? 1 : (maxPrice - minPrice);
        
        brandCount = brandIndex.size();
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
        double normPrice = (user.getLineNetTotal() - minPrice) / priceRange;

        // Feature vektörü: [gender, normPrice, one-hot brands...]
        // Not: clientCode tahmin için bir özellik (feature) değil, eşsiz bir kimliktir, bu yüzden vektöre eklenmez.
        double[] features = new double[2 + brandCount];
        features[0] = genderVal;
        features[1] = normPrice;

        // Marka bilgisini One-Hot Encoding ile vektöre yerleştir
        // Eğer test verisinde daha önce (train'de) hiç görmediğimiz bir marka varsa sıfır kalır (Out-Of-Vocabulary koruması)
        if (brandIndex.containsKey(user.getBrand())) {
            features[2 + brandIndex.get(user.getBrand())] = 1.0;
        }
        
        return features;
    }
}