package com.github.devngho.spacedout.rocket

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.fuel.Fuel
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

class LavaEngine : Engine {
    override var height: Int = 8
    override var maxFuelHeight: Int = 20
    override var maxHeight: Int = 40
    override val supportFuel: Fuel = Fuel.LAVA
    override var name: String = "용암 엔진"
    override val id: String = "lavaengine"
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.COBBLESTONE, 50), Pair(Material.STONE, 10), Pair(Material.IRON_INGOT, 30), Pair(Material.OBSIDIAN, 50))
    override var graphicMaterial: Material = Material.LAVA_BUCKET
    override val sizeY: Int = 3
    override val maxReachableDistance: UInt = 20000u
    override var fuelDistanceRatio: Double = 1000.0
    override val moduleType: ModuleType = ModuleType.ENGINE
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon

    override fun render(rocket: RocketDevice, position: Location) {
        for (x in -2..2) {
            for (z in -2..2) {
                position.clone().add(x.toDouble(), 0.0, z.toDouble()).block.type = Material.IRON_BLOCK
            }
        }
        for (x in -1..1) {
            for (z in -1..1) {
                position.clone().add(x.toDouble(), 1.0, z.toDouble()).block.type = Material.RED_CONCRETE
            }
        }
        for (x in -1..1) {
            for (z in -1..1) {
                position.clone().add(x.toDouble(), 2.0, z.toDouble()).block.type = Material.IRON_BLOCK
            }
        }
    }

    override fun newInstance(): Module {
        return LavaEngine()
    }

    override fun initModuleConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("height", 8)
        configurationSection.set("maxfuelheight", 20)
        configurationSection.set("maxheight", 40)
        configurationSection.set("name", "용암 엔진")
        configurationSection.set("graphicmaterial", "LAVA_BUCKET")
        configurationSection.set("fueldistanceratio", 1000.0)
    }

    override fun loadModuleConfig(configurationSection: ConfigurationSection) {
        height = configurationSection.getInt("height", 8)
        maxFuelHeight = configurationSection.getInt("maxfuelheight", 20)
        maxHeight = configurationSection.getInt("maxheight", 20)
        name = configurationSection.getString("name", "용암 엔진").toString()
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "LAVA_BUCKET")!!.uppercase(), false) ?: Material.LAVA_BUCKET
        fuelDistanceRatio = configurationSection.getDouble("fueldistanceratio", 1000.0)
    }
}