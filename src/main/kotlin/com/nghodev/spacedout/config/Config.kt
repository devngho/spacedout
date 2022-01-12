package com.nghodev.spacedout.config

import com.nghodev.spacedout.Instance
import com.nghodev.spacedout.planet.PlanetManager
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object Config {
    lateinit var configConfiguration: FileConfiguration
    fun loadConfigs(){
        val configData =
            File(Instance.plugin.dataFolder, File.separator + "config.yml")
        configConfiguration = YamlConfiguration.loadConfiguration(configData)
        if (!configData.exists()){
            configConfiguration.createSection("planet")
            PlanetManager.planets.forEach {
                configConfiguration.createSection("planet.${it.first.codeName}")
                it.first.initPlanetConfig(configConfiguration.getConfigurationSection("planet.${it.first.codeName}")!!)
            }
        }
        PlanetManager.planets.forEach {
            it.first.loadPlanetConfig(configConfiguration.getConfigurationSection("planet.${it.first.codeName}")!!)
        }
        configConfiguration.save(configData)
    }
    fun saveConfigs(){
        val configData =
            File(Instance.plugin.dataFolder, File.separator + "config.yml")
        configConfiguration.save(configData)
    }
}