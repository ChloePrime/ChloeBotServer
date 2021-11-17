plugins {
    java
    id("io.izzel.taboolib") version "1.26"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
}

taboolib {
    install("common")
    install("platform-bukkit")
    install("module-configuration")
    classifier = null
    version = "6.0.0-7"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v11701:11701:mapped")
    compileOnly("ink.ptms.core:v11701:11701:universal")
    compileOnly(kotlin("stdlib"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.2")
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}