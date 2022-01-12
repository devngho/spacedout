package com.nghodev.spacedout.rocket

import org.bukkit.Location

object RocketManager {
    var rockets: MutableList<RocketDevice> = mutableListOf()
    var rocketEngines: MutableList<Engine> = mutableListOf(CoalEngine())
    fun createRocketWithName(rocketName: String, location: Location): RocketDevice {
        val device = RocketDevice(rocketEngines.find { it.name == rocketName }!!, location)
        rockets.add(device)
        return device
    }
    fun createRocketWithInstaller(rocketName: String, location: Location): RocketDevice {
        val device = RocketDevice(rocketEngines.find { it.codeName == rocketName }!!, location)
        rockets.add(device)
        return device
    }
    fun tick(){
        rockets.forEach {
            it.tick()
        }
    }
}