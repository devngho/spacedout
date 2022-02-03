package com.github.devngho.spacedout.planet

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.equipment.Equipment
import com.github.devngho.spacedout.equipment.OxyzenMask
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.*


//수성
class Mercury : Planet{
    class MercuryGenerator : ChunkGenerator() {
        override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider {
            return OneBiomeProvider(Biome.CRIMSON_FOREST)
        }
        @Suppress("DEPRECATION")
        override fun generateChunkData(world: World, random: Random, chunkX: Int, chunkZ: Int, biome: BiomeGrid): ChunkData {
            val generator = SimplexOctaveGenerator(Random(world.seed), 8)
            val chunk = createChunkData(world)
            generator.setScale(0.01)
            for (X in 0..15) for (Z in 0..15) {
                val currentHeight = (generator.noise(
                    (chunkX * 16 + X).toDouble(),
                    (chunkZ * 16 + Z).toDouble(),
                    0.5,
                    0.5
                ) * 15.0 + 40.0).toInt()
                for (i in currentHeight downTo currentHeight - 10) chunk.setBlock(X, i, Z, Material.BLACKSTONE)
                for (i in currentHeight - 10 downTo 1) chunk.setBlock(X, i, Z, Material.LAVA)
                chunk.setBlock(X, 0, Z, Material.BEDROCK)
            }
            return chunk
        }
    }
    override fun configWorld(world: World) {
        world.setGameRule(GameRule.DO_WEATHER_CYCLE,  false)
        world.setGameRule(GameRule.DO_MOB_SPAWNING,  false)
    }
    override fun initPlanetConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("worldbordersize", 256.0)
        configurationSection.set("position", 5000)
        configurationSection.set("name", "수성")
        configurationSection.set("description", "이 행성은 상당히 척박하지만 용암이 많습니다.")
        configurationSection.set("graphicmaterial", "BLACKSTONE")
    }

    override fun loadPlanetConfig(configurationSection: ConfigurationSection) {
        name = configurationSection.getString("name", "수성")!!
        description = configurationSection.getString("description", "이 행성은 상당히 척박하지만 용암이 많습니다.")!!
        pos = configurationSection.getInt("position", 5000).toUInt()
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "BLACKSTONE")!!.uppercase(), false) ?: Material.BLACKSTONE
        worldBorderSize = configurationSection.getDouble("worldbordersize", 256.0)
    }
    override var name: String = "수성"
    override val codeName: String = "mercury"
    override var description: String = "이 행성은 상당히 척박하지만 용암이 많습니다."
    override val chunkGenerator: ChunkGenerator = MercuryGenerator()
    override var pos: UInt = 5000u
    override var graphicMaterial: Material = Material.BLACKSTONE
    override var worldBorderSize = 256.0
    override val needEquipments: MutableList<Equipment> = mutableListOf(OxyzenMask())
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
}