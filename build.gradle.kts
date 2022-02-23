import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile

plugins {
    kotlin("jvm") version "1.6.10"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
}

group = "com.github.devngho"
version = "v1.0-PR0"

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
    implementation("xyz.xenondevs:particle:1.7")
    implementation(files("lib/nplug.jar"))
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
        minecraftVersion("1.18.1")
    }
    task("downloadPlug") {
        val folder = file(project.projectDir.absolutePath + File.separator + "lib")
        if (folder.exists()) {
            for (file in folder.listFiles()!!) {
                if (!file.isDirectory) {
                    file.delete()
                }
            }
        }
        if (!folder.exists()) folder.mkdir()
        val unzipFolder = file(folder.absolutePath + File.separator + "nplug")
        if (!unzipFolder.exists()) unzipFolder.mkdir()
        downloadFile(uri("https://nightly.link/devngho/nplug/workflows/gradle/master/Package.zip").toURL(), folder.absolutePath + File.separator + "nplug.zip")
        unZip(folder.absolutePath + File.separator + "nplug.zip", unzipFolder.absolutePath)
        unzipFolder.listFiles()!![0].copyTo(file(folder.absolutePath + File.separator + "nplug.jar"))
    }
}

fun downloadFile(url: URL, fileName: String) {
    url.openStream().use { Files.copy(it, Paths.get(fileName)) }
}

fun unZip(zipFilePath: String, targetPath: String) {
    ZipFile(zipFilePath).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            zip.getInputStream(entry).use { input ->
                File(targetPath, entry.name).outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

bukkit {
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    main = "com.github.devngho.spacedout.Plugin"
    apiVersion = "1.18"
    authors = listOf("ngho")
    libraries = listOf("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    depend = listOf("CommandAPI")
    version = "v1.0-PR0"
}
