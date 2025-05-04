package org.exampl

import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

class View {
    var needReload = false

    fun setupView(width: Int, height: Int) {
        glShadeModel(GL_SMOOTH)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(0.0, Bin.X.toDouble(), 0.0, Bin.Y.toDouble(), -1.0, 1.0)
        glViewport(0, 0, width, height)
    }

    fun drawQuads(layerNumber: Int) {
        if (needReload) {
            generateTextureImage(layerNumber)
            load2DTexture()
            needReload = false
        }

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glEnable(GL_TEXTURE_2D)
        glBindTexture(GL_TEXTURE_2D, textureID)

        glBegin(GL_QUADS)
        glColor3f(1f, 1f, 1f)

        glTexCoord2f(0f, 0f)
        glVertex2f(0f, 0f)

        glTexCoord2f(0f, 1f)
        glVertex2f(0f, Bin.Y.toFloat())

        glTexCoord2f(1f, 1f)
        glVertex2f(Bin.X.toFloat(), Bin.Y.toFloat())

        glTexCoord2f(1f, 0f)
        glVertex2f(Bin.X.toFloat(), 0f)

        glEnd()

        glDisable(GL_TEXTURE_2D)
    }

    private lateinit var textureImage: BufferedImage
    private var textureID: Int = 0

    fun generateTextureImage(layerNumber: Int) {
        textureImage = BufferedImage(Bin.X, Bin.Y, BufferedImage.TYPE_INT_ARGB)

        for (x in 0 until Bin.X) {
            for (y in 0 until Bin.Y) {
                val pixelNumber = x + y * Bin.X + layerNumber * Bin.X * Bin.Y
                val color = TransferHelper.transferFunction(Bin.array[pixelNumber])
                textureImage.setRGB(x, y, color.rgb)
            }
        }
    }

    fun load2DTexture() {
        textureID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureID)

        val pixels = IntArray(Bin.X * Bin.Y)
        textureImage.getRGB(0, 0, Bin.X, Bin.Y, pixels, 0, Bin.X)

        val pixelBuffer = ByteBuffer.allocateDirect(pixels.size * 4)
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val a = (pixel shr 24) and 0xFF
            pixelBuffer.put(r.toByte()).put(g.toByte()).put(b.toByte()).put(a.toByte())
        }
        pixelBuffer.flip()

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, Bin.X, Bin.Y, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixelBuffer)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    }
}
