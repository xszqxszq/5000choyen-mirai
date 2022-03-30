plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("net.mamoe.mirai-console") version "2.10.1"
}

group = "xyz.xszq"
version = "1.0.0"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    implementation("com.soywiz.korlibs.korim:korim:2.7.0")
}

//tasks.processResources {
//    exclude("font")
//}