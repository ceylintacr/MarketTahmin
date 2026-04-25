package data;

import model.UserRecord;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {

    private static final int COL_TOTAL_PRICE = 7;
    private static final int COL_BRAND = 11;
    private static final int COL_CATEGORY = 12;
    private static final int COL_CLIENT_CODE = 16;
    private static final int COL_GENDER = 17;

    public List<UserRecord> load(String filePath) {
        List<UserRecord> records = new ArrayList<>();
        File dataFile = new File(filePath);

        try (FileInputStream fis = new FileInputStream(dataFile);
                Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                try {
                    Cell totalPriceCell = row.getCell(COL_TOTAL_PRICE);
                    Cell brandCell = row.getCell(COL_BRAND);
                    Cell categoryCell = row.getCell(COL_CATEGORY);
                    Cell clientCodeCell = row.getCell(COL_CLIENT_CODE);
                    Cell genderCell = row.getCell(COL_GENDER);

                    if (totalPriceCell == null || brandCell == null || categoryCell == null ||
                            clientCodeCell == null || genderCell == null) {
                        continue;
                    }

                    double lineNetTotal = totalPriceCell.getNumericCellValue();
                    String brand = getCellStringValue(brandCell);
                    String category = getCellStringValue(categoryCell);
                    String clientCode = getCellStringValue(clientCodeCell);
                    String gender = getCellStringValue(genderCell);

                    records.add(new UserRecord(clientCode, gender, lineNetTotal, brand, category));
                } catch (Exception e) {
                    // Hatalı satırları atla
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
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
