package kz.atu.lab09;

public class CarRental {
    private final String fullName;
    private final String email;
    private final String vin;
    private final int days;
    private final String carClass;

    public CarRental(String fullName, String email, String vin, int days, String carClass) {
        this.fullName = fullName;
        this.email = email;
        this.vin = vin;
        this.days = days;
        this.carClass = carClass;
    }

    // Геттеры необходимы для TableView
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getVin() { return vin; }
    public int getDays() { return days; }
    public String getCarClass() { return carClass; }
}
