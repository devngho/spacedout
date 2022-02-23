/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.equipment

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.config.I18n
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

interface Equipment {
    val type: EquipmentType
    val id: String
    val name: String
    val graphicMaterial: Material
    val customModelData: Int
    val addedAddon: Addon
    val recipe: Recipe
}

fun Equipment.toItemStack(locale: String): ItemStack{
    val item = ItemStack(this.graphicMaterial)
    item.itemMeta = item.itemMeta.apply {
        setCustomModelData(this@toItemStack.customModelData)
        displayName(I18n.getComponent(locale, this@toItemStack.name).decoration(TextDecoration.ITALIC, false))
        lore(listOf(
            when(this@toItemStack.type){
            EquipmentType.HELMET -> I18n.getComponent(locale, "equipment.helmet")
            EquipmentType.CHESTPLATE -> I18n.getComponent(locale, "equipment.chestplate")
            EquipmentType.BOOTS -> I18n.getComponent(locale, "equipment.boots")
        }.decoration(TextDecoration.ITALIC, false)
            .color(TextColor.color(255, 255, 255)),
            Component.text("from. ${this@toItemStack.addedAddon.name}").color(TextColor.color(127, 127, 127)))
        )
    }
    return item
}