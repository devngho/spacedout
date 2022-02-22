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