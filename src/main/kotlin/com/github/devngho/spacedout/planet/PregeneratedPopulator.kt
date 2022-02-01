package com.github.devngho.spacedout.planet

import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.*

interface PregeneratedPopulator {
    fun populate(world: WorldInfo, random: Random, source: ChunkGenerator.ChunkData, chunkX: Int, chunkZ: Int)
}