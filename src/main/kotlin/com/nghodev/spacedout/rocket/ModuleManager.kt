package com.nghodev.spacedout.rocket

object ModuleManager {
    val modules: MutableList<Module> = mutableListOf()
    fun isPlaceable(engine: Engine, modules: List<Module>, placeTopModule: Module): Boolean {
        return when(placeTopModule.moduleType){
            ModuleType.ENGINE -> {
                modules.isEmpty()
            }
            ModuleType.NORMAL -> {
                modules.last().moduleType != ModuleType.NOSECONE && modules.isNotEmpty()
            }
            ModuleType.NOSECONE -> {
                modules.last().moduleType != ModuleType.NOSECONE && modules.isNotEmpty()
            }
        } && engine.maxHeight >= modules.sumOf { it.height } + placeTopModule.height
    }
}