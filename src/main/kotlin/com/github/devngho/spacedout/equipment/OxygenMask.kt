package com.github.devngho.spacedout.equipment

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import org.bukkit.Material

class OxygenMask : Equipment {
    override val type: EquipmentType = EquipmentType.HELMET
    override val id: String = "oxygenmask"
    override val name: String = "산소 마스크"
    override val graphicMaterial: Material = Material.IRON_INGOT
    override val customModelData: Int = 2
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
}