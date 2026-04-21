package data;

import model.UserRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    // Excel sütun indeksleri (Sıfırdan başlar: H=7, L=11, M=12, Q=16, R=17)
    private static final int COL_TOTAL_PRICE = 7;  // LINENETTOTAL
    private static final int COL_BRAND       = 11; // Şehir/Marka proxy'si
    private static final int COL_CATEGORY    = 12; // CATEGORY_NAME1 (Hedef Sınıf)
    private static final int COL_AGE         = 16; // Yaş
    private static final int COL_GENDER      = 17; // Cinsiyet

    /**
     * Apache POI kullanarak Excel dosyasını okur ve UserRecord listesine dönüştürür.
     */
    public List<UserRecord> load(String filePath) {
        List<UserRecord> records = new ArrayList<>();

        // FileInputStream ve Workbook'u try-with-resources ile açıyoruz (Otomatik kapanır)
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            // İlk çalışma sayfasını al
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                // Başlık (Header) satırını atla
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                try {
                    // İlgili hücreleri al
                    Cell ageCell = row.getCell(COL_AGE);
                    Cell genderCell = row.getCell(COL_GENDER);
                    Cell brandCell = row.getCell(COL_BRAND);
                    Cell priceCell = row.getCell(COL_TOTAL_PRICE);
                    Cell categoryCell = row.getCell(COL_CATEGORY);

                    // Veri eksiği (boş hücre) olan satırları güvenle atla
                    if (isCellEmpty(ageCell) || isCellEmpty(genderCell) || 
                        isCellEmpty(brandCell) || isCellEmpty(priceCell) || isCellEmpty(categoryCell)) {
                        continue;
                    }

                    // POI üzerinden veri tiplerini güvenli bir şekilde çekiyoruz
                    // Excel'de sayılar genellikle Numeric, metinler String olarak saklanır
                    int age = (int) ageCell.getNumericCellValue();
                    String gender = getCellStringValue(genderCell);
                    String brand = getCellStringValue(brandCell);
                    double totalPrice = priceCell.getNumericCellValue();
                    String category = getCellStringValue(categoryCell);

                    // Modele ekle
                    records.add(new UserRecord(age, gender, brand, totalPrice, category));

                } catch (Exception e) {
                    // Tekil satırlardaki format hatalarında programın çökmesini engelle
                    // Örneğin sayı gelmesi gereken yerde metin varsa o satırı yoksayar
                }
            }
            System.out.println("Apache POI ile " + records.size() + " geçerli kayıt başarıyla yüklendi.");

        } catch (Exception e) {
            System.err.println("Kritik Hata: Excel dosyası okunurken bir sorun oluştu!");
            e.printStackTrace();
        }

        return records;
    }

    /**
     * Hücrenin boş (blank) veya null olup olmadığını kontrol eder.
     */
    private boolean isCellEmpty(Cell cell) {
        return cell == null || cell.getCellType() == CellType.BLANK;
    }

    /**
     * Hücre içeriğini tipine bakmaksızın String olarak döndürür.
     */
    private String getCellStringValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            // Numeric hücreyi metne çevirirken ondalık kısmı atıyoruz
            return String.valueOf((int) cell.getNumericCellValue()).trim();
        }
        return cell.toString().trim();
    }
}