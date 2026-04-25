package classifier;

import data.PreProcessor;
import model.ProcessedRecord;
import model.UserRecord;
import java.util.*;

public class KNNClassifier extends BaseAlgorithm {

    private List<ProcessedRecord> islenmisEgitimVerisi;
    private PreProcessor onIsleyici;
    private int k;

    public KNNClassifier(int k, PreProcessor onIsleyici) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        if (onIsleyici == null) {
            throw new IllegalArgumentException("PreProcessor cannot be null");
        }
        this.k = k;
        this.onIsleyici = onIsleyici;
    }

    @Override
    public void egit(List<UserRecord> hamEgitimVerisi) {
        if (hamEgitimVerisi == null || hamEgitimVerisi.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty");
        }

        // 1. DATA LEAKAGE FIX: Önce sadece eğitim verisiyle modeli (scaling, encoding)
        // FIT et.
        this.onIsleyici.fit(hamEgitimVerisi);

        // 2. Eğitim verilerini TRANSFORM et ve sınıflandırıcıya kaydet.
        this.islenmisEgitimVerisi = new ArrayList<>();
        for (UserRecord user : hamEgitimVerisi) {
            double[] ozellikler = this.onIsleyici.transform(user);
            this.islenmisEgitimVerisi.add(new ProcessedRecord(ozellikler, user.getCategory()));
        }

        System.out.println("[KNNClassifier] Trained on " + islenmisEgitimVerisi.size() + " records with k=" + k);
    }

    @Override
    public String predict(UserRecord user) {
        if (islenmisEgitimVerisi == null || onIsleyici == null) {
            throw new IllegalStateException("Classifier must be trained before prediction");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // Test verisini veya tekil müşteriyi mevcut eğitim istatistikleriyle TRANSFORM et.
        double[] ozellikler = onIsleyici.transform(user);

        List<Komsu> komsular = new ArrayList<>();
        for (ProcessedRecord record : islenmisEgitimVerisi) {
            double mesafe = oklidMesafesiHesapla(ozellikler, record.getFeatures());
            komsular.add(new Komsu(mesafe, record.getLabel()));
        }

        komsular.sort(Comparator.comparingDouble(n -> n.mesafe));

        Map<String, Integer> etiketSayilari = new HashMap<>();
        for (int i = 0; i < Math.min(k, komsular.size()); i++) {
            String etiket = komsular.get(i).etiket;
            etiketSayilari.put(etiket, etiketSayilari.getOrDefault(etiket, 0) + 1);
        }

        return etiketSayilari.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    @Override
    public List<String> predict(List<UserRecord> users) {
        List<String> tahminler = new ArrayList<>();
        for (UserRecord user : users) {
            tahminler.add(predict(user));
        }
        return tahminler;
    }

    @Override
    public String isimGetir() {
        return "K-Nearest Neighbors (k=" + k + ")";
    }

    private double oklidMesafesiHesapla(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Feature vectors must have same length");
        }
        double toplam = 0.0;
        for (int i = 0; i < a.length; i++) {
            double fark = a[i] - b[i];
            toplam += fark * fark;
        }
        return Math.sqrt(toplam);
    }

    private static class Komsu {
        final double mesafe;
        final String etiket;

        Komsu(double mesafe, String etiket) {
            this.mesafe = mesafe;
            this.etiket = etiket;
        }
    }

    public void setK(int k) {
        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }
        this.k = k;
    }

    public int getK() {
        return k;
    }
}