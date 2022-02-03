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
        rocketCache.forEach { t, u ->
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
        rocketCache.forEach {
            it.value.save(File(userdata, File.separator + it.key + ".yml"))
        }
    }
}