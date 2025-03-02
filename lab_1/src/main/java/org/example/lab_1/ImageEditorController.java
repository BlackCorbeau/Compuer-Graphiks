package org.example.lab_1;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ImageEditorController {

    @FXML
    private ImageView imageView;

    @FXML
    private ProgressBar progressBar;

    public void initialize() {
        // Устанавливаем изображение по умолчанию из URL
        String imageUrl = "https://s0.rbk.ru/v6_top_pics/media/img/7/15/756775857541157.jpg";
        Image defaultImage = new Image(imageUrl);
        imageView.setImage(defaultImage);
    }

    public void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.gif")
        );
        java.io.File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString());
            imageView.setImage(image);
        } else {
            Image stImage = new Image("");
        }
    }

    public void applyInversionFilter() {
        // Здесь будет логика для применения фильтра инверсии
        // Пока просто выводим сообщение
        System.out.println("Применен фильтр инверсии");
    }
}
