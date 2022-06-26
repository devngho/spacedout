/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.rocket

import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.config.RocketData
import com.github.devngho.spacedout.planet.PlanetManager
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import java.util.*

object RocketManager {
    var rockets: MutableList<RocketDevice> = mutableListOf()
    fun createRocketWithName(rocketName: String, location: Location): RocketDevice {
        val device = RocketDevice(ModuleManager.modules.find { it.name == rocketName && it.moduleType == ModuleType.ENGINE && it is Engine }?.newInstance() as Engine, location, UUID.randomUUID())
        rockets.add(device)
        return device
    }
    fun createRocketWithInstaller(rocketEngine: Engine, location: Location): RocketDevice {
        val device = RocketDevice(rocketEngine.newInstance() as Engine, location, UUID.randomUUID())
        rockets.add(device)
        return device
    }
    internal fun tick(){
        rockets.forEach {
            it.tick()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun loadRocketDevice(conf: FileConfiguration){
        if (conf.getBoolean("vaild", true)){
            val engine = ModuleManager.modules.find { it.id == conf.getString("engine", "coalengine") && it.moduleType == ModuleType.ENGINE && it is Engine }?.newInstance() as Engine
            if (conf.getMapList("moduledatas").isNotEmpty()) {
                engine.loadModuleValue(conf.getMapList("moduledatas")[0] as MutableMap<Any, Any>)
            }
            val device = RocketDevice(engine,
                conf.getLocation("location") ?: Location(Instance.server.worlds[0], 0.toDouble() , 0.toDouble(), 0.toDouble()),
                UUID.fromString(conf.getString("uuid")) ?: UUID.randomUUID())
            device.reachPlanet = PlanetManager.planets.find { it.first.codeName == conf.getString("reachplanet") }?.first
            val moduleData = conf.getMapList("moduledatas")
            device.modules.clear()
            conf.getStringList("modules").forEachIndexed { i, it ->
                val module = ModuleManager.modules.find { f -> f.id == it }
                if (module != null){
                    val instance = module.newInstance()
                    instance.loadModuleValue(moduleData[i] as MutableMap<Any, Any>)
                    device.modules += instance
                }
            }
            device.fuelHeight = conf.getInt("fuelheight", 0)
            rockets.add(device)
        }
    }
    fun saveRocketDevice(){
        val map = rockets.map {
            val conf = RocketData.getRocketData(it.uniqueId, false)
            conf.set("engine", it.engine.id)
            conf.set("location", it.installedLocation)
            conf.set("uuid", it.uniqueId.toString())
            conf.set("reachplanet", it.reachPlanet?.codeName)
            conf.set("modules", it.modules.map { t -> t.id })
            conf.set("moduledatas", it.modules.map { t -> t.saveModuleValue() })
            conf.set("fuelheight", it.fuelHeight)
            Pair(it.uniqueId, conf)
        }
        RocketData.rocketCache = map.toMap().toMutableMap()
    }
}