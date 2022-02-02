package com.github.devngho.spacedout.rocket

import com.github.devngho.spacedout.equipment.EquipmentManager
import com.github.devngho.spacedout.planet.Planet
import com.github.devngho.spacedout.planet.PlanetManager
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.ScrollType
import dev.triumphteam.gui.guis.Gui
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import kotlin.math.abs
import kotlin.math.round


class RocketDevice(val engine: Engine, val installedLocation: Location){
    val height: Int
            get() {
                return modules.sumOf { it.height }
            }
    var fuelHeight: Int = 0
    var modules: MutableList<Module> = mutableListOf(engine)
    var reachPlanet: Planet? = null

    /**
     * 모듈들을 렌더링합니다.
     */
    fun render(){
        val location = installedLocation.clone()
        modules.forEach {
            it.render(this, location)
            location.add(0.0, it.sizeY.toDouble(), 0.0)
        }
    }

    /**
     * 모듈을 처리합니다.
     */
    fun tick(){
        installedLocation.getNearbyPlayers(4.0).filter { it.isSneaking }.forEach { p ->
            //로켓 UI 렌더
            val gui = Gui.gui()
                .title(Component.text("로켓").decoration(TextDecoration.ITALIC, false))
                .rows(3)
                .create()
            gui.setDefaultClickAction { event ->
                event.isCancelled = true
            }
            gui.setItem(1, 1, ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE).name(Component.text("모듈 (총 중량 $height )").decoration(TextDecoration.ITALIC, false)).asGuiItem())
            modules.forEachIndexed { idx, item ->
                gui.setItem(1, idx+2, ItemBuilder.from(item.graphicMaterial).name(Component.text(item.name).decoration(TextDecoration.ITALIC, false)).lore(
                    listOf(Component.text("모듈 중량 : ${item.height}").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false),
                        Component.text("from. ${item.name}").color(TextColor.color(127, 127, 127)))
                ).asGuiItem())
            }
            val moduleAdder = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).name(Component.text("모듈 추가").decoration(TextDecoration.ITALIC, false)).asGuiItem()
            moduleAdder.setAction {
                val moduleAdderGui = Gui.scrolling(ScrollType.HORIZONTAL)
                    .title(Component.text("추가할 모듈").decoration(TextDecoration.ITALIC, false))
                    .rows(1)
                    .pageSize(9)
                    .create()
                moduleAdderGui.setItem(
                    1,
                    1,
                    ItemBuilder.from(Material.PAPER).name(Component.text("이전").decoration(TextDecoration.ITALIC, false))
                        .asGuiItem { moduleAdderGui.previous() })
                moduleAdderGui.setItem(
                    1,
                    9,
                    ItemBuilder.from(Material.PAPER).name(Component.text("다음").decoration(TextDecoration.ITALIC, false))
                        .asGuiItem { moduleAdderGui.next() })
                ModuleManager.modules.forEach {
                    val moduleItem = ItemBuilder.from(it.graphicMaterial).name(Component.text(it.name).decoration(TextDecoration.ITALIC, false).color(
                        TextColor.color(255, 255, 255)))
                    val moduleLore = mutableListOf<Component>()
                    moduleLore += if (ModuleManager.isPlaceable(engine, modules, it)){
                        Component.text("설치 가능").color(TextColor.color(29, 219, 22)).decoration(TextDecoration.ITALIC, false)
                    }else{
                        Component.text("설치 불가능").color(TextColor.color(201, 0, 0)).decoration(TextDecoration.ITALIC, false)
                    }
                    moduleLore += Component.text("모듈 중량 : ${it.height}").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
                    moduleLore += Component.text("from. ${it.addedAddon.name}").color(TextColor.color(127, 127, 127))
                    moduleItem.lore(moduleLore.toList())
                    moduleAdderGui.addItem(moduleItem.asGuiItem { e ->
                        if (ModuleManager.isPlaceable(engine, modules, it)) {
                            this.modules.add(it.newInstance())
                            this.render()
                            moduleAdderGui.close(e.whoClicked)
                        }
                    })
                }
                moduleAdderGui.setDefaultClickAction { event ->
                    event.isCancelled = true
                }
                moduleAdderGui.open(p)
            }
            gui.setItem(1, 9, moduleAdder)
            val planetSetting = ItemBuilder.from(reachPlanet?.graphicMaterial ?: Material.GREEN_STAINED_GLASS_PANE).name(Component.text("행성").decoration(TextDecoration.ITALIC, false)).asGuiItem()
            planetSetting.setAction { e ->
                    //행성 리스트 UI 렌더
                    val planetGui = Gui.scrolling(ScrollType.HORIZONTAL)
                        .title(Component.text("로켓 목적지").decoration(TextDecoration.ITALIC, false))
                        .rows(1)
                        .pageSize(9)
                        .create()
                    planetGui.setItem(
                        1,
                        1,
                        ItemBuilder.from(Material.PAPER).name(Component.text("이전").decoration(TextDecoration.ITALIC, false))
                            .asGuiItem { planetGui.previous() })
                    planetGui.setItem(
                        1,
                        9,
                        ItemBuilder.from(Material.PAPER).name(Component.text("다음").decoration(TextDecoration.ITALIC, false))
                            .asGuiItem { planetGui.next() })
                    PlanetManager.planets.forEach {
                        // 행성 리스트 렌더
                        val planetItem = ItemBuilder.from(it.first.graphicMaterial).name(Component.text(it.first.name).decoration(TextDecoration.ITALIC, false).color(TextColor.color(255, 255, 255)))
                        val planetLore = mutableListOf<Component>()
                        if (it.second?.name == this.installedLocation.world.name){
                            planetLore += Component.text("현재 행성").decoration(TextDecoration.ITALIC, false)
                        }
                        val distance = abs(
                            (PlanetManager.planets.find { p -> p.second?.name == installedLocation.world.name }?.first?.pos?.toInt() ?: 0) - (it.first.pos.toInt())
                        )
                        planetLore += Component.text(it.first.description).decoration(TextDecoration.ITALIC, false).color(
                            TextColor.color(255, 255, 255))
                        planetLore += Component.text("거리 : ${distance}km").decoration(TextDecoration.ITALIC, false).color(TextColor.color(255, 255, 255))
                        planetLore += Component.text("필요 방호구 : ${it.first.needEquipments.joinToString(postfix = ", ") { j -> j.name }}").decoration(TextDecoration.ITALIC, false).color(
                            TextColor.color(255, 255, 255))
                        planetLore += if (distance <= fuelHeight * engine.fuelDistanceRatio){
                            Component.text("도달 가능").decoration(TextDecoration.ITALIC, false).color(TextColor.color(29, 219, 22))
                        }else{
                            Component.text("도달 불가능").decoration(TextDecoration.ITALIC, false).color(TextColor.color(201, 0, 0))
                        }
                        val equips = EquipmentManager.getPlayerEquipments(p)
                        planetLore += if (it.first.needEquipments.count { c -> !equips.containsValue(c) } == 0){
                            Component.text("방호복 충분함").decoration(TextDecoration.ITALIC, false).color(TextColor.color(29, 219, 22))
                        }else{
                            Component.text("방호복 불충분").decoration(TextDecoration.ITALIC, false).color(TextColor.color(201, 0, 0))
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
            gui.setItem(3, 1, ItemBuilder.from(engine.supportFuel.toMaterial()).name(Component.text("연료 잔량 : ${round( fuelHeight.toDouble() / engine.maxFuelHeight.toDouble() * 1000)/10}%").decoration(TextDecoration.ITALIC, false).color(
                TextColor.color(255, 255, 255))).lore(listOf(Component.text("(${fuelHeight}${engine.supportFuel.getUnit()})").color(
                TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false), Component.text("최대 중량 : ${engine.maxFuelHeight}${engine.supportFuel.getUnit()}").color(
                TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false))).asGuiItem())
            gui.setItem(3, 2, ItemBuilder.from(if (fuelHeight == 0) {Material.RED_STAINED_GLASS_PANE} else {Material.GREEN_STAINED_GLASS_PANE}).name(Component.text("도달 가능 거리 : ${fuelHeight * engine.fuelDistanceRatio}km").decoration(TextDecoration.ITALIC, false).color(
                TextColor.color(255, 255, 255))).asGuiItem())
            val fuelInputItem = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).name(Component.text("연료 투입").decoration(TextDecoration.ITALIC, false))
            val fuelInputItemLore = mutableListOf<Component>()
            fuelInputItemLore += Component.text("좌클릭 : 1개 투입").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
            fuelInputItemLore += Component.text("우클릭 : 10개 투입").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
            fuelInputItemLore += Component.text("SHIFT + 좌클릭 : 64개 투입").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
            fuelInputItemLore += Component.text("호환 가능 : ${
                engine.supportFuel.toMaterial().name }")
                .color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
            fuelInputItem.lore(fuelInputItemLore.toList())
            gui.setItem(3, 9, fuelInputItem.asGuiItem {
                when (it.click){
                    ClickType.LEFT -> {
                        val inv = it.whoClicked.inventory
                        if (engine.maxFuelHeight >= fuelHeight + 1) {
                            if (inv.contains(ItemStack(engine.supportFuel.toMaterial()), 1)) {
                                inv.removeItem(ItemStack(engine.supportFuel.toMaterial(), 1))
                                fuelHeight += 1
                            } else {
                                it.whoClicked.sendMessage(Component.text("아이템 부족!").color(TextColor.color(201, 0, 0)))
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
                    ClickType.RIGHT -> {
                        val inv = it.whoClicked.inventory
                        if (engine.maxFuelHeight >= fuelHeight + 10) {
                            if (inv.contains(ItemStack(engine.supportFuel.toMaterial(), 10))) {
                                inv.removeItem(ItemStack(engine.supportFuel.toMaterial(), 10))
                                fuelHeight += 10
                            } else {
                                it.whoClicked.sendMessage(Component.text("아이템 부족!").color(TextColor.color(201, 0, 0)))
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
                        val inv = it.whoClicked.inventory
                        if (engine.maxFuelHeight >= fuelHeight + 64) {
                            if (inv.contains(ItemStack(engine.supportFuel.toMaterial(), 64))) {
                                inv.removeItem(ItemStack(engine.supportFuel.toMaterial(), 64))
                                fuelHeight += 64
                            } else {
                                it.whoClicked.sendMessage(Component.text("아이템 부족!").color(TextColor.color(201, 0, 0)))
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
                it.isCancelled = true
            })
            gui.open(p)
        }
    }
}