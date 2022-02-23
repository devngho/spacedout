/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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