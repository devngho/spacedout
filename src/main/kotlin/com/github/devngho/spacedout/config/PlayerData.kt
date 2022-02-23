/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.config

import com.github.devngho.spacedout.Instance
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

object PlayerData {
    private val playerCache: MutableMap<UUID, FileConfiguration> = mutableMapOf()
    fun loadAll(){
        Instance.plugin.server.onlinePlayers.forEach {
            val userdata =
                File(Instance.plugin.dataFolder, File.separator + "players")
            val f = File(userdata, File.separator + it.uniqueId + ".yml")
            if (!f.exists()){
                initPlayerData(it.uniqueId)
            }
            loadPlayerData(it.uniqueId)
        }
        Instance.plugin.server.offlinePlayers.forEach {
            val userdata =
                File(Instance.plugin.dataFolder, File.separator + "players")
            val f = File(userdata, File.separator + it.uniqueId + ".yml")
            if (!f.exists()){
                initPlayerData(it.uniqueId)
            }
            loadPlayerData(it.uniqueId)
        }
    }
    fun initPlayerData(uuid: UUID){
        loadPlayerData(uuid)
        val default = Config.configConfiguration.getConfigurationSection("playerdefault")
        playerCache[uuid]!!.createSection("player.equip")
        playerCache[uuid]!!.set("player.equip", default?.get("equip"))
        val userdata =
            File(com.github.devngho.spacedout.Instance.plugin.dataFolder, File.separator + "players")
        val f = File(userdata, File.separator + uuid + ".yml")
        playerCache[uuid]!!.save(f)
    }
    private fun loadPlayerData(uuid: UUID){
        if (!playerCache.containsKey(uuid)){
            val userdata =
                File(com.github.devngho.spacedout.Instance.plugin.dataFolder, File.separator + "players")
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
                File(com.github.devngho.spacedout.Instance.plugin.dataFolder, File.separator + "players")
            val f = File(userdata, File.separator + uuid + ".yml")
            val playerData: FileConfiguration = YamlConfiguration.loadConfiguration(f)
            playerData.save(f)
            playerCache[uuid] = playerData
            playerData
        }
    }
    fun savePlayerData(){
        val userdata =
            File(com.github.devngho.spacedout.Instance.plugin.dataFolder, File.separator + "players")
        playerCache.forEach {
            it.value.save(File(userdata, File.separator + it.key + ".yml"))
        }
    }
}