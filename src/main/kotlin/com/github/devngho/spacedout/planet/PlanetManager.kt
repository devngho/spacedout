/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.planet

import com.github.devngho.spacedout.config.Config
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType


object PlanetManager {
    /**
     * 등록된 행성 목록입니다.
     */
    internal var planets: MutableList<Pair<Planet, World?>> = mutableListOf()

    internal fun generateWorlds(){
        val newPlanets = planets.map {
            if (it.second == null){
                val planet = it.first
                val wc = WorldCreator(planet.codeName)
                wc.generator(planet.chunkGenerator)
                wc.environment(World.Environment.NETHER)
                wc.type(WorldType.NORMAL)
                val newWorld = wc.createWorld()
                if (Config.configConfiguration.getBoolean("planets.useworldborder", false)) {
                    newWorld?.worldBorder?.size = planet.worldBorderSize
                }
                planet.configWorld(newWorld!!)
                Pair(it.first, newWorld)
            }else{
                Pair(it.first, it.second)
            }
        }
        planets = newPlanets.toMutableList()
    }

    fun registerPlanet(planet: Planet){
        planets += Pair(planet, null)
    }

    fun registerPlanet(planet: Planet, world: World){
        planets += Pair(planet, world)
    }
}