package com.github.devngho.spacedout.rocket

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

class StandardNosecone : Module {
    override val id: String = "standardnosecone"
    override var height: Int = 3
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.STONE, 10))
    override val sizeY: Int = 1
    override var name: String = "일반 노즈콘"
    override var graphicMaterial: Material = Material.STONE_SLAB

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
        configurationSection.set("name", "일반 노즈콘")
        configurationSection.set("graphicmaterial", "STONE_SLAB")
    }

    override fun loadModuleConfig(configurationSection: ConfigurationSection) {
        height = configurationSection.getInt("height", 3)
        name = configurationSection.getString("name", "일반 노즈콘").toString()
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "STONE_SLAB")!!.uppercase(), false) ?: Material.STONE_SLAB
    }

    override fun newInstance(): Module {
        return StandardNosecone()
    }
}