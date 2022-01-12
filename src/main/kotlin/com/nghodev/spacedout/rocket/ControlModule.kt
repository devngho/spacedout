package com.nghodev.spacedout.rocket

import org.bukkit.Location
import org.bukkit.Material

class ControlModule : Module {
    var isRided = false
    override val name: String = "탑승 모듈"
    override val sizeY: Int = 3
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.STONE, 30), Pair(Material.IRON_INGOT, 5))
    override val height: Int = 10
    override val graphicMaterial: Material = Material.CALCITE
    override val moduleType: ModuleType = ModuleType.NORMAL

    override fun render(rocket: RocketDevice, position: Location) {
        for(y in 0..sizeY) {
            for (x in -1..1) {
                for (z in -1..1) {
                    position.clone().add(x.toDouble(), y.toDouble(), z.toDouble()).block.type = Material.IRON_BLOCK
                }
            }
        }
    }
}