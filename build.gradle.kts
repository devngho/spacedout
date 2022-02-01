plugins {
    kotlin("jvm") version "1.6.10"
    `maven-publish`
}

group = "com.github.devngho"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven("https://repo.mattstudios.me/artifactory/public/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    implementation("dev.triumphteam:triumph-gui:3.1.1")
    compileOnly("dev.jorel.CommandAPI:commandapi-core:6.5.3")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}