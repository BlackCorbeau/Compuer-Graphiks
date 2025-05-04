package org.exampl

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.system.exitProcess

fun main() {
    if (!glfwInit()) {
        throw IllegalStateException("Unable to initialize GLFW")
    }

    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

    val window = glfwCreateWindow(800, 600, "CT Visualizer", NULL, NULL)
    if (window == NULL) {
        throw RuntimeException("Failed to create the GLFW window")
    }

    glfwSetFramebufferSizeCallback(window) { _: Long, width: Int, height: Int ->
        glViewport(0, 0, width, height)
    }

    val form1 = Form1()

    glfwSetKeyCallback(window) { _: Long, key: Int, _: Int, action: Int, _: Int ->
        if (action == GLFW_PRESS || action == GLFW_REPEAT) {
            when (key) {
                GLFW_KEY_LEFT -> {
                    if (form1.currentLayer > 0) {
                        form1.currentLayer--
                        form1.view.needReload = true
                    }
                }
                GLFW_KEY_RIGHT -> {
                    if (form1.currentLayer < Bin.Z - 1) {
                        form1.currentLayer++
                        form1.view.needReload = true
                    }
                }
            }
        }
    }

    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)
    glfwShowWindow(window)

    GL.createCapabilities()

    form1.run(window)

    glfwTerminate()
    exitProcess(0)
}
