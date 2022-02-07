package com.github.devngho.spacedout.addon

import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.equipment.EquipmentManager
import com.github.devngho.spacedout.equipment.Jetpack
import com.github.devngho.spacedout.equipment.OxygenMask
import com.github.devngho.spacedout.planet.*
import com.github.devngho.spacedout.rocket.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material

object AddonManager {
    internal val spacedoutAddon: Addon = object:Addon{
        override val name: String = "Spacedout"
        override val id: String = "spacedout"
        override val description: List<Component> = listOf(
            Component.text("Spaced Out 기본 애드온입니다.").color(TextColor.color(0, 127, 0)).decoration(TextDecoration.ITALIC, false),
            Component.text("이 애드온은 기본 포함이며 제거할 수 없습니다.").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false),
            Component.text("다른 애드온을 추가하려면 플러그인 폴더에 넣으세요.").color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
        )
        override val graphicMaterial: Material = Material.END_STONE
        override fun register() {
            PlanetManager.registerPlanet(Mercury())
            PlanetManager.registerPlanet(Venus())
            PlanetManager.registerPlanet(Mars())
            PlanetManager.registerPlanet(Jupiter())
            PlanetManager.registerPlanet(Saturn())
            PlanetManager.registerPlanet(Uranus())
            PlanetManager.registerPlanet(Neptune())
            PlanetManager.registerPlanet(Earth(), Instance.server.worlds[0])

            ModuleManager.modules += CoalEngine()
            ModuleManager.modules += LavaEngine()
            ModuleManager.modules += ControlModule()
            ModuleManager.modules += StandardNosecone()

            EquipmentManager.equipments += Jetpack()
            EquipmentManager.equipments += OxygenMask()
        }
    }
    val addons: MutableList<Addon> = mutableListOf(spacedoutAddon)
    internal fun registerAddon(){
        addons.forEach {
            it.register()
        }
    }
}