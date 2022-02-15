package com.github.devngho.spacedout.config

import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.equipment.EquipmentType
import com.github.devngho.spacedout.planet.PlanetManager
import com.github.devngho.spacedout.rocket.ModuleManager
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
            configConfiguration.createSection("module")
            ModuleManager.modules.forEach {
                configConfiguration.createSection("module.${it.id}")
                it.initModuleConfig(configConfiguration.getConfigurationSection("module.${it.id}")!!)
            }
            configConfiguration.createSection("playerdefault")
            configConfiguration.createSection("playerdefault.equip")
            EquipmentType.values().forEach {
                configConfiguration.set("playerdefault.equip.${it.name}", null)
            }
            configConfiguration.createSection("planets")
            configConfiguration.set("planets.useworldborder", false)
            configConfiguration.createSection("server")
            configConfiguration.set("server.requireresourcepack", true)
        }
        PlanetManager.planets.forEach {
            val planetConfig = configConfiguration.getConfigurationSection("planet.${it.first.codeName}")
            try {
                it.first.loadPlanetConfig(planetConfig!!)
            }catch (e: Exception){
                it.first.initPlanetConfig(planetConfig!!)
            }
        }
        ModuleManager.modules.forEach {
            val moduleConfig = configConfiguration.getConfigurationSection("module.${it.id}")
            try {
                it.loadModuleConfig(moduleConfig!!)
            }catch (e: Exception){
                it.initModuleConfig(moduleConfig!!)
            }
        }
        configConfiguration.save(configData)
    }
    fun saveConfigs(){
        val configData =
            File(Instance.plugin.dataFolder, File.separator + "config.yml")
        configConfiguration.save(configData)
    }
}