package com.github.devngho.spacedout.buildable

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent

interface Buildable {
    val codeName: String
    val location: Location
    val placeItemMaterial: Material
    val placeItemName: String
    val recipeShape: List<String>
    val recipeItem: List<Pair<Char, Material>>
    val displayBlock: Material
    fun interact(event: PlayerInteractEvent)
    fun clone(location: Location): Buildable
}