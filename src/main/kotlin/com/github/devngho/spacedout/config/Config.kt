/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
            configConfiguration.set("server.defaultlang", "ko-kr")
            configConfiguration.createSection("rocket")
            configConfiguration.set("rocket.usefallinglaunch", true)
            configConfiguration.set("rocket.fallinglaunchtick", 100)
            configConfiguration.set("rocket.interactiondistance", 4.0)
            configConfiguration.save(configData)
        }
    }
    fun loadPlanetModuleConfigs(){
        val configData =
            File(Instance.plugin.dataFolder, File.separator + "config.yml")
        PlanetManager.planets.forEach {
            configConfiguration.createSection("planet.${it.first.codeName}")
            val planetConfig = configConfiguration.getConfigurationSection("planet.${it.first.codeName}")
            try {
                it.first.loadPlanetConfig(planetConfig!!)
            }catch (e: Exception){
                it.first.initPlanetConfig(planetConfig!!)
            }
        }
        ModuleManager.modules.forEach {
            configConfiguration.createSection("module.${it.id}")
            val moduleConfig = configConfiguration.getConfigurationSection("module.${it.id}")
            try {
                if (it.loadModuleConfig(moduleConfig!!)){
                    it.initModuleConfig(moduleConfig)
                }
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