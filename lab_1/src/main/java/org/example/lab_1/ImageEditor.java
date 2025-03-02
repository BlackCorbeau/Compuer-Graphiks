package org.example.lab_1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ImageEditor extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/lab_1/main-view.fxml"));
        primaryStage.setTitle("Image Editor");
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
