package classifier;

import model.UserRecord;
import java.util.List;

public abstract class BaseAlgorithm implements IClassifier {

    protected long calismaSuresi = 0;

    public long getCalismaSuresi() {
        return calismaSuresi;
    }

    public void calismaSuresiniHesapla(List<UserRecord> veri) {
        long baslangicZamani = System.currentTimeMillis();

        this.egit(veri);

        long bitisZamani = System.currentTimeMillis();
        this.calismaSuresi = bitisZamani - baslangicZamani;
    }
}