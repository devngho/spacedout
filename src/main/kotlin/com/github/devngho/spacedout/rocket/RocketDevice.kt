package com.github.devngho.spacedout.rocket

import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.equipment.EquipmentManager
import com.github.devngho.spacedout.planet.Planet
import com.github.devngho.spacedout.planet.PlanetManager
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.ScrollType
import dev.triumphteam.gui.guis.Gui
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.round


class RocketDevice(val engine: Engine, val installedLocation: Location, val uniqueId: UUID){
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
                val moduleItemLore = mutableListOf(Component.text("모듈 중량 : ${item.height}").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false))
                if (item.moduleType != ModuleType.ENGINE) moduleItemLore += Component.text("우클릭해 모듈 제거").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
                moduleItemLore += Component.text("from. ${item.addedAddon.name}").color(TextColor.color(127, 127, 127))
                gui.setItem(1, idx+2, ItemBuilder.from(item.graphicMaterial).name(Component.text(item.name).decoration(TextDecoration.ITALIC, false)).lore(
                    moduleItemLore.toList()
                ).asGuiItem {
                    if (it.isRightClick && item.moduleType != ModuleType.ENGINE){
                        item.buildRequires.forEach { b ->
                            it.whoClicked.world.dropItem(it.whoClicked.location, ItemStack(b.first, b.second))
                        }
                        modules.removeAt(idx)
                        for (x in -3..3){
                            for (y in -3..3){
                                for (z in 0..modules.sumOf { s -> s.sizeY } + item.sizeY){
                                    val resetLocation = installedLocation.clone()
                                    resetLocation.add(x.toDouble(), z.toDouble(), y.toDouble())
                                    val checkLocation = installedLocation.clone()
                                    checkLocation.add(x.toDouble(), 0.0, y.toDouble())
                                    if (installedLocation.distance(checkLocation) <= 3){
                                        resetLocation.world.getBlockAt(resetLocation).type = Material.AIR
                                    }
                                }
                            }
                        }
                        render()
                        gui.close(it.whoClicked)
                    }
                })
            }
            val moduleAdder = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).name(Component.text("모듈 추가").decoration(TextDecoration.ITALIC, false)).asGuiItem()
            moduleAdder.setAction { ev ->
                val moduleAdderGui = Gui.scrolling(ScrollType.HORIZONTAL)
                    .title(Component.text("추가할 모듈").decoration(TextDecoration.ITALIC, false))
                    .rows(1)
                    .pageSize(7)
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
                    moduleLore += if (it.buildRequires.all { a -> ev.whoClicked.inventory.contains(ItemStack(a.first, a.second)) }){
                        Component.text("자원 충분").color(TextColor.color(29, 219, 22)).decoration(TextDecoration.ITALIC, false)
                    }else{
                        Component.text("자원 부족").color(TextColor.color(201, 0, 0)).decoration(TextDecoration.ITALIC, false)
                    }
                    var requireComp = Component.text("필요 자원 : ")
                    it.buildRequires.forEachIndexed { i, item ->
                        requireComp = requireComp.append(Component.translatable(item.first.translationKey())).append(Component.text("x${item.second}${if(it.buildRequires.count() - 1 != i){","}else{""}}"))
                    }
                    moduleLore += requireComp.color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
                    moduleLore += Component.text("모듈 중량 : ${it.height}").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
                    moduleLore += Component.text("from. ${it.addedAddon.name}").color(TextColor.color(127, 127, 127))
                    moduleItem.lore(moduleLore.toList())
                    moduleAdderGui.addItem(moduleItem.asGuiItem { e ->
                        if (ModuleManager.isPlaceable(engine, modules, it) && it.buildRequires.all { ev.whoClicked.inventory.contains(ItemStack(it.first, it.second)) }) {
                            this.modules.add(it.newInstance())
                            this.render()
                            it.buildRequires.forEach { i ->
                                ev.whoClicked.inventory.remove(ItemStack(i.first, i.second))
                            }
                            moduleAdderGui.close(e.whoClicked)
                        }else {
                            e.whoClicked.sendMessage(Component.text("설치할 자원이 부족하거나 설치할 수 없습니다.").color(TextColor.color(255, 0, 0)))
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
                        .pageSize(7)
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
                            (PlanetManager.planets.find { p -> p.second?.name == installedLocation.world.name }?.first?.pos?: 0.0) - (it.first.pos)
                        )
                        planetLore += Component.text(it.first.description).decoration(TextDecoration.ITALIC, false).color(
                            TextColor.color(255, 255, 255))
                        planetLore += Component.text("거리 : ${distance}AU").decoration(TextDecoration.ITALIC, false).color(TextColor.color(255, 255, 255))
                        planetLore += Component.text("필요 방호구 : ${it.first.needEquipments.joinToString(postfix = ", ") { j -> j.name }}").decoration(TextDecoration.ITALIC, false).color(
                            TextColor.color(255, 255, 255))
                        planetLore += if (distance <= fuelHeight * engine.fuelDistanceRatio){
                            Component.text("도달 가능").decoration(TextDecoration.ITALIC, false).color(TextColor.color(29, 219, 22))
                        }else{
                            Component.text("도달 불가능").decoration(TextDecoration.ITALIC, false).color(TextColor.color(201, 0, 0))
                        }
                        planetLore += if (distance * 2 <= fuelHeight * engine.fuelDistanceRatio){
                            Component.text("왕복 가능").decoration(TextDecoration.ITALIC, false).color(TextColor.color(29, 219, 22))
                        }else{
                            Component.text("왕복 불가능").decoration(TextDecoration.ITALIC, false).color(TextColor.color(201, 0, 0))
                        }
                        val equips = EquipmentManager.getPlayerEquipments(p)
                        planetLore += if (it.first.needEquipments.all { c -> equips.map { m -> m.value.id }.contains(c.id) }){
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
            gui.setItem(3, 2, ItemBuilder.from(if (fuelHeight == 0) {Material.RED_STAINED_GLASS_PANE} else {Material.GREEN_STAINED_GLASS_PANE}).name(Component.text("도달 가능 거리 : ${fuelHeight * engine.fuelDistanceRatio}AU").decoration(TextDecoration.ITALIC, false).color(
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
            val rocketLaunchItem = ItemBuilder.from(Material.FIREWORK_ROCKET).name(Component.text("로켓 발사!").decoration(TextDecoration.ITALIC, false))
            val rocketLaunchItemLore = mutableListOf<Component>()
            rocketLaunchItemLore += if (modules.find { f -> f.moduleType == ModuleType.ENGINE } != null) {
                Component.text("엔진").color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
            }else{
                Component.text("엔진").color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
            }
            rocketLaunchItemLore += if (modules.find { f -> f.id == "controlmodule" } != null) {
                Component.text("탑승 모듈").color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
            }else{
                Component.text("탑승 모듈").color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
            }
            rocketLaunchItemLore += if (modules.find { f -> f.moduleType == ModuleType.NOSECONE } != null) {
                Component.text("노즈콘").color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
            }else{
                Component.text("노즈콘").color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
            }
            rocketLaunchItemLore += if (abs(
                    ((PlanetManager.planets.find { pl -> pl.second?.name == installedLocation.world.name }?.first?.pos
                        ?: 0.0) - (this.reachPlanet?.pos ?: 0.0))
                ) <= (fuelHeight * engine.fuelDistanceRatio)
            ) {
                Component.text("연료").color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
            }else{
                Component.text("연료").color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
            }
            rocketLaunchItemLore += if (this.reachPlanet != null) {
                Component.text("목적지 설정").color(TextColor.color(0, 255, 0)).decoration(TextDecoration.ITALIC, false)
            }else{
                Component.text("목적지 설정").color(TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)
            }
            rocketLaunchItem.lore(rocketLaunchItemLore.toList())
            gui.setItem(2, 9, rocketLaunchItem.asGuiItem {
                gui.close(it.whoClicked)
                if (modules.find { f -> f.moduleType == ModuleType.ENGINE } != null && modules.find { f -> f.id == "controlmodule" } != null && modules.find { f -> f.moduleType == ModuleType.NOSECONE } != null && (abs(
                        ((PlanetManager.planets.find { pl -> pl.second?.name == installedLocation.world.name }?.first?.pos
                            ?: 0.0) - (this.reachPlanet?.pos ?: 0.0))
                    ) <= (fuelHeight * engine.fuelDistanceRatio)) && this.reachPlanet != null){
                        it.whoClicked.showTitle(
                            Title.title(
                                Component.text("로켓 발사"),
                                Component.text("5초 후 ${reachPlanet?.name}으로!")
                            )
                        )
                    Instance.server.scheduler.scheduleSyncDelayedTask(Instance.plugin, {
                        for (x in -3..3){
                            for (y in -3..3){
                                for (z in 0..modules.sumOf { s -> s.sizeY }){
                                    val resetLocation = installedLocation.clone()
                                    resetLocation.add(x.toDouble(), z.toDouble(), y.toDouble())
                                    val checkLocation = installedLocation.clone()
                                    checkLocation.add(x.toDouble(), 0.0, y.toDouble())
                                    if (installedLocation.distance(checkLocation) <= 3){
                                        resetLocation.world.getBlockAt(resetLocation).type = Material.AIR
                                    }
                                }
                            }
                        }
                        val world = PlanetManager.planets.find { f -> f.first.codeName == this.reachPlanet?.codeName }?.second!!
                        this.fuelHeight -= ceil(abs(
                            ((PlanetManager.planets.find { pl -> pl.second?.name == installedLocation.world.name }?.first?.pos
                                ?: 0.0) - (this.reachPlanet?.pos ?: 0.0)) / engine.fuelDistanceRatio)).toInt()
                        this.installedLocation.world = world
                        this.installedLocation.y = world.getHighestBlockYAt(
                            it.whoClicked.location.x.toInt(),
                            it.whoClicked.location.z.toInt()
                        ).toDouble()
                        render()
                        it.whoClicked.teleport(Location(world, it.whoClicked.location.x, world.getHighestBlockYAt(
                            it.whoClicked.location.x.toInt(),
                            it.whoClicked.location.z.toInt()
                        ).toDouble(), it.whoClicked.location.z))
                    }, 20*5)
                }else{
                    it.whoClicked.sendMessage(Component.text("조건이 모두 충족되지 않았습니다!").color(TextColor.color(255, 0, 0)))
                }
            })
            gui.open(p)
        }
    }
}