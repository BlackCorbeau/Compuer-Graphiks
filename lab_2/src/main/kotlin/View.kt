package org.exampl

import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.ByteBuffer

class View {
    fun setupView(width: Int, height: Int) {
        glShadeModel(GL_SMOOTH)
        glMatrixMode(GL_PROJECTION)
        glLoadIdentity()
        glOrtho(0.0, Bin.X.toDouble(), 0.0, Bin.Y.toDouble(), -1.0, 1.0)
        glViewport(0, 0, width, height)
    }

    fun drawQuads(layerNumber: Int) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glBegin(GL_QUADS)

        for (x in 0 until Bin.X - 1) {
            for (y in 0 until Bin.Y - 1) {
                val value1 = Bin.array[x + y * Bin.X + layerNumber * Bin.X * Bin.Y]
                val value2 = Bin.array[x + (y + 1) * Bin.X + layerNumber * Bin.X * Bin.Y]
                val value3 = Bin.array[x + 1 + (y + 1) * Bin.X + layerNumber * Bin.X * Bin.Y]
                val value4 = Bin.array[x + 1 + y * Bin.X + layerNumber * Bin.X * Bin.Y]

                val color1 = TransferHelper.transferFunction(value1)
                val color2 = TransferHelper.transferFunction(value2)
                val color3 = TransferHelper.transferFunction(value3)
                val color4 = TransferHelper.transferFunction(value4)

                glColor3f(color1.red / 255.0f, color1.green / 255.0f, color1.blue / 255.0f)
                glVertex2f(x.toFloat(), y.toFloat())

                glColor3f(color2.red / 255.0f, color2.green / 255.0f, color2.blue / 255.0f)
                glVertex2f(x.toFloat(), (y + 1).toFloat())

                glColor3f(color3.red / 255.0f, color3.green / 255.0f, color3.blue / 255.0f)
                glVertex2f((x + 1).toFloat(), (y + 1).toFloat())

                glColor3f(color4.red / 255.0f, color4.green / 255.0f, color4.blue / 255.0f)
                glVertex2f((x + 1).toFloat(), y.toFloat())
            }
        }

        glEnd()
    }

    fun drawQuadStrip(layerNumber: Int) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        for (x in 0 until Bin.X - 1) {
            glBegin(GL_QUAD_STRIP)

            for (y in 0 until Bin.Y) {
                val value1 = Bin.array[x + y * Bin.X + layerNumber * Bin.X * Bin.Y]
                val value2 = Bin.array[x + 1 + y * Bin.X + layerNumber * Bin.X * Bin.Y]

                val color1 = TransferHelper.transferFunction(value1)
                val color2 = TransferHelper.transferFunction(value2)

                glColor3f(color1.red / 255.0f, color1.green / 255.0f, color1.blue / 255.0f)
                glVertex2f(x.toFloat(), y.toFloat())

                glColor3f(color2.red / 255.0f, color2.green / 255.0f, color2.blue / 255.0f)
                glVertex2f((x + 1).toFloat(), y.toFloat())
            }

            glEnd()
        }
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

    fun drawTexture() {
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
