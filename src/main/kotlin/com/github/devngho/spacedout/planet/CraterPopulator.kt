/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */



package com.github.devngho.spacedout.planet

import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.util.BlockVector
import org.bukkit.util.Vector
import java.util.*

class CraterPopulator(
    private val CRATER_CHANCE: Int,
    private val MIN_CRATER_SIZE: Int,
    private val SMALL_CRATER_SIZE: Int,
    private val BIG_CRATER_SIZE: Int,
    private val BIG_CRATER_CHANCE: Int) : BlockPopulator() {
    override fun populate(world: World, random: Random, source: Chunk) {
        if (random.nextInt(100) <= CRATER_CHANCE) {
            val centerX = (source.x shl 4) + random.nextInt(16)
            val centerZ = (source.z shl 4) + random.nextInt(16)
            val centerY = world.getHighestBlockYAt(centerX, centerZ)
            val center: Vector = BlockVector(centerX, centerY, centerZ)
            val radius = if (random.nextInt(100) <= BIG_CRATER_CHANCE) {
                random.nextInt(BIG_CRATER_SIZE - MIN_CRATER_SIZE + 1) + MIN_CRATER_SIZE
            } else {
                random.nextInt(SMALL_CRATER_SIZE - MIN_CRATER_SIZE + 1) + MIN_CRATER_SIZE
            }
            for (x in -radius..radius) {
                for (y in -radius..radius) {
                    for (z in -radius..radius) {
                        val position = center.clone().add(Vector(x, y, z))
                        if (center.distance(position) <= radius + 0.5) {
                            world.getBlockAt(position.toLocation(world)).type = Material.AIR
                        }
                    }
                }
            }
        }
    }
}