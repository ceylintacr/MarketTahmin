package model;

import java.util.Objects;

public class UserRecord {

    // -------------------------------------------------------------------------
    // Fields — private + final (immutable after construction)
    // -------------------------------------------------------------------------

    private final int age;
    private final String city;
    private final String gender;
    private final double spendingScore;
    private final String category;      // target label

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new UserRecord.
     *
     * @param age          Yaş
     * @param city         Şehir
     * @param gender       Cinsiyet
     * @param spendingScore Harcama Skoru
     * @param category     Ürün Kategorisi
     */
    public UserRecord(int age, String city, String gender, double spendingScore, String category) {
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("city must not be null or blank");
        }
        if (gender == null || gender.isBlank()) {
            throw new IllegalArgumentException("gender must not be null or blank");
        }
        if (spendingScore < 0) {
            throw new IllegalArgumentException("spendingScore must be >= 0, got: " + spendingScore);
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category must not be null or blank");
        }

        this.age = age;
        this.city = city.trim();
        this.gender = gender.trim();
        this.spendingScore = spendingScore;
        this.category = category.trim();
    }

    // -------------------------------------------------------------------------
    // Getters (no setters — immutable by design)
    // -------------------------------------------------------------------------

    public int getAge() { return age; }
    public String getCity() { return city; }
    public String getGender() { return gender; }
    public double getSpendingScore() { return spendingScore; }
    public String getCategory() { return category; }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format(
            "UserRecord{age=%d, city='%s', gender='%s', spendingScore=%.2f, category='%s'}",
            age, city, gender, spendingScore, category
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserRecord that = (UserRecord) obj;
        return Double.compare(that.spendingScore, spendingScore) == 0 &&
               age == that.age &&
               Objects.equals(city, that.city) &&
               Objects.equals(gender, that.gender) &&
               Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, city, gender, spendingScore, category);
    }
}