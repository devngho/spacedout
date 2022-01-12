package com.nghodev.spacedout.fuel

import org.bukkit.Material

enum class Fuel {
    COAL {
        override fun toMaterial(): Material {
            return Material.COAL
        }

        override fun getUnit(): String {
            return "개"
        }
    },
    LAVA {
        override fun toMaterial(): Material {
            return Material.LAVA_BUCKET
        }

        override fun getUnit(): String {
            return "양동이"
        }
    };
    abstract fun toMaterial(): Material
    abstract fun getUnit(): String
}