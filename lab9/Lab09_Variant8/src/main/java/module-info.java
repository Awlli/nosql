module kz.atu.lab09 {
    requires javafx.controls;
    requires javafx.fxml;


    opens kz.atu.lab09 to javafx.fxml;
    exports kz.atu.lab09;
}