/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.planet

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.equipment.Equipment
import com.github.devngho.spacedout.equipment.OxygenMask
import de.tr7zw.nbtinjector.javassist.NotFoundException
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import org.bukkit.util.noise.PerlinOctaveGenerator
import java.util.*


//화성
class Mars : Planet{
    class MarsGenerator : ChunkGenerator() {
        // Remember this
        private var currentHeight = 60
        private val prePopulators: List<PregeneratedPopulator> = listOf(
            OrePopulator(30, 70, 90, Material.RAW_IRON_BLOCK, listOf(Material.SAND, Material.STONE, Material.BASALT), 40, 20))

        override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider {
            return OneBiomeProvider(Biome.DESERT)
        }
        @Suppress("DEPRECATION")
        override fun generateChunkData(world: World, random: Random, chunkX: Int, chunkZ: Int, biome: BiomeGrid): ChunkData {
            val generator = PerlinOctaveGenerator(Random(world.seed), 8)
            generator.setScale(0.01)
            val chunk = createChunkData(world)
            for (X in 0..15) for (Z in 0..15) {
                currentHeight = (generator.noise(
                    (chunkX * 16 + X).toDouble(),
                    (chunkZ * 16 + Z).toDouble(),
                    0.5,
                    0.5
                ) * 15.0 + 50.0).toInt()
                for (i in currentHeight downTo currentHeight - 40) chunk.setBlock(X, i, Z, Material.BASALT)
                for (i in currentHeight downTo currentHeight - 18) chunk.setBlock(X, i, Z, Material.STONE)
                for (i in currentHeight downTo currentHeight - 3) chunk.setBlock(X, i, Z, Material.SAND)
                for (i in currentHeight - 41 downTo 1) chunk.setBlock(X, i, Z, Material.LAVA)
                for (i in world.maxHeight..world.minHeight) biome.setBiome(X, i, Z, Biome.DESERT)
                chunk.setBlock(X, 0, Z, Material.BEDROCK)
            }
            prePopulators.forEach { it.populate(world, random, chunk, chunkX, chunkZ) }
            return chunk
        }
    }
    override fun configWorld(world: World) {
        world.setGameRule(GameRule.DO_WEATHER_CYCLE,  false)
        world.setGameRule(GameRule.DO_MOB_SPAWNING,  false)
        world.populators.add(CraterPopulator(25,3,4,9,10))
    }

    override fun initPlanetConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("worldbordersize", 256.0)
        configurationSection.set("position", 1.5)
        configurationSection.set("graphicmaterial", "SAND")
    }

    override fun loadPlanetConfig(configurationSection: ConfigurationSection) : Boolean {
        if (!configurationSection.contains("position")) return true
        pos = configurationSection.getDouble("position", 1.5)
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "SAND")!!.uppercase(), false) ?: Material.SAND
        worldBorderSize = configurationSection.getDouble("worldbordersize", 256.0)
        return false
    }

    override val codeName: String = "mars"
    override var name: String = "planets.${codeName}"
    override var description: String = "planets.${codeName}_description"
    override val chunkGenerator: ChunkGenerator = MarsGenerator()
    override var pos: Double = 1.5
    override var graphicMaterial: Material = Material.SAND
    override var worldBorderSize = 256.0
    override val needEquipments: MutableList<Equipment> = mutableListOf(OxygenMask())
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
}