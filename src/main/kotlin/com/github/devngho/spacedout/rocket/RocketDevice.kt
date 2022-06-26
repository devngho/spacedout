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
import com.github.devngho.spacedout.config.StructureLoader
import com.github.devngho.spacedout.config.I18n.getLang
import com.github.devngho.spacedout.equipment.EquipmentManager
import com.github.devngho.spacedout.event.RocketFuelChargeEvent
import com.github.devngho.spacedout.event.RocketLaunchEvent
import com.github.devngho.spacedout.event.RocketModuleAddEvent
import com.github.devngho.spacedout.event.RocketModuleRemoveEvent
import com.github.devngho.spacedout.planet.Planet
import com.github.devngho.spacedout.planet.PlanetManager
import com.github.devngho.spacedout.planet.PlanetManager.asPlanet
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.ScrollType
import dev.triumphteam.gui.guis.Gui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.round


class RocketDevice(val engine: Engine, val installedLocation: Location, val uniqueId: UUID) {

    /**
     * 로켓 모듈 전체의 높이를 계산합니다.
     */
    val height: Int
        get() {
            return modules.sumOf { it.height }
        }

    /**
     * 로켓의 연료 양입니다.
     */
    var fuelHeight: Int = 0

    /**
     * 로켓의 모듈 목록입니다. 기본적으로 엔진을 가지고 있습니다.
     */
    var modules: MutableList<Module> = mutableListOf(engine)

    /**
     * 목적 행성입니다.
     */
    var reachPlanet: Planet? = null

    /**
     * 현재 발사되어 있는지 여부입니다.
     */
    var isLaunching = false

    /**
     * 로켓이 유효한지 여부입니다.
     */
    var isValid = true

    /**
     * 엔진을 가지고 있는지 여부입니다.
     */
    val hasEngine: Boolean
        get() = modules.any { it.moduleType == ModuleType.ENGINE }

    /**
     * 노즈콘을 가지고 있는지 여부입니다.
     */
    val hasNosecone: Boolean
        get() = modules.any { it.moduleType == ModuleType.NOSECONE }

    /**
     * 조종 모듈을 가지고 있는지 여부입니다.
     */
    val hasControlModule: Boolean
        get() = modules.any { it is ControlModule }

    /**
     * 편도 연료가 충분한지 여부입니다.
     */
    val hasFuel: Boolean
        get() {
            return if (reachPlanet != null) {
                abs((installedLocation.world.asPlanet()?.pos ?: 0.0) - (this.reachPlanet?.pos ?: 0.0)) <= (fuelHeight * engine.fuelDistanceRatio)
            }else {
                false
            }
        }

    /**
     * 목적지 행성을 가지고 있는지 여부입니다.
     */
    val hasReachPlanet: Boolean
        get() = reachPlanet != null

    /**
     * 모든 조종 모듈에 탑승해 있는지 여부입니다.
     */
    val isControlModuleRode: Boolean
        get() = modules.filterIsInstance<ControlModule>().all { it.ridedPlayer != null }

    /**
     * 탑승한 모든 플레이어가 온라인인지 여부입니다.
     */
    val isControlModuleRodeOnline: Boolean
        get() {
            return if (isControlModuleRode){
                modules.filterIsInstance<ControlModule>().all { Bukkit.getOfflinePlayer(it.ridedPlayer!!).isOnline }
            }else{
                false
            }
        }

    /**
     * 탑승한 모든 플레이어가 방호구를 충분히 착용하고 있는지 여부입니다.
     */
    val isControlModuleRodeEquipment: Boolean
        get() {
            return if (isControlModuleRode){
                modules.filterIsInstance<ControlModule>()
                    .map { Instance.server.getOfflinePlayer(it.ridedPlayer!!) }
                    .map {
                        EquipmentManager.getPlayerEquipments(it)
                    }
                    .all { reachPlanet!!.needEquipments.all { c -> it.map { m -> m.value.id }.contains(c.id) } }
            }else{
                false
            }
        }

    init {
        //로켓 위치를 블럭 위치로 변환
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
    fun render() {
        val location = installedLocation.clone()
        modules.forEach {
            if (it.useStructure) {
                StructureLoader.placeAtWorld(location, it.structure!!)
            } else {
                it.render(this, location)
            }
            location.add(0.0, it.sizeY.toDouble(), 0.0)
        }
    }

    /**
     * 모듈을 처리합니다.
     */
    fun tick() {
        //발사중엔 인터렉션 X
        if (!isLaunching) {
            installedLocation.getNearbyPlayers(Config.configConfiguration.getDouble("rocket.interactiondistance", 4.0)).filter { it.isSneaking }.forEach { p ->
                //로켓 UI 렌더
                renderGui(p)
            }
        }
    }

    fun renderGui(p: Player) {

        // GUI 생성
        val gui = Gui.gui()
            .title(Component.text(I18n.getString(p.getLang(), "rocket.name")).decoration(TextDecoration.ITALIC, false))
            .rows(3)
            .create()
        gui.setDefaultClickAction { event ->
            event.isCancelled = true
        }

        // 모듈 목록을 업데이트합니다.
        fun updateModules() {
            // 모듈 목록 아이템을 지움
            for (i in 2..8) {
                gui.removeItem(1, i)
            }

            //맨 왼쪽 로켓 정보를 보여주는 초록 색유리판 생성
            gui.setItem(1, 1, ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                .name(Component.text("${I18n.getString(p.getLang(), "module.name")} (${I18n.getString(p.getLang(), "module.allheight")} $height )")
                    .decoration(TextDecoration.ITALIC, false)).asGuiItem())

            //모듈별 렌더
            modules.forEachIndexed { idx, item ->

                // 설명(Lore) 렌더
                val moduleItemLore = mutableListOf(Component.text("${I18n.getString(p.getLang(), "module.height")} : ${item.height}").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false))
                if (item.moduleType != ModuleType.ENGINE) moduleItemLore += Component.text(I18n.getString(p.getLang(), "module.rightclickremove")).color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
                item.renderLore(p.getLang()).forEach {
                    moduleItemLore.add(it as TextComponent)
                }
                moduleItemLore += Component.text("from. ${item.addedAddon.name}").color(TextColor.color(127, 127, 127))

                //아이템 배치
                gui.setItem(1, idx + 2, ItemBuilder.from(item.graphicMaterial).name(Component.text(I18n.getString(p.getLang(), item.name)).decoration(TextDecoration.ITALIC, false)).lore(
                    moduleItemLore.toList()
                ).asGuiItem {
                    // 이벤트

                    if (it.isLeftClick) {
                        // 클릭 시 호출 후 재 렌더
                        item.onClick(it)
                        updateModules()
                    }

                    if (it.isRightClick && item.moduleType != ModuleType.ENGINE) {
                        //모듈 삭제
                        item.buildRequires.forEach { b ->
                            it.whoClicked.world.dropItem(it.whoClicked.location, ItemStack(b.first, b.second))
                        }
                        Instance.server.pluginManager.callEvent(RocketModuleRemoveEvent(it.whoClicked as Player, this, item))
                        modules.removeAt(idx)
                        clearToZ(modules.sumOf { s -> s.sizeY } + item.sizeY)
                        render()
                        updateModules()
                    }
                })
            }
            gui.update()
        }

        //초기 모듈 목록 렌더
        updateModules()

        //모듈 추가 버튼 렌더
        val moduleAdder = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).name(Component.text(I18n.getString(p.getLang(), "module.addmodule")).decoration(TextDecoration.ITALIC, false)).asGuiItem()
        moduleAdder.setAction { ev ->
            //모듈 추가 GUI
            val moduleAdderGui = Gui.scrolling(ScrollType.HORIZONTAL)
                .title(Component.text(I18n.getString(p.getLang(), "module.addmodulepick")).decoration(TextDecoration.ITALIC, false))
                .rows(1)
                .pageSize(7)
                .create()

            //앞뒤 버튼
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

            //등록된 모듈 목록
            ModuleManager.modules.forEach {
                //아이템 생성

                val moduleItem = ItemBuilder.from(it.graphicMaterial).name(Component.text(I18n.getString(p.getLang(), it.name)).decoration(TextDecoration.ITALIC, false).color(
                    TextColor.color(255, 255, 255)))

                //Lore 생성
                val moduleLore = mutableListOf<Component>()
                moduleLore += if (ModuleManager.isPlaceable(engine, modules, it)) {
                    Component.text(I18n.getString(p.getLang(), "module.caninstall")).color(TextColor.color(29, 219, 22)).decoration(TextDecoration.ITALIC, false)
                } else {
                    Component.text(I18n.getString(p.getLang(), "module.cantinstall")).color(TextColor.color(201, 0, 0)).decoration(TextDecoration.ITALIC, false)
                }
                moduleLore += if (it.buildRequires.all { a -> ev.whoClicked.inventory.contains(ItemStack(a.first, a.second)) }) {
                    Component.text(I18n.getString(p.getLang(), "module.haveresource")).color(TextColor.color(29, 219, 22)).decoration(TextDecoration.ITALIC, false)
                } else {
                    Component.text(I18n.getString(p.getLang(), "module.nohaveresource")).color(TextColor.color(201, 0, 0)).decoration(TextDecoration.ITALIC, false)
                }
                var requireComp = Component.text("${I18n.getString(p.getLang(), "module.needresource")} : ")
                it.buildRequires.forEachIndexed { i, item ->
                    requireComp = requireComp.append(Component.translatable(item.first.translationKey())).append(Component.text("x${item.second}${
                        if (it.buildRequires.count() - 1 != i) {
                            ","
                        } else {
                            ""
                        }
                    }"))
                }

                moduleLore += requireComp.color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
                moduleLore += Component.text("${I18n.getString(p.getLang(), "module.height")} : ${it.height}").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
                moduleLore += Component.text("from. ${it.addedAddon.name}").color(TextColor.color(127, 127, 127))
                moduleItem.lore(moduleLore.toList())

                //아이템 배치
                moduleAdderGui.addItem(moduleItem.asGuiItem { e ->
                    if (ModuleManager.isPlaceable(engine, modules, it) && it.buildRequires.all { a -> ev.whoClicked.inventory.contains(ItemStack(a.first, a.second)) }) {
                        //설치
                        this.modules.add(it.newInstance())
                        this.render()
                        it.buildRequires.forEach { i ->
                            ev.whoClicked.inventory.remove(ItemStack(i.first, i.second))
                        }
                        Instance.server.pluginManager.callEvent(RocketModuleAddEvent(e.whoClicked as Player, this, it.newInstance()))
                        moduleAdderGui.close(e.whoClicked)
                    } else {
                        //설치 불가 -> 취소
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

        //목적지 행성 버튼
        val planetSetting =
            ItemBuilder
                .from(reachPlanet?.graphicMaterial ?: Material.GREEN_STAINED_GLASS_PANE)
                .name(Component.text(I18n.getString(p.getLang(), "planet.name"))
                    .decoration(TextDecoration.ITALIC, false))
                .asGuiItem()

        //이벤트
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
                val planetItem = ItemBuilder
                    .from(it.first.graphicMaterial)
                    .name(Component.text(I18n.getString(p.getLang(), it.first.name))
                        .decoration(TextDecoration.ITALIC, false)
                        .color(TextColor.color(255, 255, 255)))

                //행성 Lore 렌더
                val planetLore = mutableListOf<Component>()
                if (it.second?.name == this.installedLocation.world.name) {
                    //현재 행성
                    planetLore += Component.text(I18n.getString(p.getLang(), "planet.hereplanet")).decoration(TextDecoration.ITALIC, false)
                }
                val distance = abs(
                    (PlanetManager.planets.find { p -> p.second?.name == installedLocation.world.name }?.first?.pos
                        ?: 0.0) - (it.first.pos)
                )
                planetLore += Component.text(I18n.getString(p.getLang(), it.first.description)).decoration(TextDecoration.ITALIC, false).color(
                    TextColor.color(255, 255, 255))

                //거리
                planetLore += Component.text("${I18n.getString(p.getLang(), "planet.distance")} : ${distance}AU").decoration(TextDecoration.ITALIC, false).color(TextColor.color(255, 255, 255))
                //필요 방호구
                planetLore += Component.text("${I18n.getString(p.getLang(), "equipment.needequipment")} : ${it.first.needEquipments.joinToString(postfix = ", ") { j -> I18n.getString(p.getLang(), j.name) }}").decoration(TextDecoration.ITALIC, false).color(
                    TextColor.color(255, 255, 255))
                //도착 가능/불가능
                planetLore += if (distance <= fuelHeight * engine.fuelDistanceRatio) {
                    Component.text(I18n.getString(p.getLang(), "rocket.canreach")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(29, 219, 22))
                } else {
                    Component.text(I18n.getString(p.getLang(), "rocket.cantreach")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(201, 0, 0))
                }
                //왕복 가능/불가능
                planetLore += if (distance * 2 <= fuelHeight * engine.fuelDistanceRatio) {
                    Component.text(I18n.getString(p.getLang(), "rocket.canround")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(29, 219, 22))
                } else {
                    Component.text(I18n.getString(p.getLang(), "rocket.cantround")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(201, 0, 0))
                }
                //방호구 전체 착용
                val equips = EquipmentManager.getPlayerEquipments(p)
                planetLore += if (it.first.needEquipments.all { c -> equips.map { m -> m.value.id }.contains(c.id) }) {
                    Component.text(I18n.getString(p.getLang(), "rocket.canequipment")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(29, 219, 22))
                } else {
                    Component.text(I18n.getString(p.getLang(), "rocket.cantequipment")).decoration(TextDecoration.ITALIC, false).color(TextColor.color(201, 0, 0))
                }
                //추가한 애드온
                planetLore += Component.text("from. ${it.first.addedAddon.name}").color(TextColor.color(127, 127, 127))
                planetItem.lore(planetLore.toList())

                val planetGuiItem = planetItem.asGuiItem()

                planetGuiItem.setAction { _ ->
                    //목적지 설정
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

        //연료 잔량 업데이트
        fun updateFuel() {
            //아이템 설정
            gui.setItem(
                3, 1, ItemBuilder
                    .from(engine.supportFuel)
                    //연료 잔량(name)
                    .name(
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
                    )
                    //연료 최대량(lore)
                    .lore(
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

            // 아이템 설정2
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

            //연료 투입 아이템 생성
            val fuelInputItem = ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).name(
                Component.text(I18n.getString(p.getLang(), "rocket.addfuel")).decoration(TextDecoration.ITALIC, false)
            )

            //투입 아이템 lore
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

            // 호환 연료 lore
            fuelInputItemLore += Component.text("${I18n.getString(p.getLang(), "rocket.compatiblefuel")} : ")
                .append(Component.translatable(engine.supportFuel.translationKey()))
                .color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
            fuelInputItem.lore(fuelInputItemLore.toList())

            //아이템 설정
            gui.setItem(3, 2, fuelInputItem.asGuiItem {

                //use 파라미터만큼 연료 충전
                fun chargeFuel(use: Int) {
                    val inv = it.whoClicked.inventory
                    //연료 최대량 초과 체크
                    if (engine.maxFuelHeight >= fuelHeight + use) {
                        //인벤토리 잔량 체크
                        if (inv.contains(ItemStack(engine.supportFuel), use)) {
                            //사용
                            inv.removeItem(ItemStack(engine.supportFuel, use))
                            fuelHeight += use
                            Instance.server.pluginManager.callEvent(
                                RocketFuelChargeEvent(
                                    it.whoClicked as Player,
                                    this,
                                    use
                                )
                            )
                        } else {
                            //필요 메시지
                            it.whoClicked.sendMessage(
                                Component.text(I18n.getString(p.getLang(), "rocket.itemneed"))
                                    .color(TextColor.color(201, 0, 0))
                            )
                        }
                    }
                }
                //이벤트
                when (it.click) {
                    ClickType.LEFT -> {
                        chargeFuel(1)
                    }
                    ClickType.RIGHT -> {
                        chargeFuel(10)
                    }
                    ClickType.SHIFT_LEFT -> {
                        chargeFuel(64)
                    }
                    else -> {}
                }

                //GUI 업데이트
                updateFuel()
                gui.update()
                it.isCancelled = true
            })
        }
        //초기 업데이트
        updateFuel()

        //로켓 발사 아이템 생성
        val rocketLaunchItem = ItemBuilder
            .from(Material.FIREWORK_ROCKET)
            .name(
                Component
                    .text(I18n.getString(p.getLang(), "rocket.launch"))
                    .decoration(TextDecoration.ITALIC, false))

        //발사 아이템 lore 생성
        val rocketLaunchItemLore = mutableListOf<Component>()

        val conditions = listOf(
            Pair("rocket.engine", hasEngine),
            Pair("rocket.fuel", hasFuel),
            Pair("rocket.controlmodule", hasControlModule),
            Pair("rocket.nosecone", hasNosecone),
            Pair("rocket.reachplanetpicked", hasReachPlanet),
            Pair("rocket.selectrider", isControlModuleRode),
            Pair("rocket.selectrideronline", isControlModuleRodeOnline),
            Pair("rocket.canequipment", isControlModuleRodeEquipment)
        )

        // 로켓 조건 체크

        conditions.forEach {
            rocketLaunchItemLore += Component.text(I18n.getString(p.getLang(),
                it.first
            )).color(
                if (it.second) TextColor.color(0, 255, 0) else TextColor.color(255, 0, 0)
            ).decoration(TextDecoration.ITALIC, false)
        }

        rocketLaunchItem.lore(rocketLaunchItemLore.toList())

        //발사 아이템 배치
        gui.setItem(3, 4, rocketLaunchItem.asGuiItem {
            gui.close(it.whoClicked)
            //발사 가능 조건 체크
            if (conditions.all { c -> c.second } && !isLaunching) {
                //거리 미리 계산
                val distance = abs(
                    ((PlanetManager.planets.find { pl -> pl.second?.name == installedLocation.world.name }?.first?.pos
                        ?: 0.0) - (this.reachPlanet?.pos ?: 0.0))
                )

                //플레이어 발사 알림
                modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.forEach { m ->
                    val player = Instance.server.getOfflinePlayer((m as ControlModule).ridedPlayer!!).player!!
                    player.showTitle(
                        Title.title(
                            Component.text(I18n.getString(p.getLang(), "rocket.launch")),
                            Component.text(
                                I18n.getString(p.getLang(), "rocket.reachaftersecond")
                                    .replace("{name}", I18n.getString(p.getLang(), reachPlanet!!.name)).replace(
                                        "{second}",
                                        (distance / engine.speedDistanceRatio / 20).toString()
                                    )
                            )
                        )
                    )
                }

                //발사 시작!
                isLaunching = true

                //이벤트 호출
                Instance.server.pluginManager.callEvent(
                    RocketLaunchEvent(
                        this,
                        PlanetManager.planets.find { pl -> pl.second?.name == installedLocation.world.name }?.first,
                        reachPlanet!!
                    )
                )

                //주변 블록 삭제
                clearToZ(height)

                //Falling block 발사 사용 여부 따라 사용
                if (Config.configConfiguration.getBoolean("rocket.usefallinglaunch", true)) {
                    LaunchingRocket(modules, installedLocation.clone(), false)
                }

                // 반 왔을 때 tp
                Instance.server.scheduler.scheduleSyncDelayedTask(Instance.plugin, {
                    val world =
                        PlanetManager.planets.find { f -> f.first.codeName == this.reachPlanet?.codeName }?.second!!
                    this.fuelHeight -= ceil(
                        abs(
                            ((PlanetManager.planets.find { pl -> pl.second?.name == installedLocation.world.name }?.first?.pos
                                ?: 0.0) - (this.reachPlanet?.pos ?: 0.0)) / engine.fuelDistanceRatio
                        )
                    ).toInt()

                    //로켓 이동
                    this.installedLocation.world = world
                    this.installedLocation.y = world.getHighestBlockYAt(
                        this.installedLocation.x.toInt(),
                        this.installedLocation.z.toInt()
                    ).toDouble() + 1

                    modules.filter { f -> f.id == "controlmodule" && f is ControlModule }.forEach { m ->
                        val player =
                            Instance.server.getOfflinePlayer((m as ControlModule).ridedPlayer!!).player!!
                        player.teleport(
                            Location(
                                world, player.location.x, world.getHighestBlockYAt(
                                    player.location.x.toInt(),
                                    player.location.z.toInt()
                                ).toDouble() + 1, player.location.z
                            )
                        )
                    }

                    //도착했을 때 랜딩
                    Instance.server.scheduler.scheduleSyncDelayedTask(Instance.plugin, {
                        render()
                        isLaunching = false
                    }, (distance / engine.speedDistanceRatio).toLong())

                    //착륙 Falling launch
                    if (Config.configConfiguration.getBoolean("rocket.usefallinglaunch", true)) {
                        LaunchingRocket(modules, installedLocation.clone(), true)
                    }
                }, (distance / engine.speedDistanceRatio).toLong())
            }
        })

        //로켓 삭제 아이템 배치
        gui.setItem(3, 5, ItemBuilder.from(Material.RED_CONCRETE).name(Component.text(I18n.getString(p.getLang(), "rocket.removerocket")).color(
            TextColor.color(255, 0, 0)).decoration(TextDecoration.ITALIC, false)).asGuiItem {
            modules.forEach { m ->
                m.buildRequires.forEach { r ->
                    it.whoClicked.location.world.dropItem(it.whoClicked.location, ItemStack(r.first, r.second))
                }
            }
            this.deleteRocket()
            gui.close(it.whoClicked)
        })

        //플레이어에게 보여주기
        gui.open(p)
    }

    private fun deleteRocket(){
        //블럭 청소
        clearToZ(modules.sumOf { s -> s.sizeY })

        //삭제
        this.isValid = false
        RocketManager.rockets.removeIf {r -> !r.isValid}
    }

    private fun clearToZ(toZ: Int){
        for (x in -3..3) {
            for (y in -3..3) {
                for (z in 0..toZ) {
                    val resetLocation = installedLocation.clone()
                    resetLocation.add(x.toDouble(), z.toDouble(), y.toDouble())
                    val checkLocation = installedLocation.clone()
                    checkLocation.add(x.toDouble(), 0.0, y.toDouble())
                    if (installedLocation.distance(checkLocation) <= 3) {
                        resetLocation.world.getBlockAt(resetLocation).type = Material.AIR
                    }
                }
            }
        }
    }
}