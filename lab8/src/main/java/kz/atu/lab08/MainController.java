package kz.atu.lab08;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

public class MainController {

    private static final String APP_TITLE = "Редактор заметок";

    @FXML private TextArea txtNotes;
    @FXML private Label lblStatus;

    private Color currentColor = Color.BLACK;
    private File currentFile;          // null — файл ещё не выбран
    private boolean dirty;             // есть несохранённые изменения
    private Stage helpStage;           // немодальное окно справки

    @FXML
    private void initialize() {
        txtNotes.textProperty().addListener((obs, oldText, newText) -> {
            dirty = true;
            updateTitle();
        });
    }

    // ---------------------------------------------------------------- файл

    @FXML
    private void onOpen() {
        if (!confirmDiscardChanges()) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Открыть заметку");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
        File file = chooser.showOpenDialog(window());
        if (file == null) return;                 // отмена — ничего не меняем

        try {
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            txtNotes.setText(text);
            currentFile = file;
            dirty = false;                        // setText сработал как «изменение»
            updateTitle();
            lblStatus.setText("Открыт: " + file.getName());
        } catch (IOException ex) {
            showError(ex.getMessage());
        }
    }

    /** «Сохранить»: без диалога, если путь уже известен. */
    @FXML
    private void onSave() {
        save();
    }

    @FXML
    private void onSaveAs() {
        saveAs();
    }

    private boolean save() {
        return currentFile == null ? saveAs() : writeTo(currentFile);
    }

    private boolean saveAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить заметку");
        chooser.setInitialFileName(currentFile != null ? currentFile.getName() : "note.txt");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
        File file = chooser.showSaveDialog(window());
        if (file == null) return false;           // отмена
        return writeTo(file);
    }

    private boolean writeTo(File file) {
        try {
            Files.writeString(file.toPath(), txtNotes.getText(), StandardCharsets.UTF_8);
            currentFile = file;
            dirty = false;
            updateTitle();
            lblStatus.setText("Сохранён: " + file.getName());
            return true;
        } catch (IOException ex) {
            showError(ex.getMessage());
            return false;
        }
    }

    @FXML
    private void onExit() {
        if (confirmDiscardChanges()) {
            Platform.exit();
        }
    }

    /**
     * Спрашивает, что делать с несохранёнными изменениями.
     * @return true — можно продолжать (закрывать/открывать), false — пользователь отменил.
     */
    public boolean confirmDiscardChanges() {
        if (!dirty) return true;

        ButtonType save = new ButtonType("Сохранить", ButtonBar.ButtonData.YES);
        ButtonType discard = new ButtonType("Не сохранять", ButtonBar.ButtonData.NO);
        ButtonType cancel = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Есть несохранённые изменения. Сохранить их?", save, discard, cancel);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(null);
        alert.initOwner(window());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() == cancel) return false;
        if (result.get() == save) return save();  // false, если сохранение не удалось/отменено
        return true;                              // «Не сохранять»
    }

    // ------------------------------------------------- модальное окно цвета

    @FXML
    private void onChooseColor() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("color-dialog.fxml"));
        Parent root = loader.load();
        ColorDialogController controller = loader.getController();
        controller.setInitialColor(currentColor);       // передача данных ДО показа

        Stage dialog = new Stage();
        dialog.initOwner(window());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Цвет текста");
        dialog.setResizable(false);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();                           // блокирует главное окно

        // Результат возвращается через Optional: пусто = отмена/крестик
        controller.getSelectedColor().ifPresent(color -> {
            currentColor = color;
            txtNotes.setStyle("-fx-text-fill: " + toCss(color) + ";");
            lblStatus.setText("Цвет текста изменён: " + toCss(color));
        });
    }

    private String toCss(Color c) {
        return String.format("#%02X%02X%02X",
                Math.round(c.getRed() * 255),
                Math.round(c.getGreen() * 255),
                Math.round(c.getBlue() * 255));
    }

    // ------------------------------------------------------------- справка

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText(APP_TITLE);
        alert.setContentText("Лабораторная работа №8. JavaFX, FXML, FileChooser, ColorPicker.\n"
                + "Версия 1.0");
        alert.initOwner(window());
        alert.showAndWait();
    }

    /** Немодальное окно: главное окно остаётся доступным. */
    @FXML
    private void onHelp() {
        if (helpStage != null && helpStage.isShowing()) {
            helpStage.toFront();
            return;
        }
        TextArea help = new TextArea("""
                Горячие клавиши:
                Ctrl+O — открыть файл
                Ctrl+S — сохранить
                Ctrl+Shift+S — сохранить как
                Ctrl+Q — выход

                Настройки → Цвет текста открывает модальное окно.
                Это окно справки — немодальное: можно работать в главном окне.
                """);
        help.setEditable(false);
        help.setWrapText(true);

        StackPane pane = new StackPane(help);
        pane.setPadding(new Insets(10));

        helpStage = new Stage();
        helpStage.initOwner(window());
        helpStage.initModality(Modality.NONE);
        helpStage.setTitle("Справка");
        helpStage.setScene(new Scene(pane, 420, 260));
        helpStage.show();                               // show(), а не showAndWait()
    }

    // ------------------------------------------------------------ служебное

    private void showError(String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Ошибка ввода-вывода");
        a.setHeaderText("Не удалось выполнить операцию");
        a.setContentText(message == null ? "Неизвестная ошибка" : message);
        a.initOwner(window());
        a.showAndWait();
    }

    private Window window() {
        return txtNotes.getScene() == null ? null : txtNotes.getScene().getWindow();
    }

    private void updateTitle() {
        if (window() instanceof Stage stage) {
            stage.setTitle(APP_TITLE
                    + (currentFile != null ? " — " + currentFile.getName() : "")
                    + (dirty ? " *" : ""));
        }
    }
}
