package com.github.devngho.spacedout.planet

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.equipment.Equipment
import com.github.devngho.spacedout.equipment.Jetpack
import com.github.devngho.spacedout.equipment.OxygenMask
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


//토성
class Saturn : Planet{
    class SaturnGenerator : ChunkGenerator() {
        // Remember this
        private var currentHeight = 60

        override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider {
            return OneBiomeProvider(Biome.BASALT_DELTAS)
        }
        @Suppress("DEPRECATION")
        override fun generateChunkData(world: World, random: Random, chunkX: Int, chunkZ: Int, biome: BiomeGrid): ChunkData {
            val generator = PerlinOctaveGenerator(Random(world.seed), 8)
            generator.setScale(0.025)
            val chunk = createChunkData(world)
            val rand = Random(world.seed)
            for (X in 0..15) for (Z in 0..15) {
                currentHeight = (generator.noise(
                    (chunkX * 16 + X).toDouble(),
                    (chunkZ * 16 + Z).toDouble(),
                    0.5,
                    0.5
                ) * 30.0 + 100.0).toInt()
                for (i in currentHeight downTo currentHeight - 40) chunk.setBlock(X, i, Z, if (rand.nextBoolean()){Material.ICE} else {Material.STONE})
                for (i in currentHeight downTo currentHeight - 6) chunk.setBlock(X, i, Z, Material.OBSIDIAN)
                for (i in 100 downTo 0) if (chunk.getType(X, i, Z) == Material.AIR) chunk.setBlock(X, i, Z, Material.LAVA)
                chunk.setBlock(X, 0, Z, Material.BEDROCK)
            }
            return chunk
        }
    }
    override fun configWorld(world: World) {
        world.setGameRule(GameRule.DO_WEATHER_CYCLE,  false)
        world.setGameRule(GameRule.DO_MOB_SPAWNING,  false)
        world.isThundering = true
    }

    override fun initPlanetConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("worldbordersize", 512.0)
        configurationSection.set("position", 10.0)
        configurationSection.set("name", "토성")
        configurationSection.set("description", "고리가 없어요.")
        configurationSection.set("graphicmaterial", "GRAY_CONCRETE_POWDER")
    }

    override fun loadPlanetConfig(configurationSection: ConfigurationSection) {
        name = configurationSection.getString("name", "토성")!!
        description = configurationSection.getString("description", "고리가 없어요")!!
        pos = configurationSection.getDouble("position", 10.0)
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "GRAY_CONCRETE_POWDER")!!.uppercase(), false) ?: Material.GRAY_CONCRETE_POWDER
        worldBorderSize = configurationSection.getDouble("worldbordersize", 512.0)
    }

    override var name: String = "토성"
    override val codeName: String = "saturn"
    override var description: String = "고리가 없어요"
    override val chunkGenerator: ChunkGenerator = SaturnGenerator()
    override var pos: Double = 10.0
    override var graphicMaterial: Material = Material.GRAY_CONCRETE_POWDER
    override var worldBorderSize = 512.0
    override val needEquipments: MutableList<Equipment> = mutableListOf(Jetpack(), OxygenMask())
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
}