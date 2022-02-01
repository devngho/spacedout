package com.github.devngho.spacedout.equipment

import org.bukkit.Material

class Jetpack : Equipment {
    override val type: EquipmentType = EquipmentType.CHESTPLATE
    override val id: String = "jetpack"
    override val name: String = "제트팩"
    override val graphicMaterial: Material = Material.IRON_INGOT
    override val customModelData: Int = 1
}