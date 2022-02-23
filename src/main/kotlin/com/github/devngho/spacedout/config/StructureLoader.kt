/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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