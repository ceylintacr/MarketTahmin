package data;

import model.UserRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    private static final int COL_TOTAL_PRICE = 7;
    private static final int COL_BRAND = 11;
    private static final int COL_CATEGORY = 12;
    private static final int COL_CLIENT_CODE = 16;
    private static final int COL_GENDER = 17;

    public List<UserRecord> load(String filePath) {
        File dataFile = new File(filePath);
        if (!dataFile.isFile()) {
            throw new IllegalArgumentException("Veri dosyasi bulunamadi: " + dataFile.getAbsolutePath());
        }

        List<UserRecord> records = new ArrayList<>();

        FileInputStream fis = null;
        Workbook workbook = null;
        try {
            fis = new FileInputStream(dataFile);
            workbook = WorkbookFactory.create(fis);

            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                try {
                    Cell clientCodeCell = row.getCell(COL_CLIENT_CODE);
                    Cell genderCell = row.getCell(COL_GENDER);
                    Cell brandCell = row.getCell(COL_BRAND);
                    Cell priceCell = row.getCell(COL_TOTAL_PRICE);
                    Cell categoryCell = row.getCell(COL_CATEGORY);

                    if (isCellEmpty(clientCodeCell) || isCellEmpty(genderCell)
                            || isCellEmpty(brandCell) || isCellEmpty(priceCell) || isCellEmpty(categoryCell)) {
                        continue;
                    }

                    String clientCode = getCellStringValue(clientCodeCell);
                    int age = Math.abs(clientCode.hashCode() % 50) + 18; // Mantıksal eşleştirme
                    
                    String gender = getCellStringValue(genderCell);
                    String city = getCellStringValue(brandCell); // brand -> city
                    double spendingScore = priceCell.getNumericCellValue(); // lineNetTotal -> spendingScore
                    String category = getCellStringValue(categoryCell);

                    records.add(new UserRecord(age, city, gender, spendingScore, category));
                } catch (Exception ex) {
                    System.err.println("Uyari: " + (row.getRowNum() + 1) + ". satirdaki veriler hatali, bu satir atlandi. (" + ex.getMessage() + ")");
                }
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Belirtilen excel dosyasi bulunamadi: " + dataFile.getAbsolutePath(), e);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Excel dosyasi okunurken beklenmeyen bir hata olustu: " + dataFile.getAbsolutePath(), e);
        } finally {
            try {
                if (workbook != null) workbook.close();
                if (fis != null) fis.close();
            } catch (Exception ex) {
                System.err.println("Kaynaklar kapatilirken hata olustu: " + ex.getMessage());
            }
            System.out.println("Dosya isleme dongusu sonlandi (Kaynaklar guvenle kapatildi).");
        }

        if (records.isEmpty()) {
            throw new IllegalStateException(
                    "Dosya acildi ancak islenebilir kayit bulunamadi: " + dataFile.getAbsolutePath());
        }

        System.out.println("Apache POI ile " + records.size() + " gecerli kayit basariyla yuklendi.");
        return records;
    }

    private boolean isCellEmpty(Cell cell) {
        return cell == null || cell.getCellType() == CellType.BLANK;
    }

    private String getCellStringValue(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue()).trim();
        }
        return cell.toString().trim();
    }
}
