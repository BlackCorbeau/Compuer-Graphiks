import org.jetbrains.kotlin.gradle.utils.IMPLEMENTATION

plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "org.exampl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.3"
val lwjglNatives = "natives-linux" // Для NixOS используем стандартные Linux-нативы

dependencies {
    testImplementation(kotlin("test"))
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")

    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
}

application {
    mainClass.set("org.exampl.MainKt")
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}