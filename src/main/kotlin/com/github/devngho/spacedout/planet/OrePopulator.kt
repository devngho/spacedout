package com.github.devngho.spacedout.planet

import org.bukkit.Material
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.*


class OrePopulator(
    private val tries: Int,
    private val chance: Int,
    private val veinConnectChance: Int,
    private val generateMaterial: Material,
    private val replaceBlock: List<Material>,
    private val oreSpawnY: Int,
    private val oreSpawnRadius: Int
) : PregeneratedPopulator {
    override fun populate(world: WorldInfo, random: Random, source: ChunkGenerator.ChunkData, chunkX: Int, chunkZ: Int) {
        var x: Int
        var y: Int
        var z: Int
        var isStone: Boolean
        for (i in 1..tries) {  // Number of tries
            if (random.nextInt(100) < chance) {  // The chance of spawning
                x = random.nextInt(15)
                z = random.nextInt(15)
                y = random.nextInt(oreSpawnRadius) + oreSpawnY // Get randomized coordinates
                if (replaceBlock.contains(source.getType(x, y, z))) {
                    isStone = true
                    while (isStone) {
                        try {
                            source.setBlock(x, y, z, generateMaterial)
                        }catch (e: Exception){
                            return
                        }
                        isStone = if (random.nextInt(100) <= veinConnectChance) {   // The chance of continuing the vein
                            when (random.nextInt(6)) {
                                0 -> x++
                                1 -> y++
                                2 -> z++
                                3 -> x--
                                4 -> y--
                                5 -> z--
                            }
                            replaceBlock.contains(source.getType(x, y, z))
                        } else false
                    }
                }
            }
        }
    }
}