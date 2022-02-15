package com.github.devngho.spacedout

import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.config.Config
import com.github.devngho.spacedout.config.PlayerData
import com.github.devngho.spacedout.config.RocketData
import com.github.devngho.spacedout.equipment.EquipmentManager
import com.github.devngho.spacedout.equipment.toItemStack
import com.github.devngho.spacedout.event.Event
import com.github.devngho.spacedout.planet.PlanetManager
import com.github.devngho.spacedout.rocket.*
import dev.jorel.commandapi.CommandAPICommand
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
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin


class Plugin : JavaPlugin() {
    override fun onEnable() {
        super.onEnable()
        AddonManager.registerAddon()
        Config.loadConfigs()
        PlayerData.loadAll()
        RocketData.loadAll()
        PlanetManager.generateWorlds()
        server.scheduler.scheduleSyncRepeatingTask(this, {RocketManager.tick()}, 0, 1)
        server.pluginManager.registerEvents(Event(), this)
        val rocketInstallerRecipe = ShapedRecipe(NamespacedKey(this, "rocketinstaller"), rocketInstaller)
        rocketInstallerRecipe.shape("sis")
        rocketInstallerRecipe.setIngredient('s', Material.STONE)
        rocketInstallerRecipe.setIngredient('i', Material.IRON_BLOCK)
        server.addRecipe(rocketInstallerRecipe)
    }

    override fun onLoad() {
        super.onLoad()
        Instance.server = server
        Instance.plugin = this
        // 커맨드 등록
        CommandAPICommand("spacedout")
            .withSubcommand(CommandAPICommand("rocket")
                    .withSubcommand(CommandAPICommand("create").withArguments(StringArgument("engine").includeSuggestions { ModuleManager.modules.filter { it.moduleType == ModuleType.ENGINE && it is Engine }.map { it.id }.toTypedArray() }).executesPlayer(
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
                .withSubcommand(CommandAPICommand("teleport").withArguments(StringArgument("planet").includeSuggestions { PlanetManager.planets.map { it.first.codeName }.toTypedArray() }).executesPlayer(
                    PlayerCommandExecutor {sender, args ->
                        sender.teleport(Location(PlanetManager.planets.find { it.first.codeName == args[0]}?.second!!, 0.toDouble(), 100.toDouble(), 0.toDouble()))
                    }
                ))
            )
            .withSubcommand(CommandAPICommand("config")
                .withSubcommand(CommandAPICommand("reload")
                    .withSubcommand(CommandAPICommand("all")
                        .executes(CommandExecutor { sender, _ ->
                            Config.loadConfigs()
                            sender.sendMessage(Component.text("Spacedout Plugin Config reloaded!").color(TextColor.color(0, 255, 0)))
                        }))
                    .withSubcommand(CommandAPICommand("save")
                        .executes(CommandExecutor { sender, _ ->
                            Config.saveConfigs()
                            sender.sendMessage(Component.text("Spacedout Plugin config saved!").color(TextColor.color(0, 255, 0)))
                        })
                    )
                )
            )
            .withSubcommand(CommandAPICommand("equip")
                .withSubcommand(CommandAPICommand("inv")
                    .executesPlayer(PlayerCommandExecutor { sender, _ ->
                        EquipmentManager.playerEquipmentGui[sender.uniqueId]?.open(sender)
                    })
                )
                .withSubcommand(CommandAPICommand("give")
                    .withArguments(StringArgument("equip").includeSuggestions { EquipmentManager.equipments.map { it.id }.toTypedArray() })
                    .executesPlayer(PlayerCommandExecutor { sender, args ->
                        val found = EquipmentManager.equipments.find { it.id == args[0] }
                        if (found != null){
                            sender.inventory.addItem(found.toItemStack())
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
                            ItemBuilder.from(Material.PAPER).name(Component.text("이전").decoration(TextDecoration.ITALIC, false))
                                .asGuiItem { addonGui.previous() })
                        addonGui.setItem(
                            1,
                            9,
                            ItemBuilder.from(Material.PAPER).name(Component.text("다음").decoration(TextDecoration.ITALIC, false))
                                .asGuiItem { addonGui.next() })
                        AddonManager.addons.forEach {
                            addonGui.addItem(ItemBuilder.from(it.graphicMaterial).name(Component.text(it.name).color(
                                TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)).lore(it.description).asGuiItem())
                        }
                        addonGui.open(sender)
                    }))
            )
            .register()
    }

    override fun onDisable() {
        super.onDisable()
        Config.saveConfigs()
        PlayerData.savePlayerData()
        RocketData.saveRocketData()
    }
    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return PlanetManager.planets.find { it.first.codeName == worldName }?.second?.generator!!
    }
}
