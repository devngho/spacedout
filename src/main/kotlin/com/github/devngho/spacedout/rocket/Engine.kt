package com.github.devngho.spacedout.rocket

import com.github.devngho.spacedout.fuel.Fuel

interface Engine : Module, Cloneable{
    /**
     * 로켓의 최대 연료 중량입니다.
     */
    val maxFuelHeight: Int

    /**
     * 로켓의 최대 중량입니다.
     */
    val maxHeight: Int

    /**
     * 로켓이 호환하는 연료 목록입니다.
     */
    val supportFuel: Fuel

    /**
     * 로켓의 연비입니다.
     */
    val fuelDistanceRatio: Double
}