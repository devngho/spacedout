package com.nghodev.spacedout.equipment

import com.nghodev.spacedout.config.PlayerData
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.GuiType
import dev.triumphteam.gui.guis.Gui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

object EquipmentManager {
    val equipments: MutableList<Equipment> = mutableListOf()
    val playerEquipmentGui = mutableMapOf<UUID, Gui>()
    fun getPlayerEquipments(player: Player): MutableMap<EquipmentType, Equipment> {
        val playerEquipments = mutableMapOf<EquipmentType, Equipment>()
        val playerData = PlayerData.getPlayerData(player.uniqueId).getConfigurationSection("player.equip")
        for (key in playerData?.getKeys(false)!!) {
            val eq = equipments.find { playerData.getString(key) == it.id}
            if (eq != null) {
                playerEquipments[EquipmentType.values().find { it.name == key }!!] = eq
            }
        }
        return playerEquipments
    }
    fun generatePlayerGui(player: Player){
        if (!playerEquipmentGui.containsKey(player.uniqueId)) {
            val gui = Gui.gui()
                .type(GuiType.DISPENSER)
                .title(Component.text("방호구").decoration(TextDecoration.ITALIC, false))
                .create()
            gui.disableItemSwap()
            val helmetInfoItem = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).name(Component.text("헬멧").decoration(TextDecoration.ITALIC, false)).asGuiItem { it.isCancelled = true }
            val chestplateInfoItem = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).name(Component.text("갑옷").decoration(TextDecoration.ITALIC, false)).asGuiItem { it.isCancelled = true }
            val bootsInfoItem = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).name(Component.text("부츠").decoration(TextDecoration.ITALIC, false)).asGuiItem { it.isCancelled = true }
            val blockSlotItem = ItemBuilder.from(Material.RED_STAINED_GLASS_PANE).name(Component.text(" ")).asGuiItem { it.isCancelled = true }
            gui.setItem(3, helmetInfoItem)
            gui.setItem(4, chestplateInfoItem)
            gui.setItem(5, bootsInfoItem)
            gui.setItem(6, blockSlotItem)
            gui.setItem(7, blockSlotItem)
            gui.setItem(8, blockSlotItem)
            gui.setOpenGuiAction {
                val playerEquip = PlayerData.getPlayerData(it.player.uniqueId).getConfigurationSection("player.equip")
                it.inventory.setItem(0, equipments.find { f -> playerEquip?.get("HELMET") == f.id }?.toItemStack() ?: ItemStack(Material.AIR))
                it.inventory.setItem(1, equipments.find { f -> playerEquip?.get("CHESTPLATE") == f.id }?.toItemStack() ?: ItemStack(Material.AIR))
                it.inventory.setItem(2, equipments.find { f -> playerEquip?.get("BOOTS") == f.id }?.toItemStack() ?: ItemStack(Material.AIR))
            }
            gui.setCloseGuiAction {
                val helmetItem = it.inventory.getItem(0)
                val chestplateItem = it.inventory.getItem(1)
                val bootsItem = it.inventory.getItem(2)
                val helmetEquipment = if (helmetItem?.itemMeta?.hasCustomModelData() == true) { equipments.find { f -> f.customModelData == helmetItem.itemMeta?.customModelData && f.graphicMaterial == helmetItem.type && f.type == EquipmentType.HELMET } } else { null }
                if (helmetEquipment == null && helmetItem != null){
                    it.player.world.dropItem(it.player.location, helmetItem)
                }else if (helmetItem?.amount?.compareTo(1) == 1){//helmetItem.amount > 1
                    it.player.world.dropItem(it.player.location, helmetItem.asQuantity(helmetItem.amount - 1))
                    PlayerData.getPlayerData(it.player.uniqueId).set("player.equip.HELMET", helmetEquipment?.id)
                }else{
                    PlayerData.getPlayerData(it.player.uniqueId).set("player.equip.HELMET", helmetEquipment?.id)
                }
                val chestplateEquipment = if (chestplateItem?.itemMeta?.hasCustomModelData() == true) { equipments.find { f -> f.customModelData == chestplateItem.itemMeta?.customModelData && f.graphicMaterial == chestplateItem.type && f.type == EquipmentType.CHESTPLATE } } else { null }
                if (chestplateEquipment == null && chestplateItem != null){
                    it.player.world.dropItem(it.player.location, chestplateItem)
                }else if (chestplateItem?.amount?.compareTo(1) == 1){//helmetItem.amount > 1
                    it.player.world.dropItem(it.player.location, chestplateItem.asQuantity(chestplateItem.amount - 1))
                    PlayerData.getPlayerData(it.player.uniqueId).set("player.equip.CHESTPLATE", chestplateEquipment?.id)
                }else{
                    PlayerData.getPlayerData(it.player.uniqueId).set("player.equip.CHESTPLATE", chestplateEquipment?.id)
                }
                val bootsEquipment = if (bootsItem?.itemMeta?.hasCustomModelData() == true) { equipments.find { f -> f.customModelData == bootsItem.itemMeta?.customModelData && f.graphicMaterial == bootsItem.type && f.type == EquipmentType.BOOTS } } else { null }
                if (bootsEquipment == null && bootsItem != null){
                    it.player.world.dropItem(it.player.location, bootsItem)
                }else if (bootsItem?.amount?.compareTo(1) == 1){//helmetItem.amount > 1
                    it.player.world.dropItem(it.player.location, bootsItem.asQuantity(bootsItem.amount - 1))
                    PlayerData.getPlayerData(it.player.uniqueId).set("player.equip.BOOTS", bootsEquipment?.id)
                }else{
                    PlayerData.getPlayerData(it.player.uniqueId).set("player.equip.BOOTS", bootsEquipment?.id)
                }
            }
            playerEquipmentGui[player.uniqueId] = gui
        }
    }
}