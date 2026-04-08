package data;

import model.UserRecord;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class DataLoader {

    // Kendi Excel'ine göre doğru kolon numaralarını (index) kontrol etmelisin
    private static final int COL_AGE         = 16; // YAŞ KOLONUNU BURAYA EKLEDİM (Örn: Q sütunuysa 16 olabilir, kontrol et)
    private static final int COL_TOTAL_PRICE = 7;  // LINENETTOTAL
    private static final int COL_BRAND       = 11; // Şehir olarak kullanılıyor
    private static final int COL_CATEGORY    = 12; // CATEGORY_NAME1
    private static final int COL_GENDER      = 17; // K / E

    public List<UserRecord> load(String filePath) {
        List<UserRecord> records = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(filePath)) {
            List<String> sharedStrings = readSharedStrings(zipFile);
            Document sheet = readXML(zipFile, "xl/worksheets/sheet1.xml");
            NodeList rows = sheet.getElementsByTagName("row");

            for (int i = 1; i < rows.getLength(); i++) { // header skip
                Element row = (Element) rows.item(i);
                Map<Integer, String> cells = getCells(row, sharedStrings);

                try {
                    String ageStr   = cells.get(COL_AGE); // Yaş verisi çekildi
                    String gender   = cells.get(COL_GENDER);
                    String brand    = cells.get(COL_BRAND);
                    String priceStr = cells.get(COL_TOTAL_PRICE);
                    String category = cells.get(COL_CATEGORY);

                    // Eğer herhangi biri null ise (ageStr eklendi) atla
                    if (ageStr == null || gender == null || brand == null || priceStr == null || category == null)
                        continue;

                    // String olan yaşı Integer'a çevir
                    int age = (int) Double.parseDouble(ageStr); 
                    double totalPrice = Double.parseDouble(priceStr);

                    // UserRecord artık 5 parametre bekliyor (en başta age var)
                    records.add(new UserRecord(age, gender, brand, totalPrice, category));

                } catch (Exception e) {
                    // hatalı satırı at
                }
            }
            System.out.println("Loaded records: " + records.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return records;
    }
    // ---------------- HELPER ----------------

    private Map<Integer, String> getCells(Element row, List<String> sharedStrings) {

        Map<Integer, String> map = new HashMap<>();
        NodeList cells = row.getElementsByTagName("c");

        for (int i = 0; i < cells.getLength(); i++) {

            Element cell = (Element) cells.item(i);
            String ref = cell.getAttribute("r");
            String type = cell.getAttribute("t");

            NodeList vList = cell.getElementsByTagName("v");
            if (vList.getLength() == 0) continue;

            String value = vList.item(0).getTextContent();
            int col = getColumnIndex(ref);

            if ("s".equals(type)) {
                int idx = Integer.parseInt(value);
                value = sharedStrings.get(idx);
            }

            map.put(col, value);
        }

        return map;
    }

    private int getColumnIndex(String ref) {
        String letters = ref.replaceAll("[^A-Z]", "");
        int result = 0;

        for (int i = 0; i < letters.length(); i++) {
            result = result * 26 + (letters.charAt(i) - 'A' + 1);
        }

        return result - 1;
    }

    private List<String> readSharedStrings(ZipFile zipFile) throws Exception {

        List<String> list = new ArrayList<>();

        ZipEntry entry = zipFile.getEntry("xl/sharedStrings.xml");
        if (entry == null) return list;

        Document doc = readXML(zipFile, "xl/sharedStrings.xml");
        NodeList nodes = doc.getElementsByTagName("si");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element si = (Element) nodes.item(i);
            NodeList tList = si.getElementsByTagName("t");

            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < tList.getLength(); j++) {
                sb.append(tList.item(j).getTextContent());
            }

            list.add(sb.toString());
        }

        return list;
    }

    private Document readXML(ZipFile zipFile, String path) throws Exception {

        ZipEntry entry = zipFile.getEntry(path);
        InputStream is = zipFile.getInputStream(entry);

        return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(is);
    }
}