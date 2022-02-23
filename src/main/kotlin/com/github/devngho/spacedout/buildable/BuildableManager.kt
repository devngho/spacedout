/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.buildable

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

@Suppress("unused")
object BuildableManager {
    val built: MutableList<Buildable> = mutableListOf()
    val buildable: MutableList<Buildable> = mutableListOf()
    fun register(buildable: Buildable){
        this.buildable += buildable
        val recipe = ShapedRecipe(NamespacedKey(com.github.devngho.spacedout.Instance.plugin, buildable.codeName),
            ItemStack(buildable.placeItemMaterial).apply { itemMeta = itemMeta.apply { displayName(Component.text(buildable.placeItemName).decoration(TextDecoration.ITALIC, false))
            lore(listOf(Component.text("from. ${buildable.addedAddon.name}").color(TextColor.color(127, 127, 127))))} })
        recipe.shape(buildable.recipeShape[0], buildable.recipeShape[1], buildable.recipeShape[2])
        buildable.recipeItem.forEach {
            recipe.setIngredient(it.first, it.second)
        }
        Bukkit.addRecipe(recipe)
    }
}