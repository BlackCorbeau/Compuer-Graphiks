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
}
