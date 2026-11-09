package kz.atu.lab08;

import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;

/** Контроллер модального окна выбора цвета. */
public class ColorDialogController {

    @FXML private ColorPicker colorPicker;

    /** null — пользователь отменил выбор или закрыл окно крестиком. */
    private Color selectedColor;

    /** Вызывается главным контроллером после FXMLLoader.load(). */
    public void setInitialColor(Color color) {
        colorPicker.setValue(color);
    }

    public Optional<Color> getSelectedColor() {
        return Optional.ofNullable(selectedColor);
    }

    @FXML
    private void onApply() {
        selectedColor = colorPicker.getValue();
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        ((Stage) colorPicker.getScene().getWindow()).close();
    }
}
