package com.github.devngho.spacedout.rocket

import org.bukkit.Location

object RocketManager {
    var rockets: MutableList<RocketDevice> = mutableListOf()
    fun createRocketWithName(rocketName: String, location: Location): RocketDevice {
        val device = RocketDevice(ModuleManager.modules.find { it.name == rocketName && it.moduleType == ModuleType.ENGINE && it is Engine }!! as Engine, location)
        rockets.add(device)
        return device
    }
    fun createRocketWithInstaller(rocketName: String, location: Location): RocketDevice {
        val device = RocketDevice(ModuleManager.modules.find { it.id == rocketName && it.moduleType == ModuleType.ENGINE && it is Engine }!! as Engine, location)
        rockets.add(device)
        return device
    }
    fun tick(){
        rockets.forEach {
            it.tick()
        }
    }
}