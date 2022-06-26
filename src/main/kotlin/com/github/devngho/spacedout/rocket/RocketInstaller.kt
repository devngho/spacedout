/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.rocket

import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.config.I18n
import com.github.devngho.spacedout.config.I18n.getLang
import com.github.devngho.spacedout.event.RocketCreateEvent
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.ScrollType
import dev.triumphteam.gui.guis.Gui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

val rocketInstaller: ItemStack = ItemStack(Material.IRON_NUGGET).apply { itemMeta = itemMeta.apply {
    displayName(Component.text("로켓 설치 장치").decoration(TextDecoration.ITALIC, false))
    lore(listOf(Component.text("블럭에 우클릭해 로켓을 설치하세요.").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)))
} }

internal fun onUseRocketInstaller(event: PlayerInteractEvent){
    if (event.action == Action.RIGHT_CLICK_BLOCK && event.hasBlock()) {
        val engineSelectorGui = Gui.scrolling(ScrollType.HORIZONTAL)
            .title(Component.text(I18n.getString(event.player.getLang(), "module.addmodulepick")).decoration(TextDecoration.ITALIC, false))
            .rows(1)
            .pageSize(7)
            .create()
        engineSelectorGui.setItem(
            1,
            1,
            ItemBuilder.from(Material.PAPER).name(Component.text(I18n.getString(event.player.getLang(), "text.prov")).decoration(TextDecoration.ITALIC, false))
                .asGuiItem { engineSelectorGui.previous() })
        engineSelectorGui.setItem(
            1,
            9,
            ItemBuilder.from(Material.PAPER).name(Component.text(I18n.getString(event.player.getLang(), "text.next")).decoration(TextDecoration.ITALIC, false))
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
                Component.text(I18n.getString(event.player.getLang(), "module.haveresource")).color(TextColor.color(29, 219, 22)).decoration(TextDecoration.ITALIC, false)
            } else {
                Component.text(I18n.getString(event.player.getLang(), "module.nohaveresource")).color(TextColor.color(201, 0, 0)).decoration(TextDecoration.ITALIC, false)
            }
            var requireComp = Component.text("${I18n.getString(event.player.getLang(), "module.needresource")} : ")
            it.buildRequires.forEachIndexed { i, item ->
                requireComp = requireComp.append(Component.translatable(item.first.translationKey())).append(Component.text("x${item.second}${if(it.buildRequires.count() - 1 != i){", "}else{""}}"))
            }
            moduleLore += requireComp.color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
            moduleLore += Component.text("${I18n.getString(event.player.getLang(), "module.height")} : ${it.height}").color(TextColor.color(255, 255, 255))
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
                    val rocket = RocketManager.createRocketWithInstaller(it as Engine, loc)
                    rocket.render()
                    Instance.server.pluginManager.callEvent(RocketCreateEvent(e.whoClicked as Player, rocket))
                    engineSelectorGui.close(e.whoClicked)
                    event.item!!.amount -= 1
                } else {
                    e.whoClicked.sendMessage(
                        Component.text(I18n.getString(event.player.getLang(), "module.installfailed")).color(TextColor.color(255, 0, 0))
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