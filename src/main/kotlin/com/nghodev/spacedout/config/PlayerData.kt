package com.nghodev.spacedout.config

import com.nghodev.spacedout.Instance
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

object PlayerData {
    private val playerCache: MutableMap<UUID, FileConfiguration> = mutableMapOf()
    fun loadAll(){
        Instance.plugin.server.onlinePlayers.forEach {
            loadPlayerData(it.uniqueId)
        }
        Instance.plugin.server.offlinePlayers.forEach {
            loadPlayerData(it.uniqueId)
        }
    }
    fun initPlayerData(uuid: UUID){
        loadPlayerData(uuid)
        val default = Config.configConfiguration.getConfigurationSection("playerdefault")
        playerCache[uuid]!!.createSection("player.equip")
        playerCache[uuid]!!.set("player.equip", default?.get("equip"))
    }
    fun loadPlayerData(uuid: UUID){
        if (!playerCache.containsKey(uuid)){
            val userdata =
                File(Instance.plugin.dataFolder, File.separator + "players")
            val f = File(userdata, File.separator + uuid + ".yml")
            val playerData: FileConfiguration = YamlConfiguration.loadConfiguration(f)
            playerData.save(f)
            playerCache[uuid] = playerData
        }
    }
    fun getPlayerData(uuid: UUID): FileConfiguration {
        return if (playerCache.containsKey(uuid)){
            playerCache[uuid]!!
        }else{
            val userdata =
                File(Instance.plugin.dataFolder, File.separator + "players")
            val f = File(userdata, File.separator + uuid + ".yml")
            val playerData: FileConfiguration = YamlConfiguration.loadConfiguration(f)
            playerData.save(f)
            playerCache[uuid] = playerData
            playerData
        }
    }
    fun savePlayerData(){
        val userdata =
            File(Instance.plugin.dataFolder, File.separator + "players")
        playerCache.forEach {
            it.value.save(File(userdata, File.separator + it.key + ".yml"))
        }
    }
}