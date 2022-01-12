package com.nghodev.spacedout.event

import com.nghodev.spacedout.buildable.BuildableManager
import com.nghodev.spacedout.config.Config
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.WorldSaveEvent


class Event : Listener {
    @EventHandler
    fun onUseItem(event: PlayerInteractEvent){
        if (event.hasBlock()){
            BuildableManager.builded.find { it.location.toBlockLocation() ==  event.clickedBlock?.location!!.toBlockLocation()}?.interact(event)
            if (event.hasItem()){
                val buildable = BuildableManager.buildables.find { it.placeItemMaterial == event.item?.type && it.placeItemName == event.item?.itemMeta?.displayName()?.examinableName()}
                if (buildable != null) {
                    val loc = event.clickedBlock?.location!!
                    loc.add(0.0, 1.0, 0.0)
                    loc.block.type = buildable.displayBlock
                    BuildableManager.builded.add(buildable.clone(loc))
                    event.player.inventory.setItemInMainHand(event.item?.subtract())
                }
            }
        }
    }
    @EventHandler
    fun onSave(_event: WorldSaveEvent){
        Config.saveConfigs()
    }
    @EventHandler
    fun onJoin(event: PlayerJoinEvent){
        event.player.setResourcePack("", "", true)
    }
}