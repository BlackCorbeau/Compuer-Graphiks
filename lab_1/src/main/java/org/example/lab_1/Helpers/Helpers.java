package org.example.lab_1.Helpers;

import javafx.scene.control.ProgressBar;
import javafx.scene.image.*;

public class Helpers {
    public static int CheckBit(int val, int min, int max){
        if (val > max) {
            return max;
        } else if(val < min) {
            return min;
        }
        return val;
    }
    public static double NormolizeIdealOtr(double val) {
        if (val > 1) {
            return 1;
        } else if (val < 0) {
            return 0;
        }
        return val;
    }
    public static void sortArray(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = i + 1; j < array.length; j++) {
                if (array[i] > array[j]) {
                    int temp = array[i];
                    array[i] = array[j];
                    array[j] = temp;
                }
            }
        }
    }

    public static void MatrixFilter( ImageView imageView, double[][] matrix, String log) {
        Image img = imageView.getImage();
        if (img == null) {
            return;
        }

        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        PixelReader reader = img.getPixelReader();
        WritableImage Img = new WritableImage(w, h);
        PixelWriter writer = Img.getPixelWriter();

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {

                int sumRed = 0;
                int sumGreen = 0;
                int sumBlue = 0;


                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int argb = reader.getArgb(x + dx, y + dy);
                        int r = (argb >> 16) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = argb & 0xFF;

                        sumRed += (int) (r * matrix[dy + 1][dx + 1]);
                        sumGreen += (int) (g * matrix[dy + 1][dx + 1]);
                        sumBlue += (int) (b * matrix[dy + 1][dx + 1]);
                    }
                }

                int newRed = CheckBit(sumRed + 128, 0, 255);
                int newGreen = CheckBit(sumGreen + 128, 0, 255);
                int newBlue = CheckBit(sumBlue + 128, 0, 255);

                int a = (reader.getArgb(x, y) >> 24) & 0xFF;

                int newArgb = (a << 24) | (newRed << 16) | (newGreen << 8) | newBlue;
                writer.setArgb(x, y, newArgb);
            }
        }

        imageView.setImage(Img);
        System.out.println("Применен фильтр: " + log);
    }
    public static void DoubleMatrixFilter( ImageView imageView, double[][] gx, double[][] gy, String log) {
        Image img = imageView.getImage();
        if (img == null) {
            return;
        }

        int w = (int) img.getWidth();
        int h = (int) img.getHeight();

        PixelReader reader = img.getPixelReader();
        WritableImage Img = new WritableImage(w, h);
        PixelWriter writer = Img.getPixelWriter();

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int sumGxRed = 0, sumGxGreen = 0, sumGxBlue = 0;
                int sumGyRed = 0, sumGyGreen = 0, sumGyBlue = 0;

                // Применяем свертку с ядрами Gx и Gy
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int argb = reader.getArgb(x + dx, y + dy);
                        int r = (argb >> 16) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = argb & 0xFF;

                        sumGxRed += r * gx[dy + 1][dx + 1];
                        sumGxGreen += g * gx[dy + 1][dx + 1];
                        sumGxBlue += b * gx[dy + 1][dx + 1];

                        sumGyRed += r * gy[dy + 1][dx + 1];
                        sumGyGreen += g * gy[dy + 1][dx + 1];
                        sumGyBlue += b * gy[dy + 1][dx + 1];
                    }
                }

                int gradientRed = (int) Math.sqrt(sumGxRed * sumGxRed + sumGyRed * sumGyRed);
                int gradientGreen = (int) Math.sqrt(sumGxGreen * sumGxGreen + sumGyGreen * sumGyGreen);
                int gradientBlue = (int) Math.sqrt(sumGxBlue * sumGxBlue + sumGyBlue * sumGyBlue);

                gradientRed = CheckBit(gradientRed, 0, 255);
                gradientGreen = CheckBit(gradientGreen, 0, 255);
                gradientBlue = CheckBit(gradientBlue, 0, 255);

                int a = (reader.getArgb(x, y) >> 24) & 0xFF;

                int newArgb = (a << 24) | (gradientRed << 16) | (gradientGreen << 8) | gradientBlue;
                writer.setArgb(x, y, newArgb);
            }
        }

        imageView.setImage(Img);
        System.out.println("Применен фильтр: " + log);
    }
}
