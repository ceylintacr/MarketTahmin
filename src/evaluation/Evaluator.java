package evaluation;

import classifier.IClassifier;
import model.UserRecord;
import java.util.*;
import java.util.function.Supplier;

public class Evaluator {

    public DegerlendirmeSonucu degerlendir(IClassifier siniflandirici, List<UserRecord> testVerisi) {
        if (siniflandirici == null) {
            throw new IllegalArgumentException("Sınıflandırıcı null olamaz");
        }
        if (testVerisi == null || testVerisi.isEmpty()) {
            throw new IllegalArgumentException("Test verisi null veya boş olamaz");
        }

        List<String> gercekEtiketler = new ArrayList<>();
        List<String> tahminEdilenEtiketler = new ArrayList<>();

        for (UserRecord kayit : testVerisi) {
            gercekEtiketler.add(kayit.getCategory());
            tahminEdilenEtiketler.add(siniflandirici.predict(kayit));
        }

        return metrikleriHesapla(gercekEtiketler, tahminEdilenEtiketler);
    }

    public DegerlendirmeSonucu caprazDogrula(Supplier<IClassifier> siniflandiriciSaglayici, List<UserRecord> veri, int k) {
        if (k <= 1) {
            throw new IllegalArgumentException("k değeri 1'den büyük olmalıdır");
        }
        if (veri == null || veri.size() < k) {
            throw new IllegalArgumentException("K-fold çapraz doğrulama için yeterli veri yok");
        }

        List<UserRecord> karistirilmisVeri = new ArrayList<>(veri);
        Collections.shuffle(karistirilmisVeri);

        int katmanBoyutu = veri.size() / k;
        List<DegerlendirmeSonucu> katmanSonuclari = new ArrayList<>();

        for (int i = 0; i < k; i++) {
            List<UserRecord> testKatmani = new ArrayList<>();
            List<UserRecord> egitimKatmani = new ArrayList<>();

            for (int j = 0; j < veri.size(); j++) {
                if (j >= i * katmanBoyutu && j < (i + 1) * katmanBoyutu) {
                    testKatmani.add(karistirilmisVeri.get(j));
                } else {
                    egitimKatmani.add(karistirilmisVeri.get(j));
                }
            }

            IClassifier siniflandirici = siniflandiriciSaglayici.get(); 
            
            siniflandirici.egit(egitimKatmani);
            DegerlendirmeSonucu sonuc = degerlendir(siniflandirici, testKatmani);
            katmanSonuclari.add(sonuc);
        }

        return sonuclariOrtala(katmanSonuclari);
    }

    private DegerlendirmeSonucu metrikleriHesapla(List<String> gercekEtiketler, List<String> tahminEdilenEtiketler) {
        if (gercekEtiketler.size() != tahminEdilenEtiketler.size()) {
            throw new IllegalArgumentException("Gerçek ve tahmin edilen etiketlerin boyutları aynı olmalıdır");
        }

        Map<String, Map<String, Integer>> hataMatrisi = new HashMap<>();
        Set<String> tumEtiketler = new HashSet<>();
        tumEtiketler.addAll(gercekEtiketler);
        tumEtiketler.addAll(tahminEdilenEtiketler);

        for (String gercekEtiket : tumEtiketler) {
            hataMatrisi.put(gercekEtiket, new HashMap<>());
            for (String tahminEtiketi : tumEtiketler) {
                hataMatrisi.get(gercekEtiket).put(tahminEtiketi, 0);
            }
        }

        for (int i = 0; i < gercekEtiketler.size(); i++) {
            String gercekEtiket = gercekEtiketler.get(i);
            String tahminEtiketi = tahminEdilenEtiketler.get(i);
            hataMatrisi.get(gercekEtiket).put(tahminEtiketi,
                hataMatrisi.get(gercekEtiket).get(tahminEtiketi) + 1);
        }

        int toplam = gercekEtiketler.size();
        int dogru = 0;
        Map<String, Double> kesinlik = new HashMap<>();
        Map<String, Double> duyarlilik = new HashMap<>();
        Map<String, Double> f1Skoru = new HashMap<>();

        for (String etiket : tumEtiketler) {
            int dogruPozitifler = hataMatrisi.get(etiket).get(etiket);
            int yanlisPozitifler = 0;
            int yanlisNegatifler = 0;

            for (String digerEtiket : tumEtiketler) {
                if (!digerEtiket.equals(etiket)) {
                    yanlisPozitifler += hataMatrisi.get(digerEtiket).get(etiket);
                }
            }

            for (String digerEtiket : tumEtiketler) {
                if (!digerEtiket.equals(etiket)) {
                    yanlisNegatifler += hataMatrisi.get(etiket).get(digerEtiket);
                }
            }

            dogru += dogruPozitifler;

            double kes = (dogruPozitifler + yanlisPozitifler) == 0 ? 0.0 :
                (double) dogruPozitifler / (dogruPozitifler + yanlisPozitifler);
            double duy = (dogruPozitifler + yanlisNegatifler) == 0 ? 0.0 :
                (double) dogruPozitifler / (dogruPozitifler + yanlisNegatifler);
            double f1 = (kes + duy) == 0 ? 0.0 : 2 * kes * duy / (kes + duy);

            kesinlik.put(etiket, kes);
            duyarlilik.put(etiket, duy);
            f1Skoru.put(etiket, f1);
        }

        double dogruluk = (double) dogru / toplam;

        return new DegerlendirmeSonucu(dogruluk, kesinlik, duyarlilik, f1Skoru, hataMatrisi);
    }

    private DegerlendirmeSonucu sonuclariOrtala(List<DegerlendirmeSonucu> sonuclar) {
        if (sonuclar.isEmpty()) {
            throw new IllegalArgumentException("Sonuçlar listesi boş olamaz");
        }

        double ortDogruluk = sonuclar.stream().mapToDouble(s -> s.dogruluk).average().orElse(0.0);

        Set<String> tumEtiketler = new HashSet<>();
        for (DegerlendirmeSonucu sonuc : sonuclar) {
            tumEtiketler.addAll(sonuc.kesinlik.keySet());
        }

        Map<String, Double> ortKesinlik = new HashMap<>();
        Map<String, Double> ortDuyarlilik = new HashMap<>();
        Map<String, Double> ortF1Skoru = new HashMap<>();

        for (String etiket : tumEtiketler) {
            double kes = sonuclar.stream()
                .mapToDouble(s -> s.kesinlik.getOrDefault(etiket, 0.0))
                .average().orElse(0.0);
            double duy = sonuclar.stream()
                .mapToDouble(s -> s.duyarlilik.getOrDefault(etiket, 0.0))
                .average().orElse(0.0);
            double f1 = sonuclar.stream()
                .mapToDouble(s -> s.f1Skoru.getOrDefault(etiket, 0.0))
                .average().orElse(0.0);

            ortKesinlik.put(etiket, kes);
            ortDuyarlilik.put(etiket, duy);
            ortF1Skoru.put(etiket, f1);
        }

        Map<String, Map<String, Integer>> hataMatrisi = sonuclar.get(sonuclar.size() - 1).hataMatrisi;

        return new DegerlendirmeSonucu(ortDogruluk, ortKesinlik, ortDuyarlilik, ortF1Skoru, hataMatrisi);
    }

    public static class DegerlendirmeSonucu {
        public final double dogruluk;
        public final Map<String, Double> kesinlik;
        public final Map<String, Double> duyarlilik;
        public final Map<String, Double> f1Skoru;
        public final Map<String, Map<String, Integer>> hataMatrisi;

        public DegerlendirmeSonucu(double dogruluk, Map<String, Double> kesinlik,
                               Map<String, Double> duyarlilik, Map<String, Double> f1Skoru,
                               Map<String, Map<String, Integer>> hataMatrisi) {
            this.dogruluk = dogruluk;
            this.kesinlik = new HashMap<>(kesinlik);
            this.duyarlilik = new HashMap<>(duyarlilik);
            this.f1Skoru = new HashMap<>(f1Skoru);
            this.hataMatrisi = hataMatrisiniDerinKopyala(hataMatrisi);
        }

        private Map<String, Map<String, Integer>> hataMatrisiniDerinKopyala(
                Map<String, Map<String, Integer>> orijinal) {
            Map<String, Map<String, Integer>> kopya = new HashMap<>();
            for (Map.Entry<String, Map<String, Integer>> giris : orijinal.entrySet()) {
                kopya.put(giris.getKey(), new HashMap<>(giris.getValue()));
            }
            return kopya;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Doğruluk: %.4f%n", dogruluk));
            sb.append("Sınıf bazlı metrikler:\n");

            for (String etiket : kesinlik.keySet()) {
                sb.append(String.format("  %s - Kesinlik: %.4f, Duyarlılık: %.4f, F1: %.4f%n",
                    etiket,
                    kesinlik.get(etiket),
                    duyarlilik.get(etiket),
                    f1Skoru.get(etiket)));
            }
            return sb.toString();
        }
    }
}