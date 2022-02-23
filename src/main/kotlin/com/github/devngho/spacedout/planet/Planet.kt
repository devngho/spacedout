/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.planet

import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.equipment.Equipment
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.generator.ChunkGenerator

interface Planet {
    /**
     * 행성의 이름입니다.
     */
    val name: String

    /**
     * 행성의 코드네임입니다.
     */
    val codeName: String

    /**
     * 행성의 청크 제네레이터입니다.
     */
    val chunkGenerator: ChunkGenerator

    /**
     * 행성의 태양으로부터 거리입니다. 단위는 AU입니다.
     */
    val pos: Double

    /**
     * 행성 선택 창에 렌더링될 아이템의 종류입니다.
     */
    val graphicMaterial: Material

    /**
     * 월드 보더의 크기입니다.
     */
    val worldBorderSize: Double

    /**
     * 행성의 설명입니다.
     */
    val description: String

    /**
     * 행성에 가려면 필요한 방호구들의 목록입니다.
     */
    val needEquipments: MutableList<Equipment>

    /**
     * 월드를 설정할 수 있도록 행성의 월드가 생성되면 호출됩니다.
     * @param world 등록된 행성의 생성된 월드입니다.
     */
    fun configWorld(world: World)

    /**
     * 행성의 설정 값을 초기화합니다.
     * @param configurationSection 해당 행성의 컨피그 섹션입니다.
     */
    fun initPlanetConfig(configurationSection: ConfigurationSection)

    /**
     * 행성의 설정 값을 읽어들입니다.
     * @param configurationSection 해당 행성의 컨피그 섹션입니다.
     * @return 초기화해야 할 때 호출하세요.
     */
    fun loadPlanetConfig(configurationSection: ConfigurationSection): Boolean

    /**
     * 행성을 추가한 애드온입니다.
     */
    val addedAddon: Addon
}