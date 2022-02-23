/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
            ModuleManager.modules += BigLavaEngine()
            ModuleManager.modules += GunpowderEngine()
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