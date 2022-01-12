package com.nghodev.spacedout.rocket

import kotlin.reflect.KClass

object ModuleManager {
    val modules: MutableList<KClass<Module>> = mutableListOf()
    fun isPlaceable(modules: List<Module>, placeTopModule: Module): Boolean {
        return when(placeTopModule.moduleType){
            ModuleType.ENGINE -> {
                modules.isEmpty()
            }
            ModuleType.NORMAL -> {
                modules.last().moduleType != ModuleType.NOSECONE && modules.isNotEmpty()
            }
            ModuleType.NOSECONE -> {
                modules.isNotEmpty()
            }
        }
    }
}