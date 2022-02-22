package com.github.devngho.spacedout.equipment

import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.config.Config
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe

class Jetpack : Equipment {
    override val type: EquipmentType = EquipmentType.CHESTPLATE
    override val id: String = "jetpack"
    override val name: String = "equipments.jetpack"
    override val graphicMaterial: Material = Material.IRON_INGOT
    override val customModelData: Int = 1
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
    override val recipe: Recipe
    init {
        recipe = ShapedRecipe(NamespacedKey(Instance.plugin, "jetpack"), this.toItemStack(Config.configConfiguration.getString("server.defaultlang", "en-us")!!))
        recipe.shape("   ", " g ", "iii")
        recipe.setIngredient('g', Material.GUNPOWDER)
        recipe.setIngredient('i', Material.IRON_INGOT)
    }
}