/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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

    /**
     * 로켓의 틱 당 AU입니다.
     */
    var speedDistanceRatio: Double
}