package com.nghodev.spacedout.equipment

import org.bukkit.Material

interface Equipment {
    val type: EquipmentType
    val id: String
    val name: String
    val graphicMaterial: Material
    val customModelData: Int
}