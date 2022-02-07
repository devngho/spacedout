package com.github.devngho.spacedout.addon

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

    /**
     * 애드온의 Planet 등을 등록할 함수입니다.
     */
    fun register()
}