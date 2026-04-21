import data.DataLoader;
import data.PreProcessor;
import classifier.*;
import evaluation.Evaluator;
import model.*;

import java.util.*;

public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filePath = "data/MarketSalesKocaeli.xlsx";

        System.out.println("Veri seti yükleniyor ve algoritmalar eğitiliyor. Lütfen bekleyin...");

        DataLoader loader = new DataLoader();
        List<UserRecord> rawData = loader.load(filePath);

        Collections.shuffle(rawData, new Random(42));
        int split = (int) (rawData.size() * 0.8);
        List<UserRecord> train = rawData.subList(0, split);
        List<UserRecord> test = rawData.subList(split, rawData.size());

        KNNClassifier knn = new KNNClassifier(5, new PreProcessor());
        DecisionTreeClassifier dt = new DecisionTreeClassifier(new PreProcessor());
        Evaluator evaluator = new Evaluator();

        knn.train(train);
        dt.train(train);

        boolean running = true;
        while (running) {
            System.out.println("\n============= MARKET SATIŞ TAHMİN SİSTEMİ =============");
            System.out.println("1. Modellerin Performans Raporunu Göster (%80 Train - %20 Test)");
            System.out.println("2. Yeni Müşteri İçin Tahmin Yap");
            System.out.println("3. 5-Katlamalı Çapraz Doğrulama (5-Fold Cross Validation) Yap");
            System.out.println("4. En İyi K Değerini Bul (Hiperparametre Optimizasyonu)");
            System.out.println("5. Çıkış");
            System.out.print("Seçiminiz: ");

            String secim = scanner.nextLine();

            switch (secim) {
                case "1":
                    System.out.println("\n--- KNN (K=" + knn.getK() + ") Sonuçları ---");
                    System.out.println(evaluator.evaluate(knn, test));
                    System.out.println("--- Karar Ağacı Sonuçları ---");
                    System.out.println(evaluator.evaluate(dt, test));
                    break;

                case "2":
                    System.out.println("\nLütfen Müşteri Bilgilerini Giriniz:");
                    
                    System.out.print("Yaş: ");
                    int age = 0;
                    try {
                        age = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Hatalı yaş girdiniz! Lütfen tam sayı kullanın.");
                        break;
                    }

                    System.out.print("Cinsiyet (E/K): ");
                    String gender = scanner.nextLine();
                    
                    System.out.print("Şehir (Örn: İzmit, Gebze, vb.): ");
                    String city = scanner.nextLine();
                    
                    System.out.print("Harcama Tutarı (Örn: 500.50): ");
                    double price = 0;
                    try {
                        price = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Hatalı tutar girdiniz! Lütfen sayı kullanın.");
                        break;
                    }

                    UserRecord newUser = new UserRecord(age, gender, city, price, "Bilinmiyor");

                    String knnPrediction = knn.predict(newUser);
                    String dtPrediction = dt.predict(newUser);

                    System.out.println("\n===== TAHMİN SONUÇLARI =====");
                    System.out.println("KNN (K=" + knn.getK() + ") Tahmini : Ürün Kategorisi -> " + knnPrediction);
                    System.out.println("Karar Ağacı Tahmini   : Ürün Kategorisi -> " + dtPrediction);
                    System.out.println("============================");
                    break;

                case "3":
                    System.out.println("\nVeri seti 5 parçaya bölünüyor ve Çapraz Doğrulama başlatılıyor...");
                    int kFold = 5;

                    System.out.println("\n--- KNN (" + kFold + "-Fold, K=" + knn.getK() + ") Ortalama Sonuçları ---");
                    // FIX 9: Lambda ifadesi ile her fold için yeni bir KNNClassifier objesi yaratıyoruz.
                    Evaluator.EvaluationResult cvKnn = evaluator.crossValidate(
                        () -> new KNNClassifier(knn.getK(), new PreProcessor()), rawData, kFold
                    );
                    System.out.println(cvKnn);

                    System.out.println("--- Karar Ağacı (" + kFold + "-Fold) Ortalama Sonuçları ---");
                    // FIX 9: Lambda ifadesi ile her fold için yeni bir DecisionTreeClassifier objesi yaratıyoruz.
                    Evaluator.EvaluationResult cvDt = evaluator.crossValidate(
                        () -> new DecisionTreeClassifier(new PreProcessor()), rawData, kFold
                    );
                    System.out.println(cvDt);
                    break;

                case "4":
                    System.out.println("\n--- KNN İçin En İyi K Değeri Aranıyor (K=1 ile 15 arası) ---");
                    int bestK = 1;
                    double bestAccuracy = 0.0;

                    for (int k = 1; k <= 15; k += 2) {
                        final int currentK = k; // Java lambda kuralları gereği effectively final olmalı
                        Evaluator.EvaluationResult res = evaluator.crossValidate(
                            () -> new KNNClassifier(currentK, new PreProcessor()), rawData, 5
                        );
                        System.out.printf("K = %2d deneniyor... Doğruluk: %.4f\n", k, res.accuracy);

                        if (res.accuracy > bestAccuracy) {
                            bestAccuracy = res.accuracy;
                            bestK = k;
                        }
                    }

                    System.out.println("\n=> BULUNAN EN İYİ K DEĞERİ: " + bestK + " (Doğruluk: "
                            + String.format("%.4f", bestAccuracy) + ")");
                    knn.setK(bestK);
                    System.out.println("Sistem güncellendi. Artık yeni tahminler K=" + bestK + " üzerinden yapılacak.");
                    break;

                case "5":
                    System.out.println("Sistemden çıkılıyor. İyi günler!");
                    running = false;
                    break;

                default:
                    System.out.println("Geçersiz seçim! Lütfen 1-5 arası bir rakam giriniz.");
            }
        }
        scanner.close();
    }
}