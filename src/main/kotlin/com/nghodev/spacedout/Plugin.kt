package com.nghodev.spacedout

import com.nghodev.spacedout.event.Event
import com.nghodev.spacedout.buildable.BuildableManager
import com.nghodev.spacedout.config.Config
import com.nghodev.spacedout.planet.*
import com.nghodev.spacedout.rocket.RocketManager
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
        Instance.server = server
        Instance.plugin = this
        server.scheduler.scheduleSyncRepeatingTask(this, {RocketManager.tick()}, 0, 1)
        server.pluginManager.registerEvents(Event(), this)
        registerPlanets()
        registerBuildable()
        Config.loadConfigs()
        PlanetManager.generateWorlds()
    }

    override fun onLoad() {
        super.onLoad()
        // 커맨드 등록
        CommandAPICommand("spacedout")
            .withSubcommand(CommandAPICommand("rocket")
                    .withSubcommand(CommandAPICommand("create").withArguments(StringArgument("engine").includeSuggestions { RocketManager.rocketEngines.map { it.codeName }.toTypedArray() }).executesPlayer(
                            PlayerCommandExecutor { sender, args ->
                                val engine = RocketManager.rocketEngines.find { it.codeName == args[0] }
                                if (engine != null) {
                                    val rocketDevice = RocketManager.createRocketWithName(engine.name, sender.location)
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
                    .withSubcommand(CommandAPICommand("planets")
                        .executes(CommandExecutor { sender, _ ->
                            PlanetManager.planets.forEach {
                                it.first.loadPlanetConfig(Config.configConfiguration.getConfigurationSection("planet.${it.first.codeName}")!!)
                            }
                            sender.sendMessage(Component.text("Planet config reloaded!").color(TextColor.color(0, 255, 0)))
                        }))
                    .withSubcommand(CommandAPICommand("save")
                        .executes(CommandExecutor { sender, _ ->
                            Config.saveConfigs()
                            sender.sendMessage(Component.text("Planet config saved!").color(TextColor.color(0, 255, 0)))
                        })
                    )
                )
            )
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
        PlanetManager.planets += Pair(Earth(), server.getWorld("world"))
    }
    private fun registerBuildable(){
    }
}