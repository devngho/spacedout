/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout

import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.config.*
import com.github.devngho.spacedout.equipment.EquipmentManager
import com.github.devngho.spacedout.equipment.toItemStack
import com.github.devngho.spacedout.event.Event
import com.github.devngho.spacedout.planet.PlanetManager
import com.github.devngho.spacedout.rocket.*
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandPermission
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.components.ScrollType
import dev.triumphteam.gui.guis.Gui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.generator.ChunkGenerator
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile


class Plugin : JavaPlugin() {
    override fun onEnable() {
        super.onEnable()

        //리소스 뜯기
        val jar = JarFile(this.file.absoluteFile)
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
            val enumEntries: Enumeration<*> = jar.entries()
            while (enumEntries.hasMoreElements()) {
                val file = enumEntries.nextElement() as JarEntry
                val f = File(dataFolder.absolutePath + File.separator + file.name)
                if (file.isDirectory && file.name.contains("resource")) {
                    f.mkdir()
                    continue
                } else if (file.name.contains("resource")) {
                    val ins: InputStream = jar.getInputStream(file)
                    val fos = FileOutputStream(f)
                    while (ins.available() > 0) {
                        fos.write(ins.read())
                    }
                    fos.close()
                    ins.close()
                }
            }
            jar.close()
        }

        //수많은 Load
        I18n.loadAll()
        Config.loadConfigs()
        AddonManager.registerAddon()
        Config.loadPlanetModuleConfigs()
        PlayerData.loadAll()
        PlanetManager.generateWorlds()
        RocketData.loadAll()

        server.scheduler.scheduleSyncRepeatingTask(this, { RocketManager.tick() }, 0, 1)
        server.pluginManager.registerEvents(Event(), this)
        val rocketInstallerRecipe = ShapedRecipe(NamespacedKey(this, "rocketinstaller"), rocketInstaller)
        rocketInstallerRecipe.shape("sis")
        rocketInstallerRecipe.setIngredient('s', Material.STONE)
        rocketInstallerRecipe.setIngredient('i', Material.IRON_BLOCK)
        server.addRecipe(rocketInstallerRecipe)
        EquipmentManager.equipments.forEach {
            server.addRecipe(it.recipe)
        }
    }

    override fun onLoad() {
        super.onLoad()
        Instance.server = server
        Instance.plugin = this
        // 커맨드 등록
        CommandAPICommand("spacedout")
            .withPermission(CommandPermission.OP)
            .withSubcommand(CommandAPICommand("rocket")
                    .withSubcommand(CommandAPICommand("create").withArguments(StringArgument("engine")
                        .includeSuggestions(ArgumentSuggestions.strings(*ModuleManager.modules.filter { it.moduleType == ModuleType.ENGINE && it is Engine }.map { it.id }.toTypedArray())))
                        .executesPlayer(
                            PlayerCommandExecutor { sender, args ->
                                val engine = ModuleManager.modules.find { it.id== args[0] && it.moduleType == ModuleType.ENGINE && it is Engine }
                                if (engine != null) {
                                    val rocketDevice = RocketManager.createRocketWithName(engine.name, sender.location.toBlockLocation())
                                    rocketDevice.render()
                                }
                            }
                        )
                    )
            )
            .withSubcommand(CommandAPICommand("planet")
                .withSubcommand(CommandAPICommand("teleport").withArguments(StringArgument("planet").includeSuggestions(
                    ArgumentSuggestions.strings(*PlanetManager.planets.map { it.first.codeName }.toTypedArray()))).executesPlayer(
                    PlayerCommandExecutor {sender, args ->
                        sender.teleport(Location(PlanetManager.planets.find { it.first.codeName == args[0]}?.second!!, 0.toDouble(), 100.toDouble(), 0.toDouble()))
                    }
                ))
            )
            .withSubcommand(CommandAPICommand("resetdata")
                .executes(
                    CommandExecutor { _, _ ->
                        Instance.plugin.server.onlinePlayers.forEach {
                            PlayerData.initPlayerData(it.uniqueId)
                        }
                        Instance.plugin.server.offlinePlayers.forEach {
                            PlayerData.initPlayerData(it.uniqueId)
                        }
                    }
                )
            )
            .withSubcommand(CommandAPICommand("config")
                .withSubcommand(CommandAPICommand("reload")
                    .withSubcommand(CommandAPICommand("all")
                        .executes(CommandExecutor { sender, _ ->
                            Config.loadConfigs()
                            Config.loadPlanetModuleConfigs()
                            I18n.loadAll()
                            sender.sendMessage(Component.text("Spacedout Plugin Config reloaded!").color(TextColor.color(0, 255, 0)))
                        }))
                    )
                .withSubcommand(CommandAPICommand("save")
                    .executes(CommandExecutor { sender, _ ->
                        Config.saveConfigs()
                        I18n.saveLangData()
                        sender.sendMessage(Component.text("Spacedout Plugin config saved!").color(TextColor.color(0, 255, 0)))
                    })
                )
            )
            .withSubcommand(CommandAPICommand("equip")
                .withSubcommand(CommandAPICommand("inv")
                    .executesPlayer(PlayerCommandExecutor { sender, _ ->
                        EquipmentManager.playerEquipmentGui[sender.uniqueId]?.open(sender)
                    })
                )
                .withSubcommand(CommandAPICommand("give")
                    .withArguments(StringArgument("equip").includeSuggestions (ArgumentSuggestions.strings(*EquipmentManager.equipments.map { it.id }.toTypedArray())))
                    .executesPlayer(PlayerCommandExecutor { sender, args ->
                        val found = EquipmentManager.equipments.find { it.id == args[0] }
                        if (found != null){
                            sender.inventory.addItem(found.toItemStack(sender.getLang()))
                        }
                    })))
            .withSubcommand(CommandAPICommand("addon")
                .withSubcommand(CommandAPICommand("list")
                    .executesPlayer(PlayerCommandExecutor { sender, _ ->
                        val addonGui = Gui.scrolling(ScrollType.HORIZONTAL)
                            .title(Component.text("애드온 목록").decoration(TextDecoration.ITALIC, false))
                            .rows(1)
                            .pageSize(7)
                            .create()
                        addonGui.disableAllInteractions()
                        addonGui.setItem(
                            1,
                            1,
                            ItemBuilder.from(Material.PAPER).name(I18n.getComponent(sender.getLang(), "text.prov").decoration(TextDecoration.ITALIC, false))
                                .asGuiItem { addonGui.previous() })
                        addonGui.setItem(
                            1,
                            9,
                            ItemBuilder.from(Material.PAPER).name(I18n.getComponent(sender.getLang(), "text.next").decoration(TextDecoration.ITALIC, false))
                                .asGuiItem { addonGui.next() })
                        AddonManager.addons.forEach {
                            addonGui.addItem(ItemBuilder.from(it.graphicMaterial).name(Component.text(it.name).color(
                                TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)).lore(it.description).asGuiItem())
                        }
                        addonGui.open(sender)
                    }))
            )
            .register()
        CommandAPICommand("falling")
            .withArguments(BooleanArgument("boolean"))
            .executesPlayer(PlayerCommandExecutor { sender, args ->
                if (args[0] == true){
                    sender.player?.config?.set("use_falling", true)
                    sender.sendMessage(I18n.getComponent(sender.getLang(), "text.enablefalling").clear)
                }else if(args[0] == false){
                    sender.player?.config?.set("use_falling", false)
                    sender.sendMessage(I18n.getComponent(sender.getLang(), "text.disablefalling").clear)
                }
            })
            .register()
        CommandAPICommand("equip")
            .withAliases("eq")
            .executesPlayer(PlayerCommandExecutor { sender, _ ->
                EquipmentManager.playerEquipmentGui[sender.uniqueId]?.open(sender)
            })
            .register()
    }

    override fun onDisable() {
        super.onDisable()
        Config.saveConfigs()
        PlayerData.savePlayerData()
        RocketData.saveRocketData()
        I18n.saveLangData()
    }
    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return PlanetManager.planets.find { it.first.codeName == worldName }?.second?.generator!!
    }
}
