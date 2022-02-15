package com.github.devngho.spacedout.event

import com.github.devngho.spacedout.rocket.RocketDevice
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

class RocketModuleRemoveEvent internal constructor(val player: Player, val rocket: RocketDevice, val module: com.github.devngho.spacedout.rocket.Module) : org.bukkit.event.Event() {

    companion object{
        private val handlers: HandlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList {
        return Companion.handlers
    }
}