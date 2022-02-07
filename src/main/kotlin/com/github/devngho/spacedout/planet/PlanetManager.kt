package com.github.devngho.spacedout.planet

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
                newWorld?.worldBorder?.size = planet.worldBorderSize
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