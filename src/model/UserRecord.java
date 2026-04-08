package model;

import java.util.Objects;

public class UserRecord {

    // -------------------------------------------------------------------------
    // Fields — private + final (immutable after construction)
    // -------------------------------------------------------------------------

    private final int age;              // ← Yaş değişkeni eklendi
    private final String gender;
    private final String city;
    private final double totalPrice;
    private final String category;      // ← label / class

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new UserRecord.
     *
     * @param age        user's age (must be > 0)
     * @param gender     raw gender string  ("Male" | "Female" | "E" | "K")
     * @param city       raw city name
     * @param totalPrice purchase total (must be ≥ 0)
     * @param category   target class label (must not be null/blank)
     * @throws IllegalArgumentException if any argument violates its contract
     */
    public UserRecord(int age, String gender, String city, double totalPrice, String category) {

        // Basic validation — keeps bad data out of the pipeline early
        if (age <= 0) {
            throw new IllegalArgumentException("age must be > 0, got: " + age);
        }
        if (gender == null || gender.isBlank()) {
            throw new IllegalArgumentException("gender must not be null or blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("city must not be null or blank");
        }
        if (totalPrice < 0) {
            throw new IllegalArgumentException("totalPrice must be >= 0, got: " + totalPrice);
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category must not be null or blank");
        }

        this.age        = age;
        this.gender     = gender.trim();
        this.city       = city.trim();
        this.totalPrice = totalPrice;
        this.category   = category.trim();
    }

    // -------------------------------------------------------------------------
    // Getters (no setters — immutable by design)
    // -------------------------------------------------------------------------

    /** @return user's age */
    public int getAge() {
        return age;
    }

    /** @return raw gender string */
    public String getGender() {
        return gender;
    }

    /** @return raw city name */
    public String getCity() {
        return city;
    }

    /** @return purchase total price */
    public double getTotalPrice() {
        return totalPrice;
    }

    /** @return target class label */
    public String getCategory() {
        return category;
    }

    // -------------------------------------------------------------------------
    // Object overrides
    // -------------------------------------------------------------------------

    /**
     * Human-readable representation — useful for debugging / logging.
     */
    @Override
    public String toString() {
        return String.format(
            "UserRecord{age=%d, gender='%s', city='%s', totalPrice=%.2f, category='%s'}",
            age, gender, city, totalPrice, category
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserRecord that = (UserRecord) obj;
        return age == that.age &&
               Double.compare(that.totalPrice, totalPrice) == 0 &&
               Objects.equals(gender, that.gender) &&
               Objects.equals(city, that.city) &&
               Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(age, gender, city, totalPrice, category);
    }
}