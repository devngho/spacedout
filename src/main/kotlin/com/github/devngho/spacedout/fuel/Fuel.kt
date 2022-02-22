package com.github.devngho.spacedout.fuel

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
    },
    DIAMOND {
        override fun toMaterial(): Material {
            return Material.DIAMOND
        }

        override fun getUnit(): String {
            return "개"
        }
    },
    GUNPOWDER {
        override fun getUnit(): String {
            return "개"
        }

        override fun toMaterial(): Material {
            return Material.GUNPOWDER
        }
    };
    abstract fun toMaterial(): Material
    abstract fun getUnit(): String
}