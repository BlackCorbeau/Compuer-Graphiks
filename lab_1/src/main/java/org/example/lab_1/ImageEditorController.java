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
import static org.example.lab_1.Helpers.Helpers.CheckBit;
import static org.example.lab_1.Helpers.Helpers.NormolizeIdealOtr;
import static org.example.lab_1.Helpers.Helpers.sortArray;

public class ImageEditorController {

    @FXML
    private ImageView imageView;
    private Image oldImg;

    @FXML
    private ProgressBar progressBar;

    public void initialize() {
        progressBar.setVisible(false);
        String imageUrl = "https://i.pinimg.com/originals/a9/1c/2e/a91c2ed53bdd4c8d236b82828ea99505.jpg";
        Image defaultImage = new Image(imageUrl);
        imageView.setImage(defaultImage);
    }

    public void returnToOldImage() {
        if(imageView.getImage() == oldImg || oldImg == null){
            return;
        } else {
            imageView.setImage(oldImg);
        }
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
        oldImg = imageView.getImage();
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

                r = 255 - r;
                g = 255 - g;
                b = 255 - b;

                int invertedArgb = (a << 24) | (r << 16) | (g << 8) | b;
                pixelWriter.setArgb(x, y, invertedArgb);
            }
        }

        imageView.setImage(invertedImage);
        System.out.println("Применен фильтр инверсии");
        progressBar.setVisible(false);
    }

    public void GrayScaleFilter() {
        progressBar.setVisible(true);

        oldImg = imageView.getImage();
        Image img = imageView.getImage();
        if(img == null){ return; }

        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        WritableImage GraySkaleImg = new WritableImage(w, h);

        PixelReader Reader = img.getPixelReader();
        PixelWriter Writer = GraySkaleImg.getPixelWriter();

        for(int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = Reader.getArgb(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                double grey = 0.299 * r + 0.587 * g + 0.144 * b;

                r = (int) grey;
                g = (int) grey;
                b = (int) grey;

                int newArgb = (a << 24) | (r << 16) | (g << 8) | b;
                Writer.setArgb(x, y, newArgb);
            }
        }

        imageView.setImage(GraySkaleImg);
        System.out.println("Применён оттенки серого");
        progressBar.setVisible(false);
    }

    public void Sepia() {
        progressBar.setVisible(true);
        oldImg = imageView.getImage();
        Image img = imageView.getImage();
        if(img == null){ return; }

        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        WritableImage SepiaImg = new WritableImage(w, h);

        PixelReader Reader = img.getPixelReader();
        PixelWriter Writer = SepiaImg.getPixelWriter();

        for(int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = Reader.getArgb(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                double grey = 0.299 * r + 0.587 * g + 0.144 * b;

                int k = 20;
                double f;
                r = CheckBit((int) grey+(2*k), 0, 255);
                f = grey+(0.5*k);
                g = CheckBit((int) f, 0, 255);
                b = CheckBit((int) grey-(1*k), 0, 255);

                int newArgb = (a << 24) | (r << 16) | (g << 8) | b;
                Writer.setArgb(x, y, newArgb);
            }
        }
        imageView.setImage(SepiaImg);
        System.out.println("Применён Sepia");
        progressBar.setVisible(false);
    }

    public void GetYarc() {
        progressBar.setVisible(true);
        oldImg = imageView.getImage();
        Image img = imageView.getImage();
        if (img == null) {
            return;
        }

        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        WritableImage YarcImg = new WritableImage(w, h);

        PixelReader Reader = img.getPixelReader();
        PixelWriter Writer = YarcImg.getPixelWriter();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = Reader.getArgb(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                double grey = 0.299 * r + 0.587 * g + 0.144 * b;

                int k = 20;
                r = Math.min(255, r + k);
                g = Math.min(255, g + k);
                b = Math.min(255, b + k);

                int newArgb = (a << 24) | (r << 16) | (g << 8) | b;
                Writer.setArgb(x, y, newArgb);
            }
        }
        imageView.setImage(YarcImg);
        System.out.println("Применёно Увеличение яркости");
        progressBar.setVisible(false);
    }

    public void Sdvig() {
        progressBar.setVisible(true);
        oldImg = imageView.getImage();
        Image img = imageView.getImage();
        if(img == null){ return; }

        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        WritableImage SdvigImg = new WritableImage(w, h);

        PixelReader Reader = img.getPixelReader();
        PixelWriter Writer = SdvigImg.getPixelWriter();

        for(int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if(x < 50){
                    continue;
                }
                int argb = Reader.getArgb(x - 50, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int newArgb = (a << 24) | (r << 16) | (g << 8) | b;
                Writer.setArgb(x, y, newArgb);
            }
        }
        imageView.setImage(SdvigImg);
        System.out.println("Применён Сдвиг");
        progressBar.setVisible(false);
    }

    public void grayWorld() {
        progressBar.setVisible(true);
        oldImg = imageView.getImage();
        Image img = imageView.getImage();
        if(img == null){ return; }

        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        PixelReader pixelReader = img.getPixelReader();

        double sumR = 0, sumG = 0, sumB = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = pixelReader.getArgb(x, y);

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                sumR += r;
                sumG += g;
                sumB += b;
            }
        }

        double avgR = sumR / (w * h);
        double avgG = sumG / (w * h);
        double avgB = sumB / (w * h);

        double avgGray = (avgR + avgG + avgB) / 3;

        double scaleR = avgGray / avgR;
        double scaleG = avgGray / avgG;
        double scaleB = avgGray / avgB;

        WritableImage GWImage = new WritableImage(w, h);
        PixelWriter pixelWriter = GWImage.getPixelWriter();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = pixelReader.getArgb(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int newR = (int) Math.min(255, r * scaleR);
                int newG = (int) Math.min(255, g * scaleG);
                int newB = (int) Math.min(255, b * scaleB);

                int newArgb = (a << 24) | (newR << 16) | (newG << 8) | newB;
                pixelWriter.setArgb(x, y, newArgb);
            }
        }

        imageView.setImage(GWImage);
        System.out.println("Применен фильтр 'Серый мир'");
        progressBar.setVisible(false);
    }

    public void autoLevels() {
        progressBar.setVisible(true);
        oldImg = imageView.getImage();
        Image img = imageView.getImage();
        if(img == null) { return; }

        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        PixelReader reader = img.getPixelReader();

        int rMin = 127, rMax = 127, gMin = 127, gMax = 127, bMin = 127, bMax = 127;
        for(int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++){
                int argb = reader.getArgb(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if(r < rMin) {
                    rMin = r;
                } else if (r > rMax) {
                    rMax = r;
                }

                if(g < gMin) {
                    gMin = g;
                } else if (g > gMax) {
                    gMax = g;
                }

                if(b < bMin) {
                    bMin = b;
                } else if (b > bMax) {
                    bMax = b;
                }
            }
        }

        WritableImage autolevelImg = new WritableImage(w, h);
        PixelWriter writer = autolevelImg.getPixelWriter();

        for(int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++){
                int argb = reader.getArgb(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                double _r = ((r - rMin) / (rMax - rMin)) * 255;
                r = CheckBit((int) _r , 0 , 255);

                double _g = ((g - gMin) / (gMax - gMin)) * 255;
                g = CheckBit((int) _g , 0 , 255);

                double _b = ((b - bMin) / (bMax - bMin)) * 255;
                b = CheckBit((int) _b , 0 , 255);

                int newArgb = (a << 24) | (r << 16) | (g << 8) | b;
                writer.setArgb(x, y, newArgb);
            }
        }

        imageView.setImage(autolevelImg);
        System.out.println("Применен фильтр 'Автолевел'");
        progressBar.setVisible(false);
    }

    public void IDdealOtr() {
        progressBar.setVisible(true);
        oldImg = imageView.getImage();
        Image img = imageView.getImage();
        if(img == null) { return; }

        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        PixelReader reader = img.getPixelReader();

        int rMax = 127, gMax = 127, bMax = 127;
        for(int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++){
                int argb = reader.getArgb(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if(r > rMax) {
                    rMax = r;
                }

                if(g > gMax) {
                    gMax = g;
                }

                if(b > bMax) {
                    bMax = b;
                }
            }
        }

        WritableImage IdealOtrImg = new WritableImage(w, h);
        PixelWriter writer = IdealOtrImg.getPixelWriter();

        for(int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++){
                int argb = reader.getArgb(x, y);

                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                double _r = NormolizeIdealOtr(r/rMax);
                double _g = NormolizeIdealOtr(g/gMax);
                double _b = NormolizeIdealOtr(b/bMax);

                r = (int) _r * 255;
                g = (int) _g * 255;
                b = (int) _b * 255;

                int newArgb = (a << 24) | (r << 16) | (g << 8) | b;
                writer.setArgb(x, y, newArgb);
            }
        }
        imageView.setImage(IdealOtrImg);
        System.out.println("Применен фильтр 'Идеальный отражатель'");
        progressBar.setVisible(false);
    }

    public void applyMedianFilter() {
        progressBar.setVisible(true);
        oldImg = imageView.getImage();
        Image img = imageView.getImage();
        if (img == null) {
            return;
        }

        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        PixelReader reader = img.getPixelReader();
        WritableImage medianImg = new WritableImage(w, h);
        PixelWriter writer = medianImg.getPixelWriter();

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int[] redValues = new int[9];
                int[] greenValues = new int[9];
                int[] blueValues = new int[9];
                int index = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int argb = reader.getArgb(x + dx, y + dy);
                        redValues[index] = (argb >> 16) & 0xFF;   // Красный канал
                        greenValues[index] = (argb >> 8) & 0xFF;  // Зеленый канал
                        blueValues[index] = argb & 0xFF;          // Синий канал
                        index++;
                    }
                }

                sortArray(redValues);
                sortArray(greenValues);
                sortArray(blueValues);


                int medianRed = redValues[4];
                int medianGreen = greenValues[4];
                int medianBlue = blueValues[4];

                int a = (reader.getArgb(x, y) >> 24) & 0xFF;

                int newArgb = (a << 24) | (medianRed << 16) | (medianGreen << 8) | medianBlue;
                writer.setArgb(x, y, newArgb);
            }
        }

        imageView.setImage(medianImg);
        System.out.println("Применен фильтр 'Медиана'");
        progressBar.setVisible(false);
    }

}
