package org.example.lab_1;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ImageEditorController {

    @FXML
    private ImageView imageView;

    @FXML
    private ProgressBar progressBar;

    public void initialize() {
        progressBar.setVisible(false);
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
        }
    }

    public void applyInversionFilter() {
        progressBar.setVisible(true);
        Image image = imageView.getImage();
        if (image == null) return;

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        WritableImage invertedImage = new WritableImage(width, height);
        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = invertedImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // Invert colors
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;

                // Reassemble ARGB
                int invertedArgb = (a << 24) | (r << 16) | (g << 8) | b;
                pixelWriter.setArgb(x, y, invertedArgb);
            }
        }

        imageView.setImage(invertedImage);
        System.out.println("Применен фильтр инверсии");
        progressBar.setVisible(false);
    }
}
