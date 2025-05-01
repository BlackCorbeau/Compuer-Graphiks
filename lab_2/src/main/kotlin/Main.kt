package org.exampl

/*import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.glfw.GLFW.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

class CTVisualizer {
    private var window: Long = 0
    private var vao = 0
    private var vbo = 0
    private var textureID = 0
    private var shaderProgram = 0
    private var currentLayer = 0
    private var minValue = 0
    private var maxValue = 40961
    private var windowWidth = 1600
    private var windowHeight = 1000
    private lateinit var volumeData: ShortArray
    private var width = 256  // Фиксированный размер для примера
    private var height = 256
    private var depth = 256

    fun run(binFilePath: String) {
        initWindow()
        initOpenGL()
        loadVolumeData(binFilePath)
        mainLoop()
        cleanup()
    }

    private fun initWindow() {
        if (!glfwInit()) {
            throw RuntimeException("Failed to initialize GLFW")
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)

        window = glfwCreateWindow(windowWidth, windowHeight, "КТ Визуализация (OpenTK/Kotlin)", 0, 0)
        if (window == 0L) {
            glfwTerminate()
            throw RuntimeException("Failed to create GLFW window")
        }

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)
        glfwShowWindow(window)
    }

    private fun initOpenGL() {
        GL.createCapabilities()

        val vertexShader = """
            #version 330 core
            layout(location = 0) in vec2 position;
            layout(location = 1) in vec2 texCoord;
            out vec2 TexCoord;
            void main() {
                gl_Position = vec4(position, 0.0, 1.0);
                TexCoord = texCoord;
            }
        """.trimIndent()

        val fragmentShader = """
            #version 330 core
            in vec2 TexCoord;
            out vec4 FragColor;
            uniform sampler2D ourTexture;
            void main() {
                FragColor = texture(ourTexture, TexCoord);
            }
        """.trimIndent()

        shaderProgram = createShaderProgram(vertexShader, fragmentShader)

        vao = glGenVertexArrays()
        vbo = glGenBuffers()

        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        val vertices = floatArrayOf(
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            -1.0f,  1.0f, 0.0f, 1.0f,
            1.0f,  1.0f, 1.0f, 1.0f
        )

        val vertexBuffer = MemoryUtil.memAllocFloat(vertices.size)
        vertexBuffer.put(vertices).flip()

        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 4, 0)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 4, 2 * 4)
        glEnableVertexAttribArray(0)
        glEnableVertexAttribArray(1)

        MemoryUtil.memFree(vertexBuffer)

        textureID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureID)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    }

    private fun loadVolumeData(binFilePath: String) {
        val file = File(binFilePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File $binFilePath not found")
        }

        val buffer = ByteBuffer.wrap(file.readBytes())
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()

        volumeData = ShortArray(width * height * depth)
        buffer.get(volumeData)

        // Автоматическое определение min/max значений
        minValue = volumeData.minOrNull()?.toInt() ?: 0
        maxValue = volumeData.maxOrNull()?.toInt() ?: 4096

        println("Loaded volume data: ${volumeData.size} voxels")
        println("Value range: $minValue - $maxValue")
    }

    private fun updateTexture(layer: Int) {
        val layerData = ByteBuffer.allocateDirect(width * height * 4)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val value = volumeData[x + y * width + layer * width * height].toInt()
                val normalized = ((value - minValue).toFloat() / (maxValue - minValue).toFloat()).coerceIn(0f, 1f)
                val color = (normalized * 255).toInt()

                layerData.put(color.toByte())
                layerData.put(color.toByte())
                layerData.put(color.toByte())
                layerData.put(255.toByte()) // Alpha
            }
        }

        layerData.flip()
        glBindTexture(GL_TEXTURE_2D, textureID)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, layerData)
    }

    private fun mainLoop() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT)

            updateTexture(currentLayer)

            glUseProgram(shaderProgram)
            glBindVertexArray(vao)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 4)

            glfwSwapBuffers(window)
            glfwPollEvents()

            if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
                currentLayer = min(currentLayer + 1, depth - 1)
            }
            if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
                currentLayer = max(currentLayer - 1, 0)
            }
        }
    }

    private fun createShaderProgram(vertexShaderSource: String, fragmentShaderSource: String): Int {
        val vertexShader = glCreateShader(GL_VERTEX_SHADER)
        glShaderSource(vertexShader, vertexShaderSource)
        glCompileShader(vertexShader)

        val fragmentShader = glCreateShader(GL_FRAGMENT_SHADER)
        glShaderSource(fragmentShader, fragmentShaderSource)
        glCompileShader(fragmentShader)

        val program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)

        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)

        return program
    }

    private fun cleanup() {
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteTextures(textureID)
        glDeleteProgram(shaderProgram)
        glfwTerminate()
    }
}

fun main() {
    // Укажите путь к вашему .bin файлу
    val binFilePath = "./testdata.bin"
    CTVisualizer().run(binFilePath)
}*/

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.glViewport
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

    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)
    glfwShowWindow(window)

    GL.createCapabilities()

    val form1 = Form1()
    form1.run(window)

    glfwTerminate()
    exitProcess(0)
}






