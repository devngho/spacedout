package com.github.devngho.spacedout.equipment

import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.config.Config
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe

class OxygenMask : Equipment {
    override val type: EquipmentType = EquipmentType.HELMET
    override val id: String = "oxygenmask"
    override val name: String = "equipments.oxygenmask"
    override val graphicMaterial: Material = Material.IRON_INGOT
    override val customModelData: Int = 2
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
    override val recipe: Recipe
    init {
        recipe = ShapedRecipe(NamespacedKey(Instance.plugin, "oxygenmask"), this.toItemStack(Config.configConfiguration.getString("server.defaultlang", "en-us")!!))
        recipe.shape("   ", " g ", "i")
        recipe.setIngredient('g', Material.GLASS)
        recipe.setIngredient('i', Material.IRON_INGOT)
    }
}