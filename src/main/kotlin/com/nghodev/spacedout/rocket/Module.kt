package com.nghodev.spacedout.rocket

import org.bukkit.Location
import org.bukkit.Material

interface Module {
    /**
     * 모듈의 중량입니다.
     */
    val height: Int

    /**
     * 모듈 건설에 필요한 재료입니다.
     */
    val buildRequires: List<Pair<Material, Int>>

    /**
     * 모듈의 렌더링 높이입니다.
     */
    val sizeY: Int

    /**
     * 모듈의 이름입니다.
     */
    val name: String

    /**
     * 모듈의 아이콘의 아이템 종류입니다.
     */
    val graphicMaterial: Material

    /**
     * 모듈을 렌더링할 때 호출될 함수입니다.
     * 블럭을 설치하는 방식으로 렌더링하세요.
     * @param rocket 모듈이 장착되어 있는 로켓의 RocketDevice입니다.
     * @param position 렌더링을 시작할 위치입니다.
     */
    fun render(rocket: RocketDevice, position: Location)

    /**
     * 모듈의 종류입니다.
     */
    val moduleType: ModuleType
}