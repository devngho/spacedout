package com.github.devngho.spacedout.blueprint

import com.github.devngho.spacedout.addon.Addon
import org.bukkit.Location

data class Blueprint (val string: String, val location: Location, val addedAddon: Addon)