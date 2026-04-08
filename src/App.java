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

        PreProcessor processor = new PreProcessor();
        List<ProcessedRecord> data = processor.process(rawData);

        Collections.shuffle(data, new Random(42));
        int split = (int) (data.size() * 0.8);
        List<ProcessedRecord> train = data.subList(0, split);
        List<ProcessedRecord> test = data.subList(split, data.size());

        KNNClassifier knn = new KNNClassifier(5);
        DecisionTreeClassifier dt = new DecisionTreeClassifier();
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
                    
                    // YAŞ GİRDİSİ EKLENDİ
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

                    // transformSingleRecord artık 4 parametre alıyor (sonuna age eklendi)
                    double[] newFeatures = processor.transformSingleRecord(gender, city, price, age);

                    String knnPrediction = knn.predict(newFeatures);
                    String dtPrediction = dt.predict(newFeatures);

                    System.out.println("\n===== TAHMİN SONUÇLARI =====");
                    System.out.println("KNN (K=" + knn.getK() + ") Tahmini : Ürün Kategorisi -> " + knnPrediction);
                    System.out.println("Karar Ağacı Tahmini   : Ürün Kategorisi -> " + dtPrediction);
                    System.out.println("============================");
                    break;

                case "3":
                    System.out.println("\nVeri seti 5 parçaya bölünüyor ve Çapraz Doğrulama başlatılıyor...");
                    int kFold = 5;

                    System.out.println("\n--- KNN (" + kFold + "-Fold, K=" + knn.getK() + ") Ortalama Sonuçları ---");
                    Evaluator.EvaluationResult cvKnn = evaluator.crossValidate(knn, data, kFold);
                    System.out.println(cvKnn);

                    System.out.println("--- Karar Ağacı (" + kFold + "-Fold) Ortalama Sonuçları ---");
                    Evaluator.EvaluationResult cvDt = evaluator.crossValidate(dt, data, kFold);
                    System.out.println(cvDt);
                    break;

                case "4":
                    System.out.println("\n--- KNN İçin En İyi K Değeri Aranıyor (K=1 ile 15 arası) ---");
                    int bestK = 1;
                    double bestAccuracy = 0.0;

                    // 1, 3, 5, 7, 9, 11, 13, 15 değerlerini dene
                    for (int k = 1; k <= 15; k += 2) {
                        knn.setK(k);
                        // K-Fold kullanarak en gerçekçi doğruluğu bul
                        Evaluator.EvaluationResult res = evaluator.crossValidate(knn, data, 5);
                        System.out.printf("K = %2d deneniyor... Doğruluk: %.4f\n", k, res.accuracy);

                        if (res.accuracy > bestAccuracy) {
                            bestAccuracy = res.accuracy;
                            bestK = k;
                        }
                    }

                    System.out.println("\n=> BULUNAN EN İYİ K DEĞERİ: " + bestK + " (Doğruluk: "
                            + String.format("%.4f", bestAccuracy) + ")");
                    knn.setK(bestK); // Sistemi kalıcı olarak en iyi K değerine ayarla
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