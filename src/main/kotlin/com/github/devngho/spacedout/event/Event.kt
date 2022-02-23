/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.event

import com.github.devngho.spacedout.Instance
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
import org.bukkit.event.block.BlockPistonEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.EntityBlockFormEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.WorldSaveEvent
import java.io.File


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
    @Suppress("unused_parameter")
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
        val userdata =
            File(Instance.plugin.dataFolder, File.separator + "players")
        val f = File(userdata, File.separator + event.player.uniqueId + ".yml")
        if(!event.player.hasPlayedBefore() || !f.exists()){
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
    fun onInteract(event: PlayerInteractEvent){
        if (event.hasBlock()){
            val loc = event.clickedBlock!!.location.toVector()
            loc.y = 0.0
            event.isCancelled = RocketManager.rockets.find {
                val rocketRawLoc = it.installedLocation
                val rocketLocation = Location(rocketRawLoc.world, rocketRawLoc.x, rocketRawLoc.y, rocketRawLoc.z)
                rocketLocation.y = 0.0
                var check = loc.distance(rocketLocation.toVector()) <= (it.modules.maxOf { m -> m.protectionRange })
                if (check && (event.clickedBlock!!.location.y > it.modules.sumOf { m -> m.sizeY } + it.installedLocation.y || (event.clickedBlock!!.location.y < it.installedLocation.y))){
                    check = false
                }
                check
            } != null
        }
    }
    @EventHandler
    fun onPistonExtend(event: BlockPistonExtendEvent){
        event.blocks.toList().forEach { b ->
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
                event.isCancelled = true
                return@forEach
            }
        }
    }
    @EventHandler
    fun onPistonRetract(event: BlockPistonRetractEvent){
        event.blocks.toList().forEach { b ->
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
                event.isCancelled = true
                return@forEach
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