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
    private val protonRadius = 0.5f  // Протоны немного больше электронов
    private val orbitRadii = floatArrayOf(4.0f, 6.0f, 8.0f)
    private var rotationAngle = 0.0f

    private var nucleusRotationAngle = 0.0f
    private val electronSpeeds = floatArrayOf(1.0f, 1.3f, 0.8f)
    private val protonSpeeds = floatArrayOf(0.7f, 1.1f, 0.9f)

    fun run() {
        init()
        loop()
        cleanup()
    }

    private fun loop() {
        var lastTime = glfwGetTime()
        while (isRunning && !glfwWindowShouldClose(window)) {
            val currentTime = glfwGetTime()
            val deltaTime = (currentTime - lastTime).toFloat()
            lastTime = currentTime

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            // Обновление углов вращения
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
            .rotateY(nucleusRotationAngle)
            .scale(nucleusRadius)

        glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, floatArrayToBuffer(model.get(FloatArray(16))))
        glUniform3f(glGetUniformLocation(shaderProgram, "objectColor"), 1.0f, 1.0f, 0.0f)
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, 32 * 32 * 6)
        glBindVertexArray(0)
    }

    private fun drawParticles() {
        // Красный цвет для электронов
        val electronColor = floatArrayOf(1f, 0.0f, 0.0f)

        // Синий цвет для протонов
        val protonColor = floatArrayOf(0.0f, 0.0f, 1f)

        // Отрисовка электронов (красные)
        for (i in 0 until 3) {
            val angle = rotationAngle * electronSpeeds[i]
            val x = orbitRadii[i] * cos(angle.toDouble()).toFloat()
            val z = orbitRadii[i] * sin(angle.toDouble()).toFloat()
            val y = orbitRadii[i] * 0.3f * sin(angle.toDouble() * 1.5).toFloat()

            val model = org.joml.Matrix4f()
                .translate(x, y, z)
                .scale(electronRadius)

            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, floatArrayToBuffer(model.get(FloatArray(16))))
            glUniform3f(glGetUniformLocation(shaderProgram, "objectColor"),
                electronColor[0], electronColor[1], electronColor[2])

            glBindVertexArray(vao)
            glDrawArrays(GL_TRIANGLES, 0, 32 * 32 * 6)
            glBindVertexArray(0)
        }

        // Отрисовка протонов (синие)
        for (i in 0 until 3) {
            val angle = rotationAngle * protonSpeeds[i] + PI.toFloat()
            val x = (orbitRadii[i] + 1.5f) * cos(angle.toDouble()).toFloat()
            val z = (orbitRadii[i] + 1.5f) * sin(angle.toDouble()).toFloat()
            val y = (orbitRadii[i] + 1.5f) * 0.3f * cos(angle.toDouble() * 1.2).toFloat()

            val model = org.joml.Matrix4f()
                .translate(x, y, z)
                .scale(protonRadius)

            glUniformMatrix4fv(glGetUniformLocation(shaderProgram, "model"), false, floatArrayToBuffer(model.get(FloatArray(16))))
            glUniform3f(glGetUniformLocation(shaderProgram, "objectColor"),
                protonColor[0], protonColor[1], protonColor[2])

            glBindVertexArray(vao)
            glDrawArrays(GL_TRIANGLES, 0, 32 * 32 * 6)
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

        // Создаем сферу с треугольниками для более качественного отображения
        val sphereData = createSolidSphereData(1.0f, 32, 32)
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

    private fun createSolidSphereData(radius: Float, sectors: Int, stacks: Int): FloatBuffer {
        val vertices = mutableListOf<Float>()

        val sectorStep = 2 * PI.toFloat() / sectors
        val stackStep = PI.toFloat() / stacks

        for (i in 0 until stacks) {
            val stackAngle1 = PI.toFloat() / 2 - i * stackStep
            val stackAngle2 = PI.toFloat() / 2 - (i + 1) * stackStep

            val xy1 = radius * cos(stackAngle1)
            val z1 = radius * sin(stackAngle1)
            val xy2 = radius * cos(stackAngle2)
            val z2 = radius * sin(stackAngle2)

            for (j in 0..sectors) {
                val sectorAngle1 = j * sectorStep
                val sectorAngle2 = (j + 1) * sectorStep

                // Вершины для двух треугольников, образующих квад
                for (k in 0..1) {
                    val sa = if (k == 0) sectorAngle1 else sectorAngle2
                    val stackAngle = if (k == 0) stackAngle1 else stackAngle2
                    val xy = if (k == 0) xy1 else xy2
                    val z = if (k == 0) z1 else z2

                    val x = xy * cos(sa)
                    val y = xy * sin(sa)

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

                // Вторая пара вершин для завершения квада
                val x1 = xy1 * cos(sectorAngle1)
                val y1 = xy1 * sin(sectorAngle1)
                val x2 = xy2 * cos(sectorAngle1)
                val y2 = xy2 * sin(sectorAngle1)
                val x3 = xy1 * cos(sectorAngle2)
                val y3 = xy1 * sin(sectorAngle2)
                val x4 = xy2 * cos(sectorAngle2)
                val y4 = xy2 * sin(sectorAngle2)

                // Первый треугольник
                addVertexWithNormal(vertices, x1, y1, z1, radius)
                addVertexWithNormal(vertices, x2, y2, z2, radius)
                addVertexWithNormal(vertices, x3, y3, z1, radius)

                // Второй треугольник
                addVertexWithNormal(vertices, x2, y2, z2, radius)
                addVertexWithNormal(vertices, x4, y4, z2, radius)
                addVertexWithNormal(vertices, x3, y3, z1, radius)
            }
        }

        val buffer = org.lwjgl.BufferUtils.createFloatBuffer(vertices.size)
        vertices.forEach { buffer.put(it) }
        buffer.flip()
        return buffer
    }

    private fun addVertexWithNormal(vertices: MutableList<Float>, x: Float, y: Float, z: Float, radius: Float) {
        vertices.add(x)
        vertices.add(y)
        vertices.add(z)
        vertices.add(x / radius)
        vertices.add(y / radius)
        vertices.add(z / radius)
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
        return floatArrayToBuffer(matrix.get(FloatArray(16)))
    }

    private fun createProjectionMatrix(): FloatBuffer {
        val matrix = org.joml.Matrix4f()
        matrix.perspective(45f * (PI.toFloat() / 180f), 1000f / 800f, 0.1f, 100f)
        return floatArrayToBuffer(matrix.get(FloatArray(16)))
    }

    // Добавляем вспомогательную функцию для преобразования FloatArray в FloatBuffer
    private fun floatArrayToBuffer(array: FloatArray): FloatBuffer {
        val buffer = org.lwjgl.BufferUtils.createFloatBuffer(array.size)
        buffer.put(array)
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