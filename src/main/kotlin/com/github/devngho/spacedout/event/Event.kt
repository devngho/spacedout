package com.github.devngho.spacedout.event

import com.github.devngho.spacedout.buildable.BuildableManager
import com.github.devngho.spacedout.config.Config
import com.github.devngho.spacedout.config.PlayerData
import com.github.devngho.spacedout.config.RocketData
import com.github.devngho.spacedout.equipment.EquipmentManager
import com.github.devngho.spacedout.rocket.RocketManager
import com.github.devngho.spacedout.rocket.onUseRocketInstaller
import com.github.devngho.spacedout.rocket.rocketInstaller
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.EntityBlockFormEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.WorldSaveEvent


class Event : Listener {
    @EventHandler
    fun onUseItem(event: PlayerInteractEvent){
        if (event.hasBlock()){
            BuildableManager.built.find { it.location.toBlockLocation() ==  event.clickedBlock?.location!!.toBlockLocation()}?.interact(event)
            if (event.hasItem()){
                val buildable = BuildableManager.buildable.find { it.placeItemMaterial == event.item?.type && it.placeItemName == event.item?.itemMeta?.displayName()?.examinableName()}
                if (buildable != null) {
                    val loc = event.clickedBlock?.location!!
                    loc.add(0.0, 1.0, 0.0)
                    loc.block.type = buildable.displayBlock
                    BuildableManager.built.add(buildable.clone(loc))
                    event.player.inventory.setItemInMainHand(event.item?.subtract())
                }
                if (event.item?.asOne() == rocketInstaller){
                    onUseRocketInstaller(event)
                }
            }
        }
    }
    @Suppress("unused")
    @EventHandler
    fun onSave(event: WorldSaveEvent){
        Config.saveConfigs()
        PlayerData.savePlayerData()
        RocketData.saveRocketData()
    }
    @EventHandler
    fun onJoin(event: PlayerJoinEvent){
        event.player.setResourcePack(
            "https://github.com/devngho/spacedout/releases/latest/download/resourcepack.zip", "", Config.configConfiguration.getBoolean("server.requireresourcepack", true), Component.text("Spacedout 플러그인 아이템의 렌더링을 위해 리소스팩을 적용해야 합니다."))
        EquipmentManager.generatePlayerGui(event.player)
        if(!event.player.hasPlayedBefore()){
            PlayerData.initPlayerData(event.player.uniqueId)
        }
    }
    @EventHandler
    fun onBreakAtRocket(event: BlockBreakEvent){
        val loc = event.block.location.toVector()
        loc.y = 0.0
        event.isCancelled = RocketManager.rockets.find {
            val rocketRawLoc = it.installedLocation
            val rocketLocation = Location(rocketRawLoc.world, rocketRawLoc.x, rocketRawLoc.y, rocketRawLoc.z)
            rocketLocation.y = 0.0
            var check = loc.distance(rocketLocation.toVector()) <= (it.modules.maxOf { m -> m.protectionRange })
            if (check && (event.block.location.y > it.modules.sumOf { m -> m.sizeY } + it.installedLocation.y || (event.block.location.y < it.installedLocation.y))){
                check = false
            }
            check
        } != null
    }
    @EventHandler
    fun onPlaceAtRocket(event: BlockPlaceEvent){
        val loc = event.block.location.toVector()
        loc.y = 0.0
        event.isCancelled = RocketManager.rockets.find {
            val rocketRawLoc = it.installedLocation
            val rocketLocation = Location(rocketRawLoc.world, rocketRawLoc.x, rocketRawLoc.y, rocketRawLoc.z)
            rocketLocation.y = 0.0
            var check = loc.distance(rocketLocation.toVector()) <= (it.modules.maxOf { m -> m.protectionRange })
            if (check && (event.block.location.y > it.modules.sumOf { m -> m.sizeY } + it.installedLocation.y || (event.block.location.y < it.installedLocation.y))){
                check = false
            }
            check
        } != null
    }
    @EventHandler
    fun onExplodeBlock(event: BlockExplodeEvent){
        var ignoredBlocks = 0
        event.blockList().toList().forEachIndexed { i, b ->
            val loc = b.location.toVector()
            loc.y = 0.0
            if (RocketManager.rockets.find {
                val rocketRawLoc = it.installedLocation
                val rocketLocation = Location(rocketRawLoc.world, rocketRawLoc.x, rocketRawLoc.y, rocketRawLoc.z)
                rocketLocation.y = 0.0
                var check = loc.distance(rocketLocation.toVector()) <= (it.modules.maxOf { m -> m.protectionRange })
                if (check && (b.location.y > it.modules.sumOf { m -> m.sizeY } + it.installedLocation.y || (b.location.y < it.installedLocation.y))){
                    check = false
                }
                check
            } != null){
                event.blockList().removeAt(i - ignoredBlocks)
                ignoredBlocks += 1
            }
        }
    }
    @EventHandler
    fun onExplodeEntity(event: EntityExplodeEvent){
        var ignoredBlocks = 0
        event.blockList().toList().forEachIndexed { i, b ->
            val loc = b.location.toVector()
            loc.y = 0.0
            if (RocketManager.rockets.find {
                    val rocketRawLoc = it.installedLocation
                    val rocketLocation = Location(rocketRawLoc.world, rocketRawLoc.x, rocketRawLoc.y, rocketRawLoc.z)
                    rocketLocation.y = 0.0
                    var check = loc.distance(rocketLocation.toVector()) <= (it.modules.maxOf { m -> m.protectionRange })
                    if (check && (b.location.y > it.modules.sumOf { m -> m.sizeY } + it.installedLocation.y || (b.location.y < it.installedLocation.y))){
                        check = false
                    }
                    check
                } != null){
                event.blockList().removeAt(i - ignoredBlocks)
                ignoredBlocks += 1
            }
        }
    }
    @EventHandler
    fun onFlow(event: BlockFromToEvent){
        val loc = event.block.location.toVector()
        loc.y = 0.0
        event.isCancelled = RocketManager.rockets.find {
            val rocketRawLoc = it.installedLocation
            val rocketLocation = Location(rocketRawLoc.world, rocketRawLoc.x, rocketRawLoc.y, rocketRawLoc.z)
            rocketLocation.y = 0.0
            var check = loc.distance(rocketLocation.toVector()) <= (it.modules.maxOf { m -> m.protectionRange })
            if (check && (event.block.location.y > it.modules.sumOf { m -> m.sizeY } + it.installedLocation.y || (event.block.location.y < it.installedLocation.y))){
                check = false
            }
            check
        } != null
    }
    @EventHandler
    fun onBucket(event: PlayerBucketEmptyEvent){
        val loc = event.block.location.toVector()
        loc.y = 0.0
        event.isCancelled = RocketManager.rockets.find {
            val rocketRawLoc = it.installedLocation
            val rocketLocation = Location(rocketRawLoc.world, rocketRawLoc.x, rocketRawLoc.y, rocketRawLoc.z)
            rocketLocation.y = 0.0
            var check = loc.distance(rocketLocation.toVector()) <= (it.modules.maxOf { m -> m.protectionRange })
            if (check && (event.block.location.y > it.modules.sumOf { m -> m.sizeY } + it.installedLocation.y || (event.block.location.y < it.installedLocation.y))){
                check = false
            }
            check
        } != null
    }
    @EventHandler
    fun onTnt(event: EntityBlockFormEvent){
        val loc = event.block.location.toVector()
        loc.y = 0.0
        event.isCancelled = RocketManager.rockets.find {
            val rocketRawLoc = it.installedLocation
            val rocketLocation = Location(rocketRawLoc.world, rocketRawLoc.x, rocketRawLoc.y, rocketRawLoc.z)
            rocketLocation.y = 0.0
            var check = loc.distance(rocketLocation.toVector()) <= (it.modules.maxOf { m -> m.protectionRange })
            if (check && (event.block.location.y > it.modules.sumOf { m -> m.sizeY } + it.installedLocation.y || (event.block.location.y < it.installedLocation.y))){
                check = false
            }
            check
        } != null
    }
}