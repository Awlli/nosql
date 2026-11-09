package kz.atu.lab08;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NotesApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Scene scene = new Scene(loader.load(), 780, 520);
        MainController controller = loader.getController();

        // Закрытие окна крестиком: спросить о несохранённых изменениях
        stage.setOnCloseRequest(event -> {
            if (!controller.confirmDiscardChanges()) {
                event.consume();
            }
        });

        stage.setTitle("Редактор заметок");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
