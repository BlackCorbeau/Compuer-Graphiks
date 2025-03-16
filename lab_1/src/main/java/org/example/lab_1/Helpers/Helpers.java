package org.example.lab_1.Helpers;

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
}
