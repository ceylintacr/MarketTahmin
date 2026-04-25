package model;

public class UserRecord {
    private String clientCode;
    private String gender;
    private double lineNetTotal;
    private String brand;
    private String category;

    public UserRecord(String clientCode, String gender, double lineNetTotal, String brand, String category) {
        this.clientCode = clientCode;
        this.gender = gender;
        this.lineNetTotal = lineNetTotal;
        this.brand = brand;
        this.category = category;
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getLineNetTotal() {
        return lineNetTotal;
    }

    public void setLineNetTotal(double lineNetTotal) {
        this.lineNetTotal = lineNetTotal;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "UserRecord{" +
                "clientCode='" + clientCode + '\'' +
                ", gender='" + gender + '\'' +
                ", lineNetTotal=" + lineNetTotal +
                ", brand='" + brand + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}