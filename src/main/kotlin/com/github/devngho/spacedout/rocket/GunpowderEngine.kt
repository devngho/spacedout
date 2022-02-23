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
import com.github.devngho.spacedout.fuel.Fuel
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import com.github.devngho.spacedout.util.Pair
import de.tr7zw.nbtinjector.javassist.NotFoundException
import net.kyori.adventure.text.Component
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.File

class GunpowderEngine : Engine {
    override val id: String = "gunpowderengine"
    override var height: Int = 15
    override var maxFuelHeight: Int = 100
    override var maxHeight: Int = 35
    override var supportFuel: Fuel = Fuel.GUNPOWDER
    override var name: String = "modules.${id}"
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.COBBLESTONE, 50), Pair(Material.STONE, 50), Pair(Material.OBSIDIAN, 30), Pair(Material.IRON_BLOCK, 5))
    override var graphicMaterial: Material = Material.GUNPOWDER
    override val sizeY: Int = 7
    override var fuelDistanceRatio: Double = 0.12
    override val moduleType: ModuleType = ModuleType.ENGINE
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
    override val structure: Structure = StructureLoader.loadFromFile(File(Instance.plugin.dataFolder.absolutePath + File.separator + "resource/modules" + File.separator + "$id.json"))
    override val useStructure: Boolean = true
    override val protectionRange: Int = 4
    override var speedDistanceRatio: Double = 0.015
    override fun initModuleConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("height", 15)
        configurationSection.set("maxfuelheight", 100)
        configurationSection.set("maxheight", 35)
        configurationSection.set("graphicmaterial", "GUNPOWDER")
        configurationSection.set("fueldistanceratio", 0.12)
        configurationSection.set("speeddistanceratio", 0.015)
    }

    override fun loadModuleConfig(configurationSection: ConfigurationSection): Boolean {
        if (!configurationSection.contains("height")) return true
        height = configurationSection.getInt("height", 15)
        maxFuelHeight = configurationSection.getInt("maxfuelheight", 100)
        maxHeight = configurationSection.getInt("maxheight", 35)
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "GUNPOWDER")!!.uppercase(), false) ?: Material.GUNPOWDER
        fuelDistanceRatio = configurationSection.getDouble("fueldistanceratio", 0.1)
        speedDistanceRatio = configurationSection.getDouble("speeddistanceratio", 0.015)
        return false
    }

    override fun render(rocket: RocketDevice, position: Location) {
        /*for (x in -2..2) {
            for (z in -2..2) {
                position.clone().add(x.toDouble(), 0.0, z.toDouble()).block.type = Material.STONE
            }
        }
        for (x in -1..1) {
            for (z in -1..1) {
                position.clone().add(x.toDouble(), 1.0, z.toDouble()).block.type = Material.STONE
            }
        }*/
    }

    override fun newInstance(): Module {
        return GunpowderEngine()
    }

    override fun loadModuleValue(map: MutableMap<Any, Any>) {}
    override fun saveModuleValue(): MutableMap<Any, Any> {return mutableMapOf() }
    override fun renderLore(locale: String): List<Component> { return emptyList() }
    override fun onClick(event: InventoryClickEvent) {}
}