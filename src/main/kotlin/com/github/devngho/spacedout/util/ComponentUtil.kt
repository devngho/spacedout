package com.github.devngho.spacedout.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

object ComponentUtil {
    val Component.clear : Component
    get(){
        return this.color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false)
    }
}