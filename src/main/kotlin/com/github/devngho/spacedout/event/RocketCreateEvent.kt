package com.github.devngho.spacedout.event

import com.github.devngho.spacedout.rocket.RocketDevice
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class RocketCreateEvent internal constructor(val player: Player, val rocket: RocketDevice) : org.bukkit.event.Event() {

    companion object{
        private val handlers: HandlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList {
        return Companion.handlers
    }
}