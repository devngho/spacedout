package com.github.devngho.spacedout.rocket

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.fuel.Fuel
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

class CoalEngine() : Engine {
    override val id: String = "coalengine"
    override var height: Int = 5
    override var maxFuelHeight: Int = 100
    override var maxHeight: Int = 20
    override var supportFuel: Fuel = Fuel.COAL
    override var name: String = "석탄 엔진"
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.COBBLESTONE, 50), Pair(Material.STONE, 10))
    override var graphicMaterial: Material = Material.COAL
    override val sizeY: Int = 2
    override val maxReachableDistance: UInt = 20000u
    override var fuelDistanceRatio: Double = 50.0
    override val moduleType: ModuleType = ModuleType.ENGINE
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
    override fun initModuleConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("height", 5)
        configurationSection.set("maxfuelheight", 100)
        configurationSection.set("maxheight", 20)
        configurationSection.set("name", "석탄 엔진")
        configurationSection.set("graphicmaterial", "COAL")
        configurationSection.set("fueldistanceratio", 50.0)
    }

    override fun loadModuleConfig(configurationSection: ConfigurationSection) {
        height = configurationSection.getInt("height", 5)
        maxFuelHeight = configurationSection.getInt("maxfuelheight", 100)
        maxHeight = configurationSection.getInt("maxheight", 20)
        name = configurationSection.getString("name", "석탄 엔진").toString()
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "COAL")!!.uppercase(), false) ?: Material.COAL
        fuelDistanceRatio = configurationSection.getDouble("fueldistanceratio", 50.0)
    }

    override fun render(rocket: RocketDevice, position: Location) {
        for (x in -2..2) {
            for (z in -2..2) {
                position.clone().add(x.toDouble(), 0.0, z.toDouble()).block.type = Material.STONE
            }
        }
        for (x in -1..1) {
            for (z in -1..1) {
                position.clone().add(x.toDouble(), 1.0, z.toDouble()).block.type = Material.STONE
            }
        }
    }

    override fun newInstance(): Module {
        return CoalEngine()
    }

    override fun loadModuleValue(map: MutableMap<Any, Any>) {}
    override fun saveModuleValue(): MutableMap<Any, Any> {return mutableMapOf() }
}