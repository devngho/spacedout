package com.github.devngho.spacedout.buildable

import com.github.devngho.spacedout.addon.Addon
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import com.github.devngho.spacedout.util.Pair

interface Buildable {
    val codeName: String
    val location: Location
    val placeItemMaterial: Material
    val placeItemName: String
    val recipeShape: List<String>
    val recipeItem: List<Pair<Char, Material>>
    val displayBlock: Material
    val addedAddon: Addon
    fun interact(event: PlayerInteractEvent)
    fun clone(location: Location): Buildable
}