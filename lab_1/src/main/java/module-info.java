module org.example.lab_1 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens org.example.lab_1 to javafx.fxml;
    exports org.example.lab_1;
}