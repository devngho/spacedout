/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.rocket

import com.github.devngho.nplug.api.entity.FallingBlock
import com.github.devngho.nplug.api.entity.FallingBlocks
import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.config.Config
import com.github.devngho.spacedout.config.PlayerData.config
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.Vector
import kotlin.math.abs


class LaunchingRocket(val modules: List<Module>, val location: Location, landing: Boolean) {
    private var fallingBlocksPerModule = mutableListOf<Pair<Vector, FallingBlocks>>()
    init {
        var height = 0
        modules.forEach { module ->
            if (module.useStructure){
                val fallingBlocks = FallingBlocks.createFallingBlocks(module.structure!!.blocks.map {
                    Pair(it.first, FallingBlock.createFallingBlock(
                        it.second,
                        location.clone(),
                        Instance.plugin,
                        false,
                        location.world.players.filter { p -> p.config?.get("use_falling", false) as? Boolean ?: false }.toMutableList())
                    )
                }.toMutableList(), location.clone(), Instance.plugin)
                fallingBlocksPerModule.add(Pair(Vector(0.0, height.toDouble(), 0.0), fallingBlocks))
                height += module.sizeY
            }
        }
        var tick = 0
        val task: Int
        if (!landing) {
            task = Instance.server.scheduler.scheduleSyncRepeatingTask(Instance.plugin, {
                fallingBlocksPerModule = fallingBlocksPerModule.map {
                    it.second.position = it.second.position.apply {
                        x = location.x + it.first.x
                        y = location.y + it.first.y
                        z = location.z + it.first.z
                    }
                    Pair(it.first, it.second)
                }.toMutableList()
                location.y += (tick / 60.0) * (tick / 60.0)
                location.world.spawnParticle(Particle.FLAME, location, 10)
                tick += 1
            }, 0, 1)
        }else{
            var plusY = 0.0
            for (i in 0..Config.configConfiguration.getLong("rocket.fallinglaunchtick", 100)){
                plusY += (i / 60.0) * (i / 60.0)
            }
            location.y += plusY
            task = Instance.server.scheduler.scheduleSyncRepeatingTask(Instance.plugin, {
                fallingBlocksPerModule = fallingBlocksPerModule.map {
                    it.second.position = it.second.position.apply {
                        x = location.x + it.first.x
                        y = location.y + it.first.y
                        z = location.z + it.first.z
                    }
                    Pair(it.first, it.second)
                }.toMutableList()
                location.y -= ((abs(tick - 100)) / 60.0) * ((abs(tick - 100)) / 60.0)
                location.world.spawnParticle(Particle.FLAME, location, 10)
                tick += 1
            }, 0, 1)
        }
        Instance.server.scheduler.scheduleSyncDelayedTask(Instance.plugin, {
            Instance.server.scheduler.cancelTask(task)
            fallingBlocksPerModule.forEach {
                it.second.remove()
            }
        }, Config.configConfiguration.getLong("rocket.fallinglaunchtick", 100))
    }

    @Suppress("unused")
    fun refresh(){
        fallingBlocksPerModule.forEach {
            it.second.fallingBlocks.forEach { v ->
                v.second.refreshPlayersForce()
            }
        }
    }
}