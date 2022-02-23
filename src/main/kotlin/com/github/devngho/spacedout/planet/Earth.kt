/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */



package com.github.devngho.spacedout.planet

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.equipment.Equipment
import de.tr7zw.nbtinjector.javassist.NotFoundException
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.generator.ChunkGenerator

class Earth : Planet {
    override val codeName: String = "earth"
    override var name: String = "planets.$codeName"
    //사용하지 않음
    override val chunkGenerator: ChunkGenerator = Mercury.MercuryGenerator()
    override var pos: Double = 1.0
    override var graphicMaterial: Material = Material.GRASS_BLOCK
    override var worldBorderSize: Double = 0.0
    override var description: String = "planets.${codeName}_description"
    override val needEquipments: MutableList<Equipment> = mutableListOf()
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
    override fun configWorld(world: World) {

    }
    override fun initPlanetConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("position", 1.0)
        configurationSection.set("graphicmaterial", "GRASS_BLOCK")
    }

    override fun loadPlanetConfig(configurationSection: ConfigurationSection): Boolean {
        if (!configurationSection.contains("position")) return true
        pos = configurationSection.getDouble("position", 1.0)
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "GRASS_BLOCK")!!.uppercase(), false) ?: Material.GRASS_BLOCK
        return false
    }
}