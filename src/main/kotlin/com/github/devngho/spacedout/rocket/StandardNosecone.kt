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

class StandardNosecone : Module {
    override val id: String = "standardnosecone"
    override var height: Int = 3
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.STONE, 10))
    override val sizeY: Int = 3
    override var name: String = "modules.${id}"
    override var graphicMaterial: Material = Material.STONE_SLAB
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
        return StandardNosecone()
    }
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
    override fun loadModuleValue(map: MutableMap<Any, Any>) {}
    override fun saveModuleValue(): MutableMap<Any, Any> {return mutableMapOf() }
    override fun renderLore(locale: String): List<Component> { return emptyList() }
    override fun onClick(event: InventoryClickEvent) {}
}