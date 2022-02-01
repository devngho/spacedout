package com.github.devngho.spacedout.planet

import com.github.devngho.spacedout.equipment.Equipment
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.generator.ChunkGenerator

class Earth : Planet {
    override var name: String = "지구"
    override val codeName: String = "earth"
    //사용하지 않음
    override val chunkGenerator: ChunkGenerator = Mercury.MercuryGenerator()
    override var pos: UInt = 15000u
    override var graphicMaterial: Material = Material.GRASS_BLOCK
    override var worldBorderSize: Double = 0.0
    override var description: String = "우리의 고향."
    override val needEquipments: MutableList<Equipment> = mutableListOf()
    override fun configWorld(world: World) {

    }
    override fun initPlanetConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("position", 15000)
        configurationSection.set("name", "지구")
        configurationSection.set("description", "우리의 고향.")
        configurationSection.set("graphicmaterial", "GRASS_BLOCK")
    }

    override fun loadPlanetConfig(configurationSection: ConfigurationSection) {
        name = configurationSection.getString("name", "지구")!!
        description = configurationSection.getString("description", "우리의 고향.")!!
        pos = configurationSection.getInt("position", 15000).toUInt()
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "GRASS_BLOCK")!!.uppercase(), false) ?: Material.GRASS_BLOCK
    }
}