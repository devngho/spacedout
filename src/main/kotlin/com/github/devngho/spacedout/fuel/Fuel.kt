/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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