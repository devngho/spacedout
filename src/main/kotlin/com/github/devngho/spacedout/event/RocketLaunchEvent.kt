package com.github.devngho.spacedout.event

import com.github.devngho.spacedout.planet.Planet
import com.github.devngho.spacedout.rocket.RocketDevice
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class RocketLaunchEvent internal constructor(val rocket: RocketDevice, val fromPlanet: Planet, val toPlanet: Planet) : org.bukkit.event.Event() {

    companion object{
        private val handlers: HandlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList {
        return Companion.handlers
    }
}