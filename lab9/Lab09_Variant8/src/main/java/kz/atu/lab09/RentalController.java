package kz.atu.lab09;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RentalController {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtVin;
    @FXML private TextField txtDays;
    @FXML private ComboBox<String> cmbClass;
    @FXML private CheckBox chkConsent;

    @FXML private Label lblNameError;
    @FXML private Label lblEmailError;
    @FXML private Label lblVinError;
    @FXML private Label lblDaysError;
    @FXML private Label lblClassError;
    @FXML private Label lblConsentError;

    @FXML private TableView<CarRental> tblRentals;
    @FXML private TableColumn<CarRental, String> colName;
    @FXML private TableColumn<CarRental, String> colEmail;
    @FXML private TableColumn<CarRental, String> colVin;
    @FXML private TableColumn<CarRental, Integer> colDays;
    @FXML private TableColumn<CarRental, String> colClass;

    private final ObservableList<CarRental> rentalsList = FXCollections.observableArrayList();
    private final PseudoClass invalidPseudoClass = PseudoClass.getPseudoClass("invalid");

    @FXML
    public void initialize() {
        // Привязка колонок таблицы к свойствам CarRental
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colVin.setCellValueFactory(new PropertyValueFactory<>("vin"));
        colDays.setCellValueFactory(new PropertyValueFactory<>("days"));
        colClass.setCellValueFactory(new PropertyValueFactory<>("carClass"));

        tblRentals.setItems(rentalsList);

        // Ограничение ввода TextFormatter (только цифры до 2 знаков для дней аренды)
        txtDays.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d{0,2}") ? change : null
        ));
    }

    @FXML
    private void onRegister() {
        // Валидация полей
        ValidationResult result = CarRentalValidator.validate(
                txtName.getText(), txtEmail.getText(), txtVin.getText(),
                txtDays.getText(), cmbClass.getValue(), chkConsent.isSelected(), rentalsList
        );

        showErrors(result);

        if (!result.isValid()) {
            new Alert(Alert.AlertType.WARNING, "Исправьте ошибки в форме проката!").showAndWait();
            return;
        }

        try {
            // Безопасное создание объекта и добавление в таблицу
            CarRental newRental = new CarRental(
                    txtName.getText().trim(),
                    txtEmail.getText().trim(),
                    txtVin.getText().trim().toUpperCase(),
                    Integer.parseInt(txtDays.getText().trim()),
                    cmbClass.getValue()
            );
            rentalsList.add(newRental);
            clearForm();
        } catch (RuntimeException e) {
            new Alert(Alert.AlertType.ERROR, "Не удалось завершить оформление аренды").showAndWait();
        }
    }

    private void showErrors(ValidationResult r) {
        // Отображение текстовых сообщений
        lblNameError.setText(r.error("name"));
        lblEmailError.setText(r.error("email"));
        lblVinError.setText(r.error("vin"));
        lblDaysError.setText(r.error("days"));
        lblClassError.setText(r.error("carClass"));
        lblConsentError.setText(r.error("consent"));

        // Переключение CSS-псевдоклассов для визуальной подсветки рамок
        txtName.pseudoClassStateChanged(invalidPseudoClass, !r.error("name").isEmpty());
        txtEmail.pseudoClassStateChanged(invalidPseudoClass, !r.error("email").isEmpty());
        txtVin.pseudoClassStateChanged(invalidPseudoClass, !r.error("vin").isEmpty());
        txtDays.pseudoClassStateChanged(invalidPseudoClass, !r.error("days").isEmpty());
    }

    @FXML
    private void clearForm() {
        txtName.clear();
        txtEmail.clear();
        txtVin.clear();
        txtDays.clear();
        cmbClass.setValue(null);
        chkConsent.setSelected(false);

        // Сброс ошибок
        showErrors(new ValidationResult(java.util.Map.of()));
    }

    @FXML
    private void onSaveToFile() {
        StringBuilder sb = new StringBuilder("ФИО;Email;VIN;Дни;Класс\n");
        for (CarRental r : rentalsList) {
            sb.append(r.getFullName()).append(";")
                    .append(r.getEmail()).append(";")
                    .append(r.getVin()).append(";")
                    .append(r.getDays()).append(";")
                    .append(r.getCarClass()).append("\n");
        }

        // Безопасная обработка исключений при работе с файлами (try-with-resources)
        try {
            Files.writeString(Path.of("rentals.csv"), sb.toString());
            new Alert(Alert.AlertType.INFORMATION, "Данные успешно сохранены в файл 'rentals.csv'!").showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Ошибка сохранения файла: " + e.getMessage()).showAndWait();
        }
    }
}
