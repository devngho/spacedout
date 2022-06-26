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

class StandardNosecone(override var height: Int = 3, override var graphicMaterial: Material = Material.STONE_SLAB) : Module {
    override val id: String = "standardnosecone"
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.STONE, 10))
    override val sizeY: Int = 3
    override var name: String = "modules.${id}"
    override val structure: Structure = StructureLoader.loadFromFile(File(Instance.plugin.dataFolder.absolutePath + File.separator + "resource/modules" + File.separator + "$id.json"))
    override val useStructure: Boolean = true
    override val protectionRange: Int = 3

    override fun render(rocket: RocketDevice, position: Location) {
        for (x in -1..1) {
            for (z in -1..1) {
                position.clone().add(x.toDouble(), 0.0, z.toDouble()).block.type = Material.STONE_SLAB
            }
        }
    }

    override val moduleType: ModuleType = ModuleType.NOSECONE
    override fun initModuleConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("height", 3)
        configurationSection.set("graphicmaterial", "STONE_SLAB")
    }

    override fun loadModuleConfig(configurationSection: ConfigurationSection) {
        height = configurationSection.getInt("height", 3)
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "STONE_SLAB")!!.uppercase(), false) ?: Material.STONE_SLAB
    }

    override fun newInstance(): Module {
        return StandardNosecone(height, graphicMaterial)
    }
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
    override fun loadModuleValue(map: MutableMap<Any, Any>) {}
    override fun saveModuleValue(): MutableMap<Any, Any> {return mutableMapOf() }
    override fun renderLore(locale: String): List<Component> { return emptyList() }
    override fun onClick(event: InventoryClickEvent) {}
}