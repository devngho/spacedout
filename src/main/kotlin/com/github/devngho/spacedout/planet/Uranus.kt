package com.github.devngho.spacedout.planet

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.equipment.Equipment
import com.github.devngho.spacedout.equipment.Jetpack
import com.github.devngho.spacedout.equipment.OxyzenMask
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


//수성
class Uranus : Planet{
    class UranusGenerator : ChunkGenerator() {
        // Remember this
        private var currentHeight = 60

        override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider {
            return OneBiomeProvider(Biome.ICE_SPIKES)
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
                for (i in currentHeight downTo 1) chunk.setBlock(X, i, Z, if (rand.nextBoolean()){Material.ICE} else {Material.BLUE_ICE})
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
        configurationSection.set("position", 19.2)
        configurationSection.set("name", "천왕성")
        configurationSection.set("description", "춥고 춥고 얼어 있습니다.")
        configurationSection.set("graphicmaterial", "ICE")
    }

    override fun loadPlanetConfig(configurationSection: ConfigurationSection) {
        name = configurationSection.getString("name", "천왕성")!!
        description = configurationSection.getString("description", "춥고 춥고 얼어 있습니다.")!!
        pos = configurationSection.getDouble("position", 19.2)
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "ICE")!!.uppercase(), false) ?: Material.ICE
        worldBorderSize = configurationSection.getDouble("worldbordersize", 512.0)
    }

    override var name: String = "천왕성"
    override val codeName: String = "uranus"
    override var description: String = "춥고 춥고 얼어 있습니다."
    override val chunkGenerator: ChunkGenerator = UranusGenerator()
    override var pos: Double = 19.2
    override var graphicMaterial: Material = Material.ICE
    override var worldBorderSize = 512.0
    override val needEquipments: MutableList<Equipment> = mutableListOf(Jetpack(), OxyzenMask())
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
}