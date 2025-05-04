package org.exampl

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

object Bin {
    var X = 0
    var Y = 0
    var Z = 0
    lateinit var array: ShortArray

    fun readBIN(path: String) {
        if (File(path).exists()) {
            val buffer = ByteBuffer.wrap(File(path).readBytes()).order(ByteOrder.LITTLE_ENDIAN)

            X = buffer.int
            Y = buffer.int
            Z = buffer.int

            buffer.position(buffer.position() + 3 * Float.SIZE_BYTES) // Пропуск значений масштаба

            val arraySize = X * Y * Z
            array = ShortArray(arraySize)

            for (i in 0 until arraySize) {
                array[i] = buffer.short
            }
        }
    }
}
