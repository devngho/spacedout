package com.github.devngho.spacedout.planet

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.equipment.Equipment
import com.github.devngho.spacedout.equipment.Jetpack
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
class Jupiter : Planet{
    class JupiterGenerator : ChunkGenerator() {
        // Remember this
        private var currentHeight = 60

        override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider {
            return OneBiomeProvider(Biome.BASALT_DELTAS)
        }
        override fun generateChunkData(world: World, random: Random, chunkX: Int, chunkZ: Int, biome: BiomeGrid): ChunkData {
            val generator = PerlinOctaveGenerator(Random(world.seed), 8)
            generator.setScale(0.05)
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
        configurationSection.set("worldbordersize", 1024.0)
        configurationSection.set("position", 40000)
        configurationSection.set("name", "목성")
        configurationSection.set("description", "이 행성은 수소가 있었는데 구현의 한계로 용암이 되었답니다.")
        configurationSection.set("graphicmaterial", "ORANGE_CONCRETE_POWDER")
    }

    override fun loadPlanetConfig(configurationSection: ConfigurationSection) {
        name = configurationSection.getString("name", "목성")!!
        description = configurationSection.getString("description", "이 행성은 수소가 있었는데 구현의 한계로 용암이 되었답니다.")!!
        pos = configurationSection.getInt("position", 40000).toUInt()
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "ORANGE_CONCRETE_POWDER")!!.uppercase(), false) ?: Material.ORANGE_CONCRETE_POWDER
        worldBorderSize = configurationSection.getDouble("worldbordersize", 1024.0)
    }

    override var name: String = "목성"
    override val codeName: String = "jupiter"
    override var description: String = "이 행성은 수소가 있었는데 구현의 한계로 용암이 되었답니다."
    override val chunkGenerator: ChunkGenerator = JupiterGenerator()
    override var pos: UInt = 40000u
    override var graphicMaterial: Material = Material.ORANGE_CONCRETE_POWDER
    override var worldBorderSize = 1024.0
    override val needEquipments: MutableList<Equipment> = mutableListOf(Jetpack())
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
}