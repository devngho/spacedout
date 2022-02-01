package com.nghodev.spacedout.rocket

import com.nghodev.spacedout.fuel.Fuel

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
     * 로켓이 도달 가능한 최대 거리입니다.
     */
    val maxReachableDistance: UInt

    /**
     * 로켓의 연비입니다.
     */
    val fuelDistanceRatio: Double
}