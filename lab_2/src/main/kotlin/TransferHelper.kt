package org.exampl

import java.awt.Color

object TransferHelper {
    private var min = -3000
    private var max = 16000

    fun setTF(newMin: Int, newMax: Int) {
        min = newMin
        max = newMax
    }

    fun transferFunction(value: Short): Color {
        val newVal = Math.max(0, Math.min((value - min) * 255 / (max - min), 255))
        return Color(newVal, newVal, newVal)
    }
}