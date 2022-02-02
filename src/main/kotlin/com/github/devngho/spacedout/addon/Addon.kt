package com.github.devngho.spacedout.addon

import com.github.devngho.spacedout.buildable.Buildable
import com.github.devngho.spacedout.equipment.Equipment
import com.github.devngho.spacedout.planet.Planet
import net.kyori.adventure.text.Component
import org.bukkit.Material

interface Addon {
    /**
     * 애드온의 이름입니다.
     */
    val name: String

    /**
     * 애드온의 ID입니다.
     */
    val id: String

    /**
     * 애드온의 설명입니다.
     */
    val description: List<Component>

    /**
     * 애드온이 표시될 Material 입니다.
     */
    val graphicMaterial: Material
}