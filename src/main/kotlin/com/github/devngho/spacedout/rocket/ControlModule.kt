package com.github.devngho.spacedout.rocket

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

class ControlModule : Module {
    var isRided = false
    override val id: String = "controlengine"
    override var name: String = "탑승 모듈"
    override val sizeY: Int = 3
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.STONE, 30), Pair(Material.IRON_INGOT, 5))
    override var height: Int = 10
    override var graphicMaterial: Material = Material.CALCITE
    override val moduleType: ModuleType = ModuleType.NORMAL
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon

    override fun render(rocket: RocketDevice, position: Location) {
        for(y in 0..sizeY) {
            for (x in -1..1) {
                for (z in -1..1) {
                    position.clone().add(x.toDouble(), y.toDouble(), z.toDouble()).block.type = Material.IRON_BLOCK
                }
            }
        }
    }

    override fun newInstance(): Module {
        return ControlModule()
    }
    override fun initModuleConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("height", 10)
        configurationSection.set("name", "탑승 모듈")
        configurationSection.set("graphicmaterial", "CALCITE")
    }

    override fun loadModuleConfig(configurationSection: ConfigurationSection) {
        height = configurationSection.getInt("height", 10)
        name = configurationSection.getString("name", "탑승 모듈").toString()
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "CALCITE")!!.uppercase(), false) ?: Material.CALCITE
    }
}