package com.github.devngho.spacedout.rocket

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.ScrollType
import dev.triumphteam.gui.guis.Gui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

val rocketInstaller: ItemStack = ItemStack(Material.DIAMOND).apply { itemMeta = itemMeta.apply {
    displayName(Component.text("로켓 설치 장치").decoration(TextDecoration.ITALIC, false))
    lore(listOf(Component.text("블럭에 우클릭해 로켓을 설치하세요.").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)))
} }

internal fun onUseRocketInstaller(event: PlayerInteractEvent){
    if (event.action == Action.RIGHT_CLICK_BLOCK && event.hasBlock()) {
        val engineSelectorGui = Gui.scrolling(ScrollType.HORIZONTAL)
            .title(Component.text("설치할 엔진").decoration(TextDecoration.ITALIC, false))
            .rows(1)
            .pageSize(7)
            .create()
        engineSelectorGui.setItem(
            1,
            1,
            ItemBuilder.from(Material.PAPER).name(Component.text("이전").decoration(TextDecoration.ITALIC, false))
                .asGuiItem { engineSelectorGui.previous() })
        engineSelectorGui.setItem(
            1,
            9,
            ItemBuilder.from(Material.PAPER).name(Component.text("다음").decoration(TextDecoration.ITALIC, false))
                .asGuiItem { engineSelectorGui.next() })
        ModuleManager.modules.filter { it.moduleType == ModuleType.ENGINE && it is Engine }.forEach {
            val moduleItem = ItemBuilder.from(it.graphicMaterial).name(
                Component.text(it.name).decoration(TextDecoration.ITALIC, false).color(
                    TextColor.color(255, 255, 255)
                )
            )
            val moduleLore = mutableListOf<Component>()
            moduleLore += if (it.buildRequires.all { a ->
                    event.player.inventory.contains(
                        ItemStack(
                            a.first,
                            a.second
                        )
                    )
                }) {
                Component.text("자원 충분").color(TextColor.color(29, 219, 22)).decoration(TextDecoration.ITALIC, false)
            } else {
                Component.text("자원 부족").color(TextColor.color(201, 0, 0)).decoration(TextDecoration.ITALIC, false)
            }
            var requireComp = Component.text("필요 자원 : ")
            it.buildRequires.forEachIndexed { i, item ->
                requireComp = requireComp.append(Component.translatable(item.first.translationKey())).append(Component.text("x${item.second}${if(it.buildRequires.count() - 1 != i){", "}else{""}}"))
            }
            moduleLore += requireComp.color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
            moduleLore += Component.text("모듈 중량 : ${it.height}").color(TextColor.color(255, 255, 255))
                .decoration(TextDecoration.ITALIC, false)
            moduleLore += Component.text("from. ${it.addedAddon.name}").color(TextColor.color(127, 127, 127))
            moduleItem.lore(moduleLore.toList())
            engineSelectorGui.addItem(moduleItem.asGuiItem { e ->
                if (it.buildRequires.all { a -> event.player.inventory.contains(ItemStack(a.first, a.second)) }) {
                    it.buildRequires.forEach { i ->
                        event.player.inventory.remove(ItemStack(i.first, i.second))
                    }
                    val loc = event.clickedBlock!!.location.clone()
                    loc.y += 1
                    RocketManager.createRocketWithInstaller(it as Engine, loc).render()
                    engineSelectorGui.close(e.whoClicked)
                    event.item!!.amount -= 1
                } else {
                    e.whoClicked.sendMessage(
                        Component.text("설치할 자원이 부족하거나 설치할 수 없습니다.").color(TextColor.color(255, 0, 0))
                    )
                    engineSelectorGui.close(e.whoClicked)
                }
            })
        }
        engineSelectorGui.setDefaultClickAction { c ->
            c.isCancelled = true
        }
        engineSelectorGui.open(event.player)
    }
}