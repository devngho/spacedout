package com.github.devngho.spacedout.event

import com.github.devngho.spacedout.rocket.RocketDevice
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

@Suppress("unused")
class RocketFuelChargeEvent internal constructor(val player: Player, val rocket: RocketDevice, val amount: Int) : org.bukkit.event.Event() {

    companion object{
        private val handlers: HandlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList {
        return Companion.handlers
    }
}