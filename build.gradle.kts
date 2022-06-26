plugins {
    kotlin("jvm") version "1.6.10"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
}

group = "com.github.devngho"
version = "v1.1"

repositories {
    mavenCentral()
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven("https://repo.mattstudios.me/artifactory/public/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
}

dependencies {
    implementation("dev.triumphteam:triumph-gui:3.1.2")
    implementation("com.github.devngho:nplug:v0.1-alpha25")

    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))

    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("dev.jorel.CommandAPI:commandapi-core:8.1.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create <MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = "spacedout"
            version = project.version as String

            from(components["kotlin"])
        }
    }
}

tasks {
    jar {
        finalizedBy(shadowJar)
    }
    runServer {
        minecraftVersion("1.18.2")
    }
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "com.github.devngho.spacedout.Plugin"
    apiVersion = "1.18"
    authors = listOf("ngho")
    depend = listOf("CommandAPI")
    libraries = listOf("org.jetbrains.kotlin:kotlin-reflect:1.6.10", "org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    version = "1.1"
}
