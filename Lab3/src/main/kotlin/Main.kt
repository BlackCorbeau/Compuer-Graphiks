package org.exampl

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil.*
import java.nio.FloatBuffer
import kotlin.math.*

class AtomModelShader {
    private var window: Long = 0
    private var isRunning = false
    private var shaderProgram = 0
    private var vao = 0
    private var vbo = 0

    // Параметры камеры
    private var cameraDistance = 15.0f
    private var cameraAngleX = 30.0f
    private var cameraAngleY = 45.0f

    // Параметры атома
    private val nucleusRadius = 1.2f
    private val electronRadius = 0.4f
    private val orbitRadii = floatArrayOf(4.0f, 6.0f, 8.0f)
    private var rotationAngle = 0.0f

    private var nucleusRotationAngle = 0.0f
    private val electronSpeeds = floatArrayOf(1.0f, 1.3f, 0.8f) // Разные скорости для электронов
    private val protonSpeeds = floatArrayOf(0.7f, 1.1f, 0.9f)   // Скорости для протонов

    fun run() {
        init()
        loop()
        cleanup()
    }

    // ... (init(), initShaders(), initBuffers() остаются без изменений)

    private fun loop() {
        var lastTime = glfwGetTime()
        while (isRunning && !glfwWindowShouldClose(window)) {
            val currentTime = glfwGetTime()
            val deltaTime = (currentTime - lastTime).toFloat()
            lastTime = currentTime

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // Обновление углов вращения с учетом deltaTime
            nucleusRotationAngle += 0.5f * deltaTime
            rotationAngle += 0.6f * deltaTime

            // Активация шейдера
            glUseProgram(shaderProgram)

            // Настройка камеры
            val view = createViewMatrix()
            val projection = createProjectionMatrix()

            // Установка uniform-переменных
            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "view"), false, view)
            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "projection"), false, projection)
            glUniform3f(glGetUniformLocation(shaderProgram, "lightPos"), 10f, 10f, 10f)
            glUniform3f(glGetUniformLocation(shaderProgram, "lightColor"), 1f, 1f, 1f)
            glUniform3f(glGetUniformLocation(shaderProgram, "viewPos"),
                0f, 0f, cameraDistance)

            // Отрисовка ядра с вращением
            drawNucleus()

            // Отрисовка электронов и протонов
            drawParticles()

            glfwSwapBuffers(window)
            glfwPollEvents()
        }
    }

    private fun drawNucleus() {
        val model = org.joml.Matrix4f()
            .rotateY(nucleusRotationAngle) // Вращение ядра
            .scale(nucleusRadius)

        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, model.get(FloatArray(16)).toFloatBuffer())
        glUniform3f(glGetUniformLocation(shaderProgram, "objectColor"), 0.3f, 0.5f, 1.0f)

        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 32 * 32 * 6)
        glBindVertexArray(0)
    }

    private fun drawParticles() {
        // Цвета для частиц
        val electronColors = arrayOf(
            floatArrayOf(1f, 0.2f, 0.2f), // Красный
            floatArrayOf(0.2f, 1f, 0.2f), // Зеленый
            floatArrayOf(0.4f, 0.4f, 1f)  // Синий
        )

        val protonColors = arrayOf(
            floatArrayOf(1f, 0.5f, 0.5f), // Светло-красный
            floatArrayOf(0.5f, 1f, 0.5f), // Светло-зеленый
            floatArrayOf(0.7f, 0.7f, 1f)  // Светло-синий
        )

        // Отрисовка электронов
        for (i in 0 until 3) {
            val angle = rotationAngle * electronSpeeds[i]
            val x = orbitRadii[i] * cos(angle.toDouble()).toFloat()
            val z = orbitRadii[i] * sin(angle.toDouble()).toFloat()
            val y = orbitRadii[i] * 0.3f * sin(angle.toDouble() * 1.5).toFloat()

            val model = org.joml.Matrix4f()
                .translate(x, y, z)
                .scale(electronRadius)

            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, model.get(FloatArray(16)).toFloatBuffer())
            glUniform3f(glGetUniformLocation(shaderProgram, "objectColor"),
                electronColors[i][0], electronColors[i][1], electronColors[i][2])

            glBindVertexArray(vao)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 32 * 32 * 6)
            glBindVertexArray(0)
        }

        // Отрисовка протонов (на других орбитах)
        for (i in 0 until 3) {
            val angle = rotationAngle * protonSpeeds[i] + PI.toFloat() // Смещение на 180 градусов
            val x = (orbitRadii[i] + 1.5f) * cos(angle.toDouble()).toFloat()
            val z = (orbitRadii[i] + 1.5f) * sin(angle.toDouble()).toFloat()
            val y = (orbitRadii[i] + 1.5f) * 0.3f * cos(angle.toDouble() * 1.2).toFloat()

            val model = org.joml.Matrix4f()
                .translate(x, y, z)
                .scale(electronRadius * 0.8f) // Протоны немного меньше

            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, model.get(FloatArray(16)).toFloatBuffer())
            glUniform3f(glGetUniformLocation(shaderProgram, "objectColor"),
                protonColors[i][0], protonColors[i][1], protonColors[i][2])

            glBindVertexArray(vao)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 32 * 32 * 6)
            glBindVertexArray(0)
        }
    }

    private fun init() {
        if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)

        window = glfwCreateWindow(1000, 800, "3D Atom Model with Shaders", NULL, NULL)
            ?: throw RuntimeException("Failed to create GLFW window")

        glfwMakeContextCurrent(window)
        GL.createCapabilities()

        // Инициализация шейдеров
        initShaders()
        initBuffers()

        glEnable(GL_DEPTH_TEST)
        glClearColor(0.1f, 0.1f, 0.15f, 1.0f)

        glfwSetFramebufferSizeCallback(window) { _, width, height ->
            glViewport(0, 0, width, height)
        }

        glfwShowWindow(window)
        isRunning = true
        setupControls()
    }

    private fun initShaders() {
        // Вершинный шейдер
        val vertexShader = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            layout (location = 1) in vec3 aNormal;
            
            out vec3 Normal;
            out vec3 FragPos;
            
            uniform mat4 model;
            uniform mat4 view;
            uniform mat4 projection;
            
            void main() {
                gl_Position = projection * view * model * vec4(aPos, 1.0);
                FragPos = vec3(model * vec4(aPos, 1.0));
                Normal = mat3(transpose(inverse(model))) * aNormal;
            }
        """.trimIndent()

        // Фрагментный шейдер
        val fragmentShader = """
            #version 330 core
            out vec4 FragColor;
            
            in vec3 Normal;
            in vec3 FragPos;
            
            uniform vec3 objectColor;
            uniform vec3 lightPos;
            uniform vec3 lightColor;
            uniform vec3 viewPos;
            
            void main() {
                // Ambient
                float ambientStrength = 0.1;
                vec3 ambient = ambientStrength * lightColor;
                
                // Diffuse 
                vec3 norm = normalize(Normal);
                vec3 lightDir = normalize(lightPos - FragPos);
                float diff = max(dot(norm, lightDir), 0.0);
                vec3 diffuse = diff * lightColor;
                
                // Specular
                float specularStrength = 0.5;
                vec3 viewDir = normalize(viewPos - FragPos);
                vec3 reflectDir = reflect(-lightDir, norm);
                float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);
                vec3 specular = specularStrength * spec * lightColor;
                
                vec3 result = (ambient + diffuse + specular) * objectColor;
                FragColor = vec4(result, 1.0);
            }
        """.trimIndent()

        shaderProgram = glCreateProgram()
        val vs = compileShader(vertexShader, GL_VERTEX_SHADER)
        val fs = compileShader(fragmentShader, GL_FRAGMENT_SHADER)

        glAttachShader(shaderProgram, vs)
        glAttachShader(shaderProgram, fs)
        glLinkProgram(shaderProgram)

        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            throw RuntimeException("Shader linking failed: ${glGetProgramInfoLog(shaderProgram)}")
        }

        glDeleteShader(vs)
        glDeleteShader(fs)
    }

    private fun compileShader(source: String, type: Int): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, source)
        glCompileShader(shader)

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw RuntimeException("Shader compilation failed: ${glGetShaderInfoLog(shader)}")
        }

        return shader
    }

    private fun initBuffers() {
        vao = glGenVertexArrays()
        vbo = glGenBuffers()

        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        // Создаем сферу для ядра и электронов
        val sphereData = createSphereData(1.0f, 32, 32)
        glBufferData(GL_ARRAY_BUFFER, sphereData, GL_STATIC_DRAW)

        // Позиции вершин (0)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, 0)
        glEnableVertexAttribArray(0)

        // Нормали (1)
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, 3 * Float.SIZE_BYTES.toLong())
        glEnableVertexAttribArray(1)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }

    private fun createSphereData(radius: Float, sectors: Int, stacks: Int): FloatBuffer {
        val vertices = mutableListOf<Float>()
        val sectorStep = 2 * PI.toFloat() / sectors
        val stackStep = PI.toFloat() / stacks

        for (i in 0..stacks) {
            val stackAngle = PI.toFloat() / 2 - i * stackStep
            val xy = radius * cos(stackAngle)
            val z = radius * sin(stackAngle)

            for (j in 0..sectors) {
                val sectorAngle = j * sectorStep
                val x = xy * cos(sectorAngle)
                val y = xy * sin(sectorAngle)

                // Нормаль
                val nx = x / radius
                val ny = y / radius
                val nz = z / radius

                vertices.add(x)
                vertices.add(y)
                vertices.add(z)
                vertices.add(nx)
                vertices.add(ny)
                vertices.add(nz)
            }
        }

        val buffer = org.lwjgl.BufferUtils.createFloatBuffer(vertices.size)
        vertices.forEach { buffer.put(it) }
        buffer.flip()
        return buffer
    }

    private fun setupControls() {
        glfwSetKeyCallback(window) { _, key, _, action, _ ->
            when {
                key == GLFW_KEY_ESCAPE && action == GLFW_PRESS -> glfwSetWindowShouldClose(window, true)
                action == GLFW_PRESS || action == GLFW_REPEAT -> handleKeyPress(key)
            }
        }

        glfwSetScrollCallback(window) { _, _, yOffset ->
            cameraDistance = (cameraDistance - yOffset).coerceIn(5.0, 30.0).toFloat()
        }

        glfwSetCursorPosCallback(window) { _, xPos, yPos ->
            if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
                cameraAngleY += xPos.toFloat() * 0.2f
                cameraAngleX = (cameraAngleX + yPos.toFloat() * 0.1f).coerceIn(5f, 85f)
            }
        }
    }

    private fun handleKeyPress(key: Int) {
        when (key) {
            GLFW_KEY_W -> cameraAngleX -= 2f
            GLFW_KEY_S -> cameraAngleX += 2f
            GLFW_KEY_A -> cameraAngleY -= 2f
            GLFW_KEY_D -> cameraAngleY += 2f
            GLFW_KEY_Q -> cameraDistance += 0.5f
            GLFW_KEY_E -> cameraDistance -= 0.5f
        }
    }

    private fun createViewMatrix(): FloatBuffer {
        val matrix = org.joml.Matrix4f()
        matrix.rotateX(cameraAngleX * (PI.toFloat() / 180f))
        matrix.rotateY(cameraAngleY * (PI.toFloat() / 180f))
        matrix.translate(0f, 0f, -cameraDistance)
        return matrix.get(FloatArray(16)).toFloatBuffer()
    }

    private fun createProjectionMatrix(): FloatBuffer {
        val matrix = org.joml.Matrix4f()
        matrix.perspective(45f * (PI.toFloat() / 180f), 1000f / 800f, 0.1f, 100f)
        return matrix.get(FloatArray(16)).toFloatBuffer()
    }

    private fun drawElectrons() {
        val colors = arrayOf(
            floatArrayOf(1f, 0.2f, 0.2f), // Красный
            floatArrayOf(0.2f, 1f, 0.2f), // Зеленый
            floatArrayOf(0.4f, 0.4f, 1f)  // Синий
        )

        for (i in 0 until 3) {
            val angle = rotationAngle * (1 + i * 0.3f)
            val x = orbitRadii[i] * cos(angle.toDouble()).toFloat()
            val z = orbitRadii[i] * sin(angle.toDouble()).toFloat()
            val y = orbitRadii[i] * 0.3f * sin(angle.toDouble() * 1.5).toFloat()

            val model = org.joml.Matrix4f()
                .translate(x, y, z)
                .scale(electronRadius)

            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, model.get(FloatArray(16)).toFloatBuffer())
            glUniform3f(glGetUniformLocation(shaderProgram, "objectColor"), colors[i][0], colors[i][1], colors[i][2])

            glBindVertexArray(vao)
            glDrawArrays(GL_TRIANGLE_STRIP, 0, 32 * 32 * 6)
            glBindVertexArray(0)
        }
    }

    private fun FloatArray.toFloatBuffer(): FloatBuffer {
        val buffer = org.lwjgl.BufferUtils.createFloatBuffer(size)
        buffer.put(this)
        buffer.flip()
        return buffer
    }

    private fun cleanup() {
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        glDeleteProgram(shaderProgram)
        glfwDestroyWindow(window)
        glfwTerminate()
    }
}

fun main() {
    AtomModelShader().run()
}