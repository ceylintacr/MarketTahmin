package classifier;

import model.UserRecord;
import java.util.List;

public interface IClassifier {

    void egit(List<UserRecord> egitimVerisi);

    String predict(UserRecord user);

    List<String> predict(List<UserRecord> users);

    String isimGetir();

    void calismaSuresiniHesapla(List<UserRecord> egitimVerisi);

    long getCalismaSuresi();
}