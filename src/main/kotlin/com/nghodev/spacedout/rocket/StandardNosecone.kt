package com.nghodev.spacedout.rocket

import org.bukkit.Location
import org.bukkit.Material

class StandardNosecone : Module {
    override val height: Int = 3
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.STONE, 10))
    override val sizeY: Int = 1
    override val name: String = "일반 노즈콘"
    override val graphicMaterial: Material = Material.STONE_SLAB

    override fun render(rocket: RocketDevice, position: Location) {
        for (x in -1..1) {
            for (z in -1..1) {
                position.clone().add(x.toDouble(), 0.0, z.toDouble()).block.type = Material.STONE_SLAB
            }
        }
    }

    override val moduleType: ModuleType = ModuleType.NOSECONE

}