/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.rocket

import com.github.devngho.nplug.api.structure.Structure
import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.config.StructureLoader
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import com.github.devngho.spacedout.util.Pair
import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.File

class BigLavaEngine(override var height: Int = 10, override var maxFuelHeight: Int = 50, override var maxHeight: Int = 60, override var graphicMaterial: Material = Material.LAVA_BUCKET, override var fuelDistanceRatio: Double = 0.25, override var speedDistanceRatio: Double = 0.0075) : Engine {
    override val supportFuel = Material.LAVA_BUCKET
    override val id: String = "biglavaengine"
    override var name: String = "modules.${id}"
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.COBBLESTONE, 50), Pair(Material.STONE, 50), Pair(Material.IRON_INGOT, 30), Pair(Material.OBSIDIAN, 30), Pair(Material.DIAMOND_BLOCK, 5))
    override val sizeY: Int = 8
    override val moduleType: ModuleType = ModuleType.ENGINE
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
    override val structure: Structure = StructureLoader.loadFromFile(File(Instance.plugin.dataFolder.absolutePath + File.separator + "resource/modules" + File.separator + "$id.json"))
    override val useStructure: Boolean = true
    override val protectionRange: Int = 3

    override fun render(rocket: RocketDevice, position: Location) {
        for (x in -2..2) {
            for (z in -2..2) {
                position.clone().add(x.toDouble(), 0.0, z.toDouble()).block.type = Material.IRON_BLOCK
            }
        }
        for (x in -1..1) {
            for (z in -1..1) {
                position.clone().add(x.toDouble(), 1.0, z.toDouble()).block.type = Material.RED_CONCRETE
            }
        }
        for (x in -1..1) {
            for (z in -1..1) {
                position.clone().add(x.toDouble(), 2.0, z.toDouble()).block.type = Material.RED_CONCRETE
            }
        }
        for (x in -1..1) {
            for (z in -1..1) {
                position.clone().add(x.toDouble(), 3.0, z.toDouble()).block.type = Material.IRON_BLOCK
            }
        }
    }

    override fun newInstance(): Module {
        return BigLavaEngine(height, maxFuelHeight, maxHeight, graphicMaterial, fuelDistanceRatio, speedDistanceRatio)
    }

    override fun initModuleConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("height", 10)
        configurationSection.set("maxfuelheight", 20)
        configurationSection.set("maxheight", 40)
        configurationSection.set("graphicmaterial", "LAVA_BUCKET")
        configurationSection.set("fueldistanceratio", 0.3)
        configurationSection.set("speeddistanceratio", 0.075)
    }

    override fun loadModuleConfig(configurationSection: ConfigurationSection) {
        height = configurationSection.getInt("height", 10)
        maxFuelHeight = configurationSection.getInt("maxfuelheight", 20)
        maxHeight = configurationSection.getInt("maxheight", 20)
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "LAVA_BUCKET")!!.uppercase(), false) ?: Material.LAVA_BUCKET
        fuelDistanceRatio = configurationSection.getDouble("fueldistanceratio", 0.3)
        speedDistanceRatio = configurationSection.getDouble("speeddistanceratio", 0.075)
    }

    override fun loadModuleValue(map: MutableMap<Any, Any>) {}
    override fun saveModuleValue(): MutableMap<Any, Any> {return mutableMapOf() }
    override fun renderLore(locale: String): List<Component> { return emptyList() }
    override fun onClick(event: InventoryClickEvent) {}
}