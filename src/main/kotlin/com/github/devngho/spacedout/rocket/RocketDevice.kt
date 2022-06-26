/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.rocket

import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.config.Config
import com.github.devngho.spacedout.config.I18n
import com.github.devngho.spacedout.config.I18n.getLang
import com.github.devngho.spacedout.config.StructureLoader
import com.github.devngho.spacedout.equipment.EquipmentManager
import com.github.devngho.spacedout.event.RocketFuelChargeEvent
import com.github.devngho.spacedout.event.RocketLaunchEvent
import com.github.devngho.spacedout.event.RocketModuleAddEvent
import com.github.devngho.spacedout.event.RocketModuleRemoveEvent
import com.github.devngho.spacedout.planet.Planet
import com.github.devngho.spacedout.planet.PlanetManager
import com.github.devngho.spacedout.util.ItemUtil.hasMaterialCount
import com.github.devngho.spacedout.util.ItemUtil.useMaterialCount
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.ScrollType
import dev.triumphteam.gui.guis.Gui
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.*


class RocketDevice(val engine: Engine, val installedLocation: Location, val uniqueId: UUID){
    val height: Int
            get() {
                return modules.sumOf { it.height }
            }
    var fuelHeight: Int = 0
    var modules: MutableList<Module> = mutableListOf(engine)
    var reachPlanet: Planet? = null
    private var isLaunching = false

    init {
        val blockLocation = installedLocation.toBlockLocation()
        installedLocation.apply {
            x = blockLocation.x
            y = blockLocation.y
            z = blockLocation.z
        }
    }

    /**
     * 모듈들을 렌더링합니다.
     */
    fun render(){
        val location = installedLocation.clone()
        modules.forEach {
            if (it.useStructure){
                StructureLoader.placeAtWorld(location, it.structure!!)
            }else {
                it.render(this, location)
            }
            location.add(0.0, it.sizeY.toDouble(), 0.0)
        }
    }

    /**
     * 모듈을 처리합니다.
     */
    fun tick(){
        if (!isLaunching) {
            installedLocation.getNearbyPlayers(Config.configConfiguration.getDouble("rocket.interactiondistance", 4.0)).filter { it.isSneaking }.forEach { p ->
                //로켓 UI 렌더
                renderGui(p)
            }
        }
    }

    private fun renderGui(p: Player){
        val gui = Gui.gui()
            .title(Component.text(I18n.getString(p.getLang(), "rocket.name")).decoration(TextDecoration.ITALIC, false))
            .rows(3)
            .create()
        gui.setDefaultClickAction { event ->
            event.isCancelled = true
        }
        fun updateModules(){
            for (i in 2..8){
                gui.removeItem(1, i)
            }
            gui.setItem(1, 1, ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                .name(Component.text("${I18n.getString(p.getLang(), "module.name")} (${I18n.getString(p.getLang(), "module.allheight")} $height )")
                    .decoration(TextDecoration.ITALIC, false)).asGuiItem())
            modules.forEachIndexed { idx, item ->
                val moduleItemLore = mutableListOf(Component.text("${I18n.getString(p.getLang(), "module.height")} : ${item.height}").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false))
                if (item.moduleType != ModuleType.ENGINE) moduleItemLore += Component.text(I18n.getString(p.getLang(), "module.rightclickremove")).color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
                item.renderLore(p.getLang()).forEach {
                    moduleItemLore.add(it as TextComponent)
                }
                moduleItemLore += Component.text("from. ${item.addedAddon.name}").color(TextColor.color(127, 127, 127))
                gui.setItem(1, idx+2, ItemBuilder.from(item.graphicMaterial).name(Component.text(I18n.getString(p.getLang(), item.name)).decoration(TextDecoration.ITALIC, false)).lore(
                    moduleItemLore.toList()
                ).asGuiItem {
                    if (it.isLeftClick){
                        item.onClick(it)
                        updateModules()
                    }
                    if (it.isRightClick && item.moduleType != ModuleType.ENGINE){
                        item.buildRequires.forEach { b ->
                            it.whoClicked.world.dropItem(it.whoClicked.location, ItemStack(b.first, b.second))
                        }
                        Instance.server.pluginManager.callEvent(RocketModuleRemoveEvent(it.whoClicked as Player, this, item))
                        modules.removeAt(idx)
                        val protectionRange = modules.maxOf { m -> m.protectionRange }
                        for (x in -protectionRange..protectionRange){
                            for (y in -protectionRange..protectionRange){
                                for (z in 0..modules.sumOf { s -> s.sizeY } + item.sizeY){
                                    val resetLocation = installedLocation.clone()
                                    resetLocation.add(x.toDouble(), z.toDouble(), y.toDouble())
                                    val checkLocation = installedLocation.clone()
                                    checkLocation.add(x.toDouble(), 0.0, y.toDouble())
                                    if (installedLocation.distance(checkLocation) <= protectionRange){
                                        resetLocation.world.getBlockAt(resetLocation).type = Material.AIR
                                    }
                                }
                            }
                        }
                        render()
                        updateModules()
                    }
                })
            }
            gui.update()
        }
        updateModules()
        val moduleAdder = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).name(Component.text(I18n.getString(p.getLang(), "module.addmodule")).decoration(TextDecoration.ITALIC, false)).asGuiItem()
        moduleAdder.setAction { ev ->
            val moduleAdderGui = Gui.scrolling(ScrollType.HORIZONTAL)
                .title(Component.text(I18n.getString(p.getLang(), "module.addmodulepick")).decoration(TextDecoration.ITALIC, false))
                .rows(1)
                .pageSize(7)
                .create()
            moduleAdderGui.setItem(
                1,
                1,
                ItemBuilder.from(Material.PAPER).name(Component.text(I18n.getString(p.getLang(), "text.prov")).decoration(TextDecoration.ITALIC, false))
                    .asGuiItem { moduleAdderGui.previous() })
            moduleAdderGui.setItem(
                1,
                9,
                ItemBuilder.from(Material.PAPER).name(Component.text(I18n.getString(p.getLang(), "text.next")).decoration(TextDecoration.ITALIC, false))
                    .asGuiItem { moduleAdderGui.next() })
            ModuleManager.modules.forEach {
                val moduleItem = ItemBuilder.from(it.graphicMaterial).name(Component.text(I18n.getString(p.getLang(), it.name)).decoration(TextDecoration.ITALIC, false).color(
                    TextColor.color(255, 255, 255)))
                val moduleLore = mutableListOf<Component>()
                moduleLore += if (ModuleManager.isPlaceable(engine, modules, it)){
                    Component.text(I18n.getString(p.getLang(), "module.caninstall")).color(TextColor.color(29, 219, 22)).decoration(TextDecoration.ITALIC, false)
                }else{
                    Component.text(I18n.getString(p.getLang(), "module.cantinstall")).color(TextColor.color(201, 0, 0)).decoration(TextDecoration.ITALIC, false)
                }
                moduleLore += if (it.buildRequires.all { a -> ev.whoClicked.hasMaterialCount(a.first, a.second) }){
                    Component.text(I18n.getString(p.getLang(), "module.haveresource")).color(TextColor.color(29, 219, 22)).decoration(TextDecoration.ITALIC, false)
                }else{
                    Component.text(I18n.getString(p.getLang(), "module.nohaveresource")).color(TextColor.color(201, 0, 0)).decoration(TextDecoration.ITALIC, false)
                }
                var requireComp = Component.text("${I18n.getString(p.getLang(), "module.needresource")} : ")
                it.buildRequires.forEachIndexed { i, item ->
                    requireComp = requireComp.append(Component.translatable(item.first.translationKey())).append(Component.text("x${item.second}${if(it.buildRequires.count() - 1 != i){","}else{""}}"))
                }
                moduleLore += requireComp.color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
                moduleLore += Component.text("${I18n.getString(p.getLang(), "module.height")} : ${it.height}").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
                moduleLore += Component.text("from. ${it.addedAddon.name}").color(TextColor.color(127, 127, 127))
                moduleItem.lore(moduleLore.toList())
                moduleAdderGui.addItem(moduleItem.asGuiItem { e ->
                    if (ModuleManager.isPlaceable(engine, modules, it) && it.buildRequires.all { a -> ev.whoClicked.hasMaterialCount(a.first, a.second) }) {
                        this.modules.add(it.newInstance())
                        this.render()
                        it.buildRequires.forEach { i ->
                            ev.whoClicked.useMaterialCount(i.first, i.second)
                        }
                        Instance.server.pluginManager.callEvent(RocketModuleAddEvent(e.whoClicked as Player, this, it.newInstance()))
                        moduleAdderGui.close(e.whoClicked)
                    }else {
                        e.whoClicked.sendMessage(Component.text(I18n.getString(p.getLang(), "module.installfailed")).color(TextColor.color(255, 0, 0)))
                        moduleAdderGui.close(e.whoClicked)
                    }
                })
            }
            moduleAdderGui.setDefaultClickAction { event ->
                event.isCancelled = true
            }
            moduleAdderGui.open(p)
        }
        gui.setItem(3, 3, moduleAdder)
        val planetSetting = ItemBuilder.from(reachPlanet?.graphicMaterial ?: Material.GREEN_STAINED_GLASS_PANE).name(Component.text(I18n.getString(p.getLang(), "planet.name")).decoration(TextDecoration.ITALIC, false)).asGuiItem()
        planetSetting.setAction { e ->
            //행성 리스트 UI 렌더
            val planetGui = Gui.scrolling(ScrollType.HORIZONTAL)
                .title(Component.text(I18n.getString(p.getLang(), "rocket.reachplanet")).decoration(TextDecoration.ITALIC, false))
                .rows(1)
                .pageSize(7)
                .create()
            planetGui.setItem(
                1,
                1,
                ItemBuilder.from(Material.PAPER).name(Component.text(I18n.getString(p.getLang(), "text.prov")).decoration(TextDecoration.ITALIC, false))
                    .asGuiItem { planetGui.previous() })
            planetGui.setItem(
                1,
                9,
                ItemBuilder.from(Material.PAPER).name(Component.text(I18n.getString(p.getLang(), "text.next")).decoration(TextDecoration.ITALIC, false))
                    .asGuiItem { planetGui.next() })
            PlanetManager.planets.forEach {
                // 행성 리스트 렌더
                val planetItem = ItemBuilder.from(it.first.graphicMaterial).name(Component.text(I18n.getString(p.getLang(), it.first.name)).decoration(TextDecoration.ITALIC, false).color(TextColor.color(255, 255, 255)))
                val planetLore = mutableListOf<Component>()
                if (it.second?.name == this.installedLocation.world.name){
                    planetLore += Component.text(I18n.getString(p.getLang(), "planet.hereplanet")).decoration(TextDecoration.ITALIC, false)
                }
                val distance = abs(
                    (PlanetManager.planets.find { p -> p.second?.name == installedLocation.world.name }?.first?.pos?: 0.0) - (it.first.pos)
                )
                planetLore += Component.text(I18n.getString(p.getLang(), it.first.description)).decoration(TextDecoration.ITALIC, false).color(
                    TextColor.color(255, 255, 255))
                planetLore += Component.text("${I18n.getString(p.getLang(), "planet.distance")} : ${distance}AU").decoration(TextDecoration.ITALIC, false).color(TextColor.color(255, 255, 255))
                planetLore += Component.text("${I18n.getString(p.getLang(), "equipment.needequipment")} : ${it.first.needEquipments.joinToString(postfix = ", ") { j -> I18n.getString(p.getLang(), j.name) }}").decoration(TextDecoration.ITALIC, false).color(
                    TextColor.color(255, 255, 255))
                planetLore += if (distance <= fuelHeight * engine.fuelDistanceRatio){
                    Component.text(I18n.getString(p.getLang(), "rocket.canreach")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(29, 219, 22))
                }else{
                    Component.text(I18n.getString(p.getLang(), "rocket.cantreach")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(201, 0, 0))
                }
                planetLore += if (distance * 2 <= fuelHeight * engine.fuelDistanceRatio){
                    Component.text(I18n.getString(p.getLang(), "rocket.canround")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(29, 219, 22))
                }else{
                    Component.text(I18n.getString(p.getLang(), "rocket.cantround")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(201, 0, 0))
                }
                val equips = EquipmentManager.getPlayerEquipments(p)
                planetLore += if (it.first.needEquipments.all { c -> equips.map { m -> m.value.id }.contains(c.id) }){
                    Component.text(I18n.getString(p.getLang(), "rocket.canequipment")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(29, 219, 22))
                }else{
                    Component.text(I18n.getString(p.getLang(), "rocket.cantequipment")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(201, 0, 0))
                }
                planetLore += Component.text("from. ${it.first.addedAddon.name}").color(TextColor.color(127, 127, 127))
                planetItem.lore(planetLore.toList())
                val planetGuiItem = planetItem.asGuiItem()

                planetGuiItem.setAction { _ ->
                    this.reachPlanet = it.first
                    planetGui.close(e.whoClicked)
                }

                planetGui.addItem(planetGuiItem)
            }
            planetGui.setDefaultClickAction { event ->
                event.isCancelled = true
            }
            planetGui.open(p)
        }
        gui.setItem(2, 1, planetSetting)
        fun updateFuel() {
            gui.setItem(
                3, 1, ItemBuilder.from(engine.supportFuel).name(
                    Component.text(
                        "${
                            I18n.getString(
                                p.getLang(),
                                "rocket.fuelheight"
                            )
                        } : ${round(fuelHeight.toDouble() / engine.maxFuelHeight.toDouble() * 1000) / 10}%"
                    ).decoration(TextDecoration.ITALIC, false).color(
                        TextColor.color(255, 255, 255)
                    )
                ).lore(
                    listOf(
                        Component.text("(${fuelHeight})").color(
                            TextColor.color(255, 255, 255)
                        ).decoration(TextDecoration.ITALIC, false),
                        Component.text(
                            "${
                                I18n.getString(
                                    p.getLang(),
                                    "rocket.maxfuelheight"
                                )
                            } : ${engine.maxFuelHeight}"
                        ).color(
                            TextColor.color(255, 255, 255)
                        ).decoration(TextDecoration.ITALIC, false)
                    )
                ).asGuiItem()
            )
            gui.setItem(
                3, 2, ItemBuilder.from(
                    if (fuelHeight == 0) {
                        Material.RED_STAINED_GLASS_PANE
                    } else {
                        Material.GREEN_STAINED_GLASS_PANE
                    }
                ).name(
                    Component.text(
                        "${
                            I18n.getString(
                                p.getLang(),
                                "rocket.reachdistance"
                            )
                        } : ${fuelHeight * engine.fuelDistanceRatio}AU"
                    ).decoration(TextDecoration.ITALIC, false).color(
                        TextColor.color(255, 255, 255)
                    )
                ).asGuiItem()
            )
            val fuelInputItem = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).name(
                Component.text(I18n.getString(p.getLang(), "rocket.addfuel")).decoration(TextDecoration.ITALIC, false)
            )
            val fuelInputItemLore = mutableListOf<Component>()
            fuelInputItemLore += Component.text(
                "${
                    I18n.getString(
                        p.getLang(),
                        "text.leftclick"
                    )
                } : 1"
            ).color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
            fuelInputItemLore += Component.text(
                "${
                    I18n.getString(
                        p.getLang(),
                        "text.rightclick"
                    )
                } : 10"
            ).color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
            fuelInputItemLore += Component.text(
                "${
                    I18n.getString(
                        p.getLang(),
                        "text.shiftclick"
                    )
                } : 64"
            ).color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
            fuelInputItemLore += Component.text("${I18n.getString(p.getLang(), "rocket.compatiblefuel")} : ")
                .append(Component.translatable(engine.supportFuel.translationKey()))
                .color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
            fuelInputItem.lore(fuelInputItemLore.toList())
            gui.setItem(3, 2, fuelInputItem.asGuiItem {
                when (it.click) {
                    ClickType.LEFT -> {
                        if (engine.maxFuelHeight >= fuelHeight + 1) {
                            if (it.whoClicked.hasMaterialCount(engine.supportFuel, 1)) {
                                it.whoClicked.useMaterialCount(engine.supportFuel, 1)
                                fuelHeight += 1
                                Instance.server.pluginManager.callEvent(
                                    RocketFuelChargeEvent(
                                        it.whoClicked as Player,
                                        this,
                                        1
                                    )
                                )
                            } else {
                                it.whoClicked.sendMessage(
                                    Component.text(I18n.getString(p.getLang(), "rocket.itemneed"))
                                        .color(TextColor.color(201, 0, 0))
                                )
                            }
                        }
                    }
                    ClickType.RIGHT -> {
                        if (engine.maxFuelHeight >= fuelHeight + 10) {
                            if (it.whoClicked.hasMaterialCount(engine.supportFuel, 10)) {
                                it.whoClicked.useMaterialCount(engine.supportFuel, 10)
                                fuelHeight += 10
                                Instance.server.pluginManager.callEvent(
                                    RocketFuelChargeEvent(
                                        it.whoClicked as Player,
                                        this,
                                        10
                                    )
                                )
                            } else {
                                it.whoClicked.sendMessage(
                                    Component.text(I18n.getString(p.getLang(), "rocket.itemneed"))
                                        .color(TextColor.color(201, 0, 0))
                                )
                                it.whoClicked.playSound(
                                    Sound.sound(
                                        org.bukkit.Sound.BLOCK_ANVIL_PLACE,
                                        Sound.Source.MASTER,
                                        1f,
                                        1f
                                    )
                                )
                            }
                        }
                    }
                    ClickType.SHIFT_LEFT -> {
                        if (engine.maxFuelHeight >= fuelHeight + 64) {
                            if (it.whoClicked.hasMaterialCount(engine.supportFuel, 64)) {
                                it.whoClicked.useMaterialCount(engine.supportFuel, 64)
                                fuelHeight += 64
                                Instance.server.pluginManager.callEvent(
                                    RocketFuelChargeEvent(
                                        it.whoClicked as Player,
                                        this,
                                        64
                                    )
                                )
                            } else {
                                it.whoClicked.sendMessage(
                                    Component.text(I18n.getString(p.getLang(), "rocket.itemneed"))
                                        .color(TextColor.color(201, 0, 0))
                                )
                                it.whoClicked.playSound(
                                    Sound.sound(
                                        org.bukkit.Sound.BLOCK_ANVIL_PLACE,
                                        Sound.Source.MASTER,
                                        1f,
                                        1f
                                    )
                                )
                            }
                        }
                    }
                    else -> {}
                }
                updateFuel()
                gui.update()
                it.isCancelled = true
            })
        }
        updateFuel()
        val rocketLaunchItem = ItemBuilder.from(Material.FIREWORK_ROCKET).name(Component.text(I18n.getString(p.getLang(), "rocket.launch")).decoration(TextDecoration.ITALIC, false))
        val rocketLaunchItemLore = mutableListOf<Component>()
        rocketLaunchItemLore += if (modules.find { f -> f.moduleType == ModuleType.ENGINE } != null) {
            Component.text(I18n.getString(p.getLang(), "rocket.engine")).color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
        }else{
            Component.text(I18n.getString(p.getLang(), "rocket.engine")).color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
        }
        rocketLaunchItemLore += if (modules.find { f -> f.id == "controlmodule" } != null) {
            Component.text(I18n.getString(p.getLang(), "modules.controlmodule")).color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
        }else{
            Component.text(I18n.getString(p.getLang(), "modules.controlmodule")).color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
        }
        rocketLaunchItemLore += if (modules.find { f -> f.moduleType == ModuleType.NOSECONE } != null) {
            Component.text(I18n.getString(p.getLang(), "rocket.nosecone")).color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
        }else{
            Component.text(I18n.getString(p.getLang(), "rocket.nosecone")).color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
        }
        rocketLaunchItemLore += if (abs(
                ((PlanetManager.planets.find { pl -> pl.second?.name == installedLocation.world.name }?.first?.pos
                    ?: 0.0) - (this.reachPlanet?.pos ?: 0.0))
            ) <= (fuelHeight * engine.fuelDistanceRatio)
        ) {
            Component.text(I18n.getString(p.getLang(), "rocket.fuel")).color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
        }else{
            Component.text(I18n.getString(p.getLang(), "rocket.fuel")).color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
        }
        rocketLaunchItemLore += if (this.reachPlanet != null) {
            Component.text(I18n.getString(p.getLang(), "rocket.reachplanetpicked")).color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
        }else{
            Component.text(I18n.getString(p.getLang(), "rocket.reachplanetpicked")).color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
        }
        rocketLaunchItemLore += if (modules.find { f -> f.id == "controlmodule" } != null) {
            if (modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.all { (it as ControlModule).ridedPlayer != null }) {
                Component.text(I18n.getString(p.getLang(), "rocket.selectrider")).color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
            }else{
                Component.text(I18n.getString(p.getLang(), "rocket.selectrider")).color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
            }
        }else{
            Component.text(I18n.getString(p.getLang(), "rocket.selectrider")).color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
        }
        rocketLaunchItemLore += if (modules.find { f -> f.id == "controlmodule" } != null) {
            if (modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.all { (it as ControlModule).ridedPlayer != null }) {
                if (modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.all { Instance.server.getOfflinePlayer((it as ControlModule).ridedPlayer!!).isOnline }) {
                    Component.text(I18n.getString(p.getLang(), "rocket.selectrideronline"))
                        .color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
                }else{
                    Component.text(I18n.getString(p.getLang(), "rocket.selectrideronline")).color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
                }
            }else{
                Component.text(I18n.getString(p.getLang(), "rocket.selectrideronline")).color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
            }
        }else{
            Component.text(I18n.getString(p.getLang(), "rocket.selectrideronline")).color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
        }
        rocketLaunchItemLore += if (modules.find { f -> f.id == "controlmodule" } != null && reachPlanet != null) {
            if (modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.all { (it as ControlModule).ridedPlayer != null }) {
                if (modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.all { Instance.server.getOfflinePlayer((it as ControlModule).ridedPlayer!!).isOnline }) {
                    if (
                        modules.filter { f -> f.id == "controlmodule" && f is ControlModule }
                            .map { Instance.server.getOfflinePlayer((it as ControlModule).ridedPlayer!!).player!! }
                            .map {
                                EquipmentManager.getPlayerEquipments(it)
                            }
                            .all { reachPlanet!!.needEquipments.all { c -> it.map { m -> m.value.id }.contains(c.id) } }
                    ){
                        Component.text(I18n.getString(p.getLang(), "rocket.canequipment")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(0, 255, 0))
                    }else{
                        Component.text(I18n.getString(p.getLang(), "rocket.cantequipment")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(255, 0, 0))
                    }
                }
                else{
                    Component.text(I18n.getString(p.getLang(), "rocket.cantequipment")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(255, 0, 0))
                }
            }else{
                Component.text(I18n.getString(p.getLang(), "rocket.cantequipment")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(255, 0, 0))
            }
        }else{
            Component.text(I18n.getString(p.getLang(), "rocket.cantequipment")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(255, 0, 0))
        }
        rocketLaunchItem.lore(rocketLaunchItemLore.toList())
        gui.setItem(3, 4, rocketLaunchItem.asGuiItem {
            gui.close(it.whoClicked)
            if (modules.find { f -> f.moduleType == ModuleType.ENGINE } != null && modules.find { f -> f.id == "controlmodule" } != null && modules.find { f -> f.moduleType == ModuleType.NOSECONE } != null && (abs(
                    ((PlanetManager.planets.find { pl -> pl.second?.name == installedLocation.world.name }?.first?.pos
                        ?: 0.0) - (this.reachPlanet?.pos ?: 0.0))
                ) <= (fuelHeight * engine.fuelDistanceRatio)) && this.reachPlanet != null && modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.all { a -> (a as ControlModule).ridedPlayer != null } && !isLaunching) {
                if (modules.filter { f -> f.id == "controlmodule" && f is ControlModule }
                        .all { a -> Instance.server.getOfflinePlayer((a as ControlModule).ridedPlayer!!).isOnline }) {
                    if (modules.filter { f -> f.id == "controlmodule" && f is ControlModule }
                            .map { m -> Instance.server.getOfflinePlayer((m as ControlModule).ridedPlayer!!).player!! }
                            .map { m -> EquipmentManager.getPlayerEquipments(m) }
                            .all {
                                reachPlanet!!.needEquipments.all { c ->
                                    it.map { m -> m.value.id }.contains(c.id)
                                }
                            }) {
                        val distance = abs(
                            ((PlanetManager.planets.find { pl -> pl.second?.name == installedLocation.world.name }?.first?.pos
                                ?: 0.0) - (this.reachPlanet?.pos ?: 0.0))
                        )
                        modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.forEach { m ->
                            val player = Instance.server.getOfflinePlayer((m as ControlModule).ridedPlayer!!).player!!
                            player.showTitle(
                                Title.title(
                                    Component.text(I18n.getString(p.getLang(), "rocket.launch")),
                                    Component.text(
                                        I18n.getString(p.getLang(), "rocket.reachaftersecond")
                                            .replace("{name}", I18n.getString(p.getLang(), reachPlanet!!.name)).replace(
                                                "{second}",
                                                (round(distance / engine.speedDistanceRatio / 20)).toString()
                                            )
                                    )
                                )
                            )
                        }
                        val reachPlanetWorld = PlanetManager.planets.find { f -> f.first.codeName == this.reachPlanet?.codeName }?.second!!
                        isLaunching = true
                        Instance.server.pluginManager.callEvent(
                            RocketLaunchEvent(
                                this,
                                PlanetManager.planets.find { pl -> pl.second?.name == installedLocation.world.name }?.first,
                                reachPlanet!!
                            )
                        )
                        val protectionRange = modules.maxOf { m -> m.protectionRange }
                        for (x in -protectionRange..protectionRange) {
                            for (y in -protectionRange..protectionRange) {
                                for (z in 0..modules.sumOf { s -> s.sizeY }) {
                                    val resetLocation = installedLocation.clone()
                                    resetLocation.add(x.toDouble(), z.toDouble(), y.toDouble())
                                    val checkLocation = installedLocation.clone()
                                    checkLocation.add(x.toDouble(), 0.0, y.toDouble())
                                    if (installedLocation.distance(checkLocation) <= protectionRange) {
                                        resetLocation.world.getBlockAt(resetLocation).type = Material.AIR
                                    }
                                }
                            }
                        }
                        var launchingRocket = LaunchingRocket(modules, installedLocation.clone(), false)
                        Instance.server.scheduler.scheduleSyncDelayedTask(Instance.plugin, {
                            this.fuelHeight -= ceil(
                                abs(
                                    ((PlanetManager.planets.find { pl -> pl.second?.name == installedLocation.world.name }?.first?.pos
                                        ?: 0.0) - (this.reachPlanet?.pos ?: 0.0)) / engine.fuelDistanceRatio
                                )
                            ).toInt()
                            this.installedLocation.world = reachPlanetWorld
                            this.installedLocation.y = reachPlanetWorld.getHighestBlockYAt(
                                this.installedLocation.x.toInt(),
                                this.installedLocation.z.toInt()
                            ).toDouble() + 1
                            Instance.server.scheduler.scheduleSyncDelayedTask(Instance.plugin, {
                                render()
                                isLaunching = false
                            }, (distance / engine.speedDistanceRatio).toLong())
                            Instance.server.scheduler.scheduleSyncDelayedTask(Instance.plugin, {
                                launchingRocket = LaunchingRocket(modules, installedLocation.clone(), true)
                            }, max((distance / engine.speedDistanceRatio).toLong() - Config.configConfiguration.getLong("rocket.fallinglaunchtick", 100), 0))
                            Instance.server.scheduler.scheduleSyncDelayedTask(Instance.plugin, {
                                render()
                                isLaunching = false
                            }, (distance / engine.speedDistanceRatio).toLong())
                        }, (distance / engine.speedDistanceRatio).toLong())
                        var taskID: Int?
                        taskID = Instance.server.scheduler.scheduleSyncRepeatingTask(Instance.plugin, {
                            modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.forEach { m ->
                                val player =
                                    Instance.server.getOfflinePlayer((m as ControlModule).ridedPlayer!!).player!!
                                player.setGravity(false)
                                player.isInvisible = true
                                player.isInvulnerable = true
                                val loc = launchingRocket.location.clone().apply {
                                    x += Config.configConfiguration.getDouble("launchviewdistance", 12.0)
                                    y += modules.subList(0, modules.indexOfFirst { i -> i.id == "controlmodule" }).sumOf { s -> s.sizeY } + Config.configConfiguration.getDouble("launchviewdistance", 12.0)
                                    z += Config.configConfiguration.getDouble("launchviewdistance", 12.0)
                                }
                                loc.direction = launchingRocket.location.toVector().apply {
                                    y += modules.sumOf { s -> s.sizeY } / 2.0
                                }.subtract(player.location.toVector()).normalize()
                                player.teleport(loc)
                            }
                        }, 0, 1)
                        Instance.server.scheduler.scheduleSyncDelayedTask(Instance.plugin, {
                            Instance.server.scheduler.cancelTask(taskID!!)
                            taskID = Instance.server.scheduler.scheduleSyncRepeatingTask(Instance.plugin, {
                                modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.forEach { m ->
                                    val player =
                                        Instance.server.getOfflinePlayer((m as ControlModule).ridedPlayer!!).player!!
                                    player.setGravity(false)
                                    player.isInvisible = true
                                    player.isInvulnerable = true
                                    val loc = launchingRocket.location.clone().apply {
                                        world = reachPlanetWorld
                                        x += Config.configConfiguration.getDouble("launchviewdistance", 12.0)
                                        y += modules.subList(0, modules.indexOfFirst { i -> i.id == "controlmodule" }).sumOf { s -> s.sizeY } + Config.configConfiguration.getDouble("launchviewdistance", 12.0)
                                        z += Config.configConfiguration.getDouble("launchviewdistance", 12.0)
                                    }
                                    loc.direction = launchingRocket.location.toVector().apply {
                                        y += modules.sumOf { s -> s.sizeY } / 2.0
                                    }.subtract(player.location.toVector()).normalize()
                                    player.teleport(loc)
                                }
                            }, 0, 1)
                            Instance.server.scheduler.scheduleSyncDelayedTask(Instance.plugin, {
                                Instance.server.scheduler.cancelTask(taskID!!)
                                modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.forEach { m ->
                                    val player =
                                        Instance.server.getOfflinePlayer((m as ControlModule).ridedPlayer!!).player!!
                                    player.setGravity(true)
                                    player.isInvisible = false
                                    player.isInvulnerable = false
                                    player.teleport(
                                        Location(
                                            reachPlanetWorld,
                                            installedLocation.x, reachPlanetWorld.getHighestBlockYAt(
                                                installedLocation.x.toInt(),
                                                installedLocation.z.toInt()
                                            ).toDouble(), installedLocation.z
                                        )
                                    )
                                }
                            }, (distance / engine.speedDistanceRatio).toLong())
                        }, (distance / engine.speedDistanceRatio).toLong())
                    } else {
                        it.whoClicked.sendMessage(
                            Component.text(I18n.getString(p.getLang(), "module.installfailed"))
                                .color(TextColor.color(255, 0, 0))
                        )
                    }
                } else {
                    it.whoClicked.sendMessage(
                        Component.text(I18n.getString(p.getLang(), "module.installfailed"))
                            .color(TextColor.color(255, 0, 0))
                    )
                }
            } else {
                it.whoClicked.sendMessage(
                    Component.text(I18n.getString(p.getLang(), "module.installfailed"))
                        .color(TextColor.color(255, 0, 0))
                )
            }
        })
        gui.setItem(3, 5, ItemBuilder.from(Material.RED_CONCRETE).name(Component.text(I18n.getString(p.getLang(), "rocket.removerocket")).color(
            TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)).asGuiItem {
            val protectionRange = modules.maxOf { m -> m.protectionRange }
            for (x in -protectionRange..protectionRange){
                for (y in -protectionRange..protectionRange){
                    for (z in 0..protectionRange){
                        val resetLocation = installedLocation.clone()
                        resetLocation.add(x.toDouble(), z.toDouble(), y.toDouble())
                        val checkLocation = installedLocation.clone()
                        checkLocation.add(x.toDouble(), 0.0, y.toDouble())
                        if (installedLocation.distance(checkLocation) <= protectionRange){
                            resetLocation.world.getBlockAt(resetLocation).type = Material.AIR
                        }
                    }
                }
            }
            modules.forEach { m ->
                m.buildRequires.forEach { r ->
                    it.whoClicked.location.world.dropItem(it.whoClicked.location, ItemStack(r.first, r.second))
                }
            }
            RocketManager.rockets.removeIf { r -> r.uniqueId == this.uniqueId }
            gui.close(it.whoClicked)
        })
        gui.open(p)
    }
}