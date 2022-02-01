package com.nghodev.spacedout.config

import com.nghodev.spacedout.Instance
import com.nghodev.spacedout.equipment.EquipmentType
import com.nghodev.spacedout.planet.PlanetManager
import com.nghodev.spacedout.rocket.ModuleManager
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
        }
        PlanetManager.planets.forEach {
            it.first.loadPlanetConfig(configConfiguration.getConfigurationSection("planet.${it.first.codeName}")!!)
        }
        ModuleManager.modules.forEach {
            it.loadModuleConfig(configConfiguration.getConfigurationSection("module.${it.id}")!!)
        }
        configConfiguration.save(configData)
    }
    fun saveConfigs(){
        val configData =
            File(Instance.plugin.dataFolder, File.separator + "config.yml")
        configConfiguration.save(configData)
    }
}