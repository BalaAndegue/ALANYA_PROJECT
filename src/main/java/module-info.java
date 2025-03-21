module com.example.alanyaproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.alanyaproject to javafx.fxml;
    exports com.example.alanyaproject;
}