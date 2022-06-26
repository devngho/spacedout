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
    private var noExists = false
    fun loadConfigs(){
        val configData =
            File(Instance.plugin.dataFolder, File.separator + "config.yml")
        configConfiguration = YamlConfiguration.loadConfiguration(configData)
        if (!configData.exists()){
            noExists = true
            configConfiguration.createSection("planet")
            configConfiguration.createSection("playerdefault")
            configConfiguration.createSection("playerdefault.equip")
            EquipmentType.values().forEach {
                configConfiguration.set("playerdefault.equip.${it.name}", null)
            }
            configConfiguration.set("playerdefault.use_falling", false)
            configConfiguration.createSection("planets")
            configConfiguration.set("planets.useworldborder", false)
            configConfiguration.createSection("server")
            configConfiguration.set("server.requireresourcepack", true)
            configConfiguration.set("server.defaultlang", "ko-kr")
            configConfiguration.createSection("rocket")
            configConfiguration.set("rocket.fallinglaunchtick", 100)
            configConfiguration.set("rocket.interactiondistance", 4.0)
            configConfiguration.set("rocket.launchviewdistance", 12.0)
        }
    }
    fun loadPlanetModuleConfigs(){
        if (noExists){
            PlanetManager.planets.forEach {
                configConfiguration.createSection("planet.${it.first.codeName}")
                it.first.initPlanetConfig(configConfiguration.getConfigurationSection("planet.${it.first.codeName}")!!)
            }
            ModuleManager.modules.forEach {
                configConfiguration.createSection("module.${it.id}")
                it.initModuleConfig(configConfiguration.getConfigurationSection("module.${it.id}")!!)
            }
        }
        PlanetManager.planets.forEach {
            val planetConfig = configConfiguration.getConfigurationSection("planet.${it.first.codeName}")
            it.first.loadPlanetConfig(planetConfig!!)
        }
        ModuleManager.modules.forEach {
            val moduleConfig = configConfiguration.getConfigurationSection("module")!!.getConfigurationSection(it.id)
            it.loadModuleConfig(moduleConfig!!)
        }
    }
    fun saveConfigs(){
        val configData =
            File(Instance.plugin.dataFolder, File.separator + "config.yml")
        configConfiguration.save(configData)
    }
}