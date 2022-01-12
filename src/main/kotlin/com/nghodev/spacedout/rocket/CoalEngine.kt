package com.nghodev.spacedout.rocket

import com.nghodev.spacedout.fuel.Fuel
import org.bukkit.Location
import org.bukkit.Material

class CoalEngine() : Engine {
    override val height: Int = 5
    override val maxFuelHeight: Int = 100
    override val maxHeight: Int = 160
    override val supportFuel: Fuel = Fuel.COAL
    override val name: String = "석탄 로켓"
    override val codeName: String = "COAL_ENGINE"
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.COBBLESTONE, 5000), Pair(Material.STONE, 10))
    override val graphicMaterial: Material = Material.COAL
    override val sizeY: Int = 2
    override val maxReachableDistance: UInt = 20000u
    override val fuelDistanceRatio: Double = 50.0
    override val moduleType: ModuleType = ModuleType.ENGINE

    override fun render(rocket: RocketDevice, position: Location) {
        for (x in -2..2) {
            for (z in -2..2) {
                position.clone().add(x.toDouble(), 0.0, z.toDouble()).block.type = Material.STONE
            }
        }
        for (x in -1..1) {
            for (z in -1..1) {
                position.clone().add(x.toDouble(), 1.0, z.toDouble()).block.type = Material.STONE
            }
        }
    }
}