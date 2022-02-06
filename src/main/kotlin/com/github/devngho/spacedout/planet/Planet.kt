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
     */
    fun loadPlanetConfig(configurationSection: ConfigurationSection)

    /**
     * 행성을 추가한 애드온입니다.
     */
    val addedAddon: Addon
}