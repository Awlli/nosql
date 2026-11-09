package kz.atu.lab09;

import java.util.LinkedHashMap;
import java.util.List;

public final class CarRentalValidator {

    public static ValidationResult validate(
            String name, String email, String vin, String daysText,
            String carClass, boolean consent, List<CarRental> existingRentals) {

        var errors = new LinkedHashMap<String, String>();

        // 1. Проверка ФИО
        if (name == null || name.trim().length() < 2 || name.trim().length() > 80) {
            errors.put("name", "ФИО: от 2 до 80 символов");
        }

        // 2. Проверка Email
        if (email == null || !email.trim().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.put("email", "Введите корректный email");
        }

        // 3. Проверка уникального идентификатора (VIN-код)
        if (vin == null || vin.trim().length() != 7) {
            errors.put("vin", "VIN-код должен содержать ровно 7 символов");
        } else {
            // Проверка на уникальность в текущем списке
            boolean isDuplicate = existingRentals.stream()
                    .anyMatch(rental -> rental.getVin().equalsIgnoreCase(vin.trim()));
            if (isDuplicate) {
                errors.put("vin", "Автомобиль с таким VIN уже арендован!");
            }
        }

        // 4. Проверка срока аренды (число)
        try {
            int days = Integer.parseInt(daysText.trim());
            if (days < 1 || days > 30) {
                errors.put("days", "Срок аренды: от 1 до 30 дней");
            }
        } catch (NumberFormatException | NullPointerException e) {
            errors.put("days", "Введите целое число дней");
        }

        // 5. Проверка выбора класса машины
        if (carClass == null || carClass.isBlank()) {
            errors.put("carClass", "Выберите класс автомобиля");
        }

        // 6. Проверка согласия
        if (!consent) {
            errors.put("consent", "Необходимо согласие с правилами");
        }

        return new ValidationResult(errors);
    }
}
