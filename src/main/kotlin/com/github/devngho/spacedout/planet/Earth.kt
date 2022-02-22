package com.github.devngho.spacedout.planet

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.equipment.Equipment
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

    override fun loadPlanetConfig(configurationSection: ConfigurationSection) {
        pos = configurationSection.getDouble("position", 1.0)
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "GRASS_BLOCK")!!.uppercase(), false) ?: Material.GRASS_BLOCK
    }
}