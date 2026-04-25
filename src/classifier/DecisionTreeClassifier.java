package classifier;

import data.PreProcessor;
import model.ProcessedRecord;
import model.UserRecord;
import java.util.*;

public class DecisionTreeClassifier extends BaseAlgorithm {

    private Dugum kokDugum;
    private PreProcessor onIsleyici;
    private int maksDerinlik;

    public DecisionTreeClassifier(PreProcessor onIsleyici, int maksDerinlik) {
        if (onIsleyici == null) {
            throw new IllegalArgumentException("PreProcessor cannot be null");
        }
        if (maksDerinlik <= 0) {
            throw new IllegalArgumentException("maksDerinlik must be positive");
        }
        this.onIsleyici = onIsleyici;
        this.maksDerinlik = maksDerinlik;
    }

    @Override
    public void egit(List<UserRecord> hamEgitimVerisi) {
        if (hamEgitimVerisi == null || hamEgitimVerisi.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty");
        }

        this.onIsleyici.fit(hamEgitimVerisi);

        List<ProcessedRecord> islenmisVeri = new ArrayList<>();
        for (UserRecord user : hamEgitimVerisi) {
            double[] ozellikler = this.onIsleyici.transform(user);
            islenmisVeri.add(new ProcessedRecord(ozellikler, user.getCategory()));
        }

        kokDugum = agacOlusturma(islenmisVeri, 0);
    }

    @Override
    public String predict(UserRecord user) {
        if (kokDugum == null || onIsleyici == null) {
            throw new IllegalStateException("Classifier must be trained before prediction");
        }

        // Yeni gelen kullanıcıyı (test) transform et.
        double[] ozellikler = onIsleyici.transform(user);
        return tahminDongusu(kokDugum, ozellikler);
    }

    @Override
    public List<String> predict(List<UserRecord> users) {
        List<String> sonuclar = new ArrayList<>();
        for (UserRecord user : users) {
            sonuclar.add(predict(user));
        }
        return sonuclar;
    }

    @Override
    public String isimGetir() {
        return "Decision Tree (Gini)";
    }

    private String tahminDongusu(Dugum dugum, double[] ozellikler) {
        if (dugum.etiket != null)
            return dugum.etiket;

        if (ozellikler[dugum.ozellikIndeksi] < dugum.esikDegeri)
            return tahminDongusu(dugum.solDugum, ozellikler);
        else
            return tahminDongusu(dugum.sagDugum, ozellikler);
    }

    private Dugum agacOlusturma(List<ProcessedRecord> veri, int derinlik) {
        if (veri.isEmpty())
            return null;

        if (derinlik >= maksDerinlik || safMi(veri)) {
            return new Dugum(cogunluguAl(veri));
        }

        int ozellikSayisi = veri.get(0).getFeatures().length;
        double enIyiGini = Double.MAX_VALUE;
        int enIyiOzellik = -1;
        double enIyiEsikDegeri = 0;

        for (int i = 0; i < ozellikSayisi; i++) {
            List<Double> degerler = new ArrayList<>();
            for (ProcessedRecord r : veri) {
                degerler.add(r.getFeatures()[i]);
            }
            Collections.sort(degerler);

            for (int j = 1; j < degerler.size(); j++) {
                if (degerler.get(j).equals(degerler.get(j - 1)))
                    continue;

                double esikDegeri = (degerler.get(j - 1) + degerler.get(j)) / 2;
                double giniSkoru = bolunmeGiniHesapla(veri, i, esikDegeri);

                if (giniSkoru < enIyiGini) {
                    enIyiGini = giniSkoru;
                    enIyiOzellik = i;
                    enIyiEsikDegeri = esikDegeri;
                }
            }
        }

        if (enIyiOzellik == -1) {
            return new Dugum(cogunluguAl(veri));
        }

        List<ProcessedRecord> solVeri = new ArrayList<>();
        List<ProcessedRecord> sagVeri = new ArrayList<>();

        for (ProcessedRecord r : veri) {
            if (r.getFeatures()[enIyiOzellik] < enIyiEsikDegeri)
                solVeri.add(r);
            else
                sagVeri.add(r);
        }

        if (solVeri.isEmpty() || sagVeri.isEmpty()) {
            return new Dugum(cogunluguAl(veri));
        }

        Dugum dugum = new Dugum(enIyiOzellik, enIyiEsikDegeri);
        dugum.solDugum = agacOlusturma(solVeri, derinlik + 1);
        dugum.sagDugum = agacOlusturma(sagVeri, derinlik + 1);

        return dugum;
    }

    private double bolunmeGiniHesapla(List<ProcessedRecord> veri, int ozellik, double esikDegeri) {
        Map<String, Integer> solGrup = new HashMap<>();
        Map<String, Integer> sagGrup = new HashMap<>();
        int solSayi = 0;
        int sagSayi = 0;

        for (ProcessedRecord r : veri) {
            if (r.getFeatures()[ozellik] < esikDegeri) {
                solGrup.put(r.getLabel(), solGrup.getOrDefault(r.getLabel(), 0) + 1);
                solSayi++;
            } else {
                sagGrup.put(r.getLabel(), sagGrup.getOrDefault(r.getLabel(), 0) + 1);
                sagSayi++;
            }
        }

        int toplam = veri.size();
        double solAgirlik = (double) solSayi / toplam;
        double sagAgirlik = (double) sagSayi / toplam;

        return (solAgirlik * giniHesapla(solGrup)) + (sagAgirlik * giniHesapla(sagGrup));
    }

    private double giniHesapla(Map<String, Integer> harita) {
        int toplam = harita.values().stream().mapToInt(i -> i).sum();
        if (toplam == 0)
            return 0;

        double toplamGini = 0;
        for (int sayi : harita.values()) {
            double olasilik = (double) sayi / toplam;
            toplamGini += olasilik * olasilik;
        }

        return 1 - toplamGini;
    }

    private boolean safMi(List<ProcessedRecord> veri) {
        String ilkEtiket = veri.get(0).getLabel();
        for (ProcessedRecord r : veri) {
            if (!r.getLabel().equals(ilkEtiket))
                return false;
        }
        return true;
    }

    private String cogunluguAl(List<ProcessedRecord> veri) {
        Map<String, Integer> harita = new HashMap<>();
        for (ProcessedRecord r : veri) {
            harita.put(r.getLabel(), harita.getOrDefault(r.getLabel(), 0) + 1);
        }
        return harita.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();
    }

    static class Dugum {
        int ozellikIndeksi;
        double esikDegeri;
        Dugum solDugum, sagDugum;
        String etiket;

        Dugum(String etiket) {
            this.etiket = etiket;
        }

        Dugum(int ozellikIndeksi, double esikDegeri) {
            this.ozellikIndeksi = ozellikIndeksi;
            this.esikDegeri = esikDegeri;
        }
    }
}