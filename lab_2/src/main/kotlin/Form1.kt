package org.exampl

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class Form1 {
    private var loaded = false
    private var currentLayer = 0
    private val bin = Bin()
    private val view = View()

    private var minTF = -3000
    private var widthTF = 2000

    private var frameCount = 0
    private var nextFPSUpdate = System.currentTimeMillis() + 1000

    fun displayFPS() {
        if (System.currentTimeMillis() >= nextFPSUpdate) {
            println("CT Visualizer (fps = $frameCount)")
            nextFPSUpdate = System.currentTimeMillis() + 1000
            frameCount = 0
        }
        frameCount++
    }

    fun openFileDialog() {
        val fileChooser = JFileChooser()
        fileChooser.fileFilter = FileNameExtensionFilter("BIN Files", "bin")
        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            bin.readBIN(file.absolutePath)
            view.setupView(800, 600) // Установите размеры окна
            loaded = true
        }
    }

    fun run(window: Long) {
        openFileDialog()
        while (!glfwWindowShouldClose(window)) {
            displayFPS()
            if (loaded) {
                view.drawQuads(currentLayer)
            }
            glfwSwapBuffers(window)
            glfwPollEvents()
        }
    }
}
