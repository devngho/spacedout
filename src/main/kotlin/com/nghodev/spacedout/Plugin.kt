package com.nghodev.spacedout

import com.nghodev.spacedout.config.Config
import com.nghodev.spacedout.config.PlayerData
import com.nghodev.spacedout.equipment.EquipmentManager
import com.nghodev.spacedout.equipment.Jetpack
import com.nghodev.spacedout.equipment.toItemStack
import com.nghodev.spacedout.event.Event
import com.nghodev.spacedout.planet.*
import com.nghodev.spacedout.rocket.*
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin


class Plugin : JavaPlugin() {
    override fun onEnable() {
        super.onEnable()
        server.scheduler.scheduleSyncRepeatingTask(this, {RocketManager.tick()}, 0, 1)
        server.pluginManager.registerEvents(Event(), this)
        // PlanetManager.generateWorlds()
    }

    override fun onLoad() {
        super.onLoad()
        Instance.server = server
        Instance.plugin = this
        registerPlanets()
        registerModules()
        registerBuildable()
        registerEquipments()
        Config.loadConfigs()
        PlayerData.loadAll()
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
            .register()
    }

    override fun onDisable() {
        super.onDisable()
        Config.saveConfigs()
    }
    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return PlanetManager.planets.find { it.first.codeName == worldName }?.second?.generator!!
    }
    private fun registerPlanets(){
        PlanetManager.planets += Pair(Mercury(), null)
        PlanetManager.planets += Pair(Venus(), null)
        PlanetManager.planets += Pair(Mars(), null)
        PlanetManager.planets += Pair(Jupiter(), null)
        PlanetManager.planets += Pair(Earth(), server.getWorld("world"))
    }
    private fun registerModules(){
        ModuleManager.modules += CoalEngine()
        ModuleManager.modules += LavaEngine()
        ModuleManager.modules += ControlModule()
        ModuleManager.modules += StandardNosecone()
    }
    private fun registerBuildable(){
    }
    private fun registerEquipments(){
        EquipmentManager.equipments += Jetpack()
    }
}
