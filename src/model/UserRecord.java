package model;

import java.util.Objects;

public class UserRecord {

    // -------------------------------------------------------------------------
    // Fields — private + final (immutable after construction)
    // -------------------------------------------------------------------------

    private final String clientCode;
    private final String gender;
    private final String brand;
    private final double lineNetTotal;
    private final String category;      // target label

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new UserRecord.
     *
     * @param clientCode   Müşteri ID
     * @param gender       Cinsiyet ("Male" | "Female" | "E" | "K")
     * @param brand        Marka
     * @param lineNetTotal Toplam Ücret (must be ≥ 0)
     * @param category     Ürün Kategorisi (must not be null/blank)
     * @throws IllegalArgumentException if any argument violates its contract
     */
    public UserRecord(String clientCode, String gender, String brand, double lineNetTotal, String category) {
        if (clientCode == null || clientCode.isBlank()) {
            throw new IllegalArgumentException("clientCode must not be null or blank");
        }
        if (gender == null || gender.isBlank()) {
            throw new IllegalArgumentException("gender must not be null or blank");
        }
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("brand must not be null or blank");
        }
        if (lineNetTotal < 0) {
            throw new IllegalArgumentException("lineNetTotal must be >= 0, got: " + lineNetTotal);
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category must not be null or blank");
        }

        this.clientCode   = clientCode.trim();
        this.gender       = gender.trim();
        this.brand        = brand.trim();
        this.lineNetTotal = lineNetTotal;
        this.category     = category.trim();
    }

    // -------------------------------------------------------------------------
    // Getters (no setters — immutable by design)
    // -------------------------------------------------------------------------

    public String getClientCode() { return clientCode; }
    public String getGender() { return gender; }
    public String getBrand() { return brand; }
    public double getLineNetTotal() { return lineNetTotal; }
    public String getCategory() { return category; }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format(
            "UserRecord{clientCode='%s', gender='%s', brand='%s', lineNetTotal=%.2f, category='%s'}",
            clientCode, gender, brand, lineNetTotal, category
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserRecord that = (UserRecord) obj;
        return Double.compare(that.lineNetTotal, lineNetTotal) == 0 &&
               Objects.equals(clientCode, that.clientCode) &&
               Objects.equals(gender, that.gender) &&
               Objects.equals(brand, that.brand) &&
               Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientCode, gender, brand, lineNetTotal, category);
    }
}