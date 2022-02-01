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
            var radius = 0
            radius = if (random.nextInt(100) <= BIG_CRATER_CHANCE) {
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