plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.3"
val lwjglNatives = "natives-linux" // Для NixOS используем стандартные Linux-нативы

dependencies {
    testImplementation(kotlin("test"))
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    // Основные LWJGL модули
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")

    // Дополнительные модули
    implementation("org.lwjgl:lwjgl-stb")
    implementation("org.lwjgl:lwjgl-nanovg")
    implementation("org.lwjgl:lwjgl-nfd")
    implementation("org.lwjgl:lwjgl-openal")
    implementation("org.joml:joml:1.10.5")

    // Нативные библиотеки
    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-nanovg::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-nfd::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-openal::$lwjglNatives")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("org.exampl.MainKt") // Укажите главный класс
}

kotlin {
    jvmToolchain(21)
}