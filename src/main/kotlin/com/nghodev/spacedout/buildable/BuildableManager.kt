package com.nghodev.spacedout.buildable

import com.nghodev.spacedout.Instance
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

object BuildableManager {
    val builded: MutableList<Buildable> = mutableListOf()
    val buildables: MutableList<Buildable> = mutableListOf()
    fun register(buildable: Buildable){
        buildables += buildable
        val recipe = ShapedRecipe(NamespacedKey(Instance.plugin, buildable.codeName),
            ItemStack(buildable.placeItemMaterial).apply { itemMeta = itemMeta.apply { displayName(Component.text(buildable.placeItemName).decoration(TextDecoration.ITALIC, false)) } })
        recipe.shape(buildable.recipeShape[0], buildable.recipeShape[1], buildable.recipeShape[2])
        buildable.recipeItem.forEach {
            recipe.setIngredient(it.first, it.second)
        }
        Bukkit.addRecipe(recipe)
    }
}