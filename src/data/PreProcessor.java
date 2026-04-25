package data;

import model.UserRecord;
import java.util.*;

public class PreProcessor {
    private Map<String, Integer> brandIndexMap = new HashMap<>();
    private double minTotal = Double.MAX_VALUE;
    private double maxTotal = -Double.MAX_VALUE;
    private boolean hazirMi = false;

    public void egit(List<UserRecord> veri) {
        if (veri == null || veri.isEmpty())
            return;

        brandIndexMap.clear();
        minTotal = Double.MAX_VALUE;
        maxTotal = -Double.MAX_VALUE;

        for (UserRecord record : veri) {
            double total = record.getLineNetTotal();
            if (total < minTotal)
                minTotal = total;
            if (total > maxTotal)
                maxTotal = total;

            String brand = record.getBrand();
            if (!brandIndexMap.containsKey(brand)) {
                brandIndexMap.put(brand, brandIndexMap.size());
            }
        }
        hazirMi = true;
    }

    public double[] donustur(UserRecord kayit) {
        if (!hazirMi) {
            throw new IllegalStateException("PreProcessor must be trained with egit() before transformation.");
        }

        double genderEncoded = kayit.getGender().equalsIgnoreCase("Male") ? 1.0 : 0.0;

        double normalizedTotal = 0.0;
        if (maxTotal - minTotal != 0) {
            normalizedTotal = (kayit.getLineNetTotal() - minTotal) / (maxTotal - minTotal);
        }

        double[] brandOneHot = new double[brandIndexMap.size()];
        Integer index = brandIndexMap.get(kayit.getBrand());
        if (index != null) {
            brandOneHot[index] = 1.0;
        }

        double[] features = new double[2 + brandOneHot.length];
        features[0] = genderEncoded;
        features[1] = normalizedTotal;
        System.arraycopy(brandOneHot, 0, features, 2, brandOneHot.length);

        return features;
    }

    public int getFeatureCount() {
        return 2 + brandIndexMap.size();
    }
}