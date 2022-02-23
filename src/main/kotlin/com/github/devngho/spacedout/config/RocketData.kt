/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.config

import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.rocket.RocketManager
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

object RocketData {
    var rocketCache: MutableMap<UUID, FileConfiguration> = mutableMapOf()
    fun loadAll() {
        val folder = File(Instance.plugin.dataFolder, File.separator + "rockets")
        folder.walk().forEach {
            if (it.isFile) {
                val f = it
                val rocketData: FileConfiguration = YamlConfiguration.loadConfiguration(f)
                rocketData.save(f)
                rocketCache[UUID.fromString(rocketData.getString("rocket.uuid", f.nameWithoutExtension))
                    ?: UUID.fromString(f.nameWithoutExtension)] = rocketData
            }
        }
        rocketCache.forEach { (_, u) ->
            RocketManager.loadRocketDevice(u)
        }
    }
    @Suppress("UNUSED")
    fun loadRocketData(uuid: UUID){
        if (!rocketCache.containsKey(uuid)){
            val userdata =
                File(Instance.plugin.dataFolder, File.separator + "rockets")
            val f = File(userdata, File.separator + uuid + ".yml")
            val playerData: FileConfiguration = YamlConfiguration.loadConfiguration(f)
            playerData.save(f)
            rocketCache[uuid] = playerData
        }
    }
    @Suppress("UNUSED")
    fun getRocketData(uuid: UUID): FileConfiguration {
        RocketManager.saveRocketDevice()
        return if (rocketCache.containsKey(uuid)){
            rocketCache[uuid]!!
        }else{
            val userdata =
                File(Instance.plugin.dataFolder, File.separator + "rockets")
            val f = File(userdata, File.separator + uuid + ".yml")
            val rocketData: FileConfiguration = YamlConfiguration.loadConfiguration(f)
            rocketData.save(f)
            rocketCache[uuid] = rocketData
            rocketData
        }
    }
    fun getRocketData(uuid: UUID, save: Boolean): FileConfiguration {
        if (save) RocketManager.saveRocketDevice()
        return if (rocketCache.containsKey(uuid)){
            rocketCache[uuid]!!
        }else{
            val userdata =
                File(Instance.plugin.dataFolder, File.separator + "rockets")
            val f = File(userdata, File.separator + uuid + ".yml")
            val rocketData: FileConfiguration = YamlConfiguration.loadConfiguration(f)
            rocketData.save(f)
            rocketCache[uuid] = rocketData
            rocketData
        }
    }
    fun saveRocketData(){
        RocketManager.saveRocketDevice()
        val userdata =
            File(Instance.plugin.dataFolder, File.separator + "rockets")
        userdata.walk().forEach { f ->
            if (RocketManager.rockets.find { it.uniqueId.toString() == f.nameWithoutExtension } != null){
                f.delete()
            }
        }
        rocketCache.forEach {
            it.value.save(File(userdata, File.separator + it.key + ".yml"))
        }
    }
}