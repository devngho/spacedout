package com.github.devngho.spacedout.config

import com.github.devngho.nplug.api.structure.Structure
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.util.Vector
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileNotFoundException
import java.io.Reader

@Suppress("unused")
object StructureLoader {
    fun loadFromFile(file: File): Structure {
        if (file.exists()){
            val json = JSONParser().parse(file.bufferedReader()) as JSONArray
            val blocks = json.map {
                if (it is JSONObject) {
                    val vectorObj = it["pos"] as JSONObject
                    val vector = Vector(vectorObj["x"] as Double, vectorObj["y"] as Double, vectorObj["z"] as Double)
                    val material = Material.getMaterial(it["material"] as String) ?: Material.AIR
                    Pair(vector, material)
                }else {
                    Pair(Vector(0.0, 0.0, 0.0), Material.AIR)
                }
            }.toMutableList()
            return Structure.createFrom(blocks)
        }else{
            throw FileNotFoundException("Structure file not exists.")
        }
    }
    fun loadFromReader(file: Reader): Structure {
        val json = JSONParser().parse(file) as JSONArray
        val blocks = json.map {
            if (it is JSONObject) {
                val vectorObj = it["pos"] as JSONObject
                val vector = Vector(vectorObj["x"] as Double, vectorObj["y"] as Double, vectorObj["z"] as Double)
                val material = Material.getMaterial(it["material"] as String) ?: Material.AIR
                Pair(vector, material)
            } else {
                Pair(Vector(0.0, 0.0, 0.0), Material.AIR)
            }
        }.toMutableList()
        return Structure.createFrom(blocks)
    }
    fun placeAtWorld(location: Location, structure: Structure){
        structure.blocks.forEach {
            val blockPosition: Location = location.clone().add(it.first)
            location.world.getBlockAt(blockPosition).type = it.second
        }
    }
}