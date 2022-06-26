package com.github.devngho.spacedout.util

import org.bukkit.Material
import org.bukkit.entity.HumanEntity

object ItemUtil {
    fun HumanEntity.hasMaterialCount(material: Material, count: Int): Boolean{
        return (this.inventory.contents?.sumOf { if (it?.type == material) it.amount else 0 } ?: 0) >= count
    }

    fun HumanEntity.useMaterialCount(material: Material, count: Int){
        var used = count
        this.inventory.contents?.let { a ->
            a.forEach {
                it?.let { i ->
                    if (i.type == material){
                        if (i.amount > used){
                            i.subtract(used)
                            return
                        }else {
                            i.subtract(i.amount)
                            used -= i.amount
                        }
                    }
                }
            }
        }
    }
}