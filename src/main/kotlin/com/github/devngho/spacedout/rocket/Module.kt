package com.github.devngho.spacedout.rocket

import com.github.devngho.nplug.api.structure.Structure
import com.github.devngho.spacedout.addon.Addon
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.inventory.InventoryClickEvent

interface Module {
    /**
     * 모듈의 아이디입니다.
     */
    val id: String
    /**
     * 모듈의 중량입니다.
     */
    val height: Int

    /**
     * 모듈 건설에 필요한 재료입니다.
     */
    val buildRequires: List<com.github.devngho.spacedout.util.Pair<Material, Int>>

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
     * [structure]를 사용해 렌더링할 지 결정합니다.
     * [LaunchingRocket]는 [structure]를 사용할 때에만 작동합니다.
     */
    val useStructure: Boolean

    /**
     * 모듈을 렌더링할 [Structure]를 설정합니다.
     * [useStructure]가 true여야 사용됩니다.
     *
     */
    val structure: Structure?

    /**
     * 모듈을 렌더링할 때 호출될 함수입니다.
     * 블럭을 설치하는 방식으로 렌더링하세요.
     * @param rocket 모듈이 장착되어 있는 로켓의 RocketDevice입니다.
     * @param position 렌더링을 시작할 위치입니다.
     */
    fun render(rocket: RocketDevice, position: Location)

    /**
     * 모듈을 로켓에 추가할 때 새 인스턴스를 만들 때 호출될 함수입니다.
     * this를 반환하지 말고 기본 상태의 새로운 인스턴스를 반환하세요.
     */
    fun newInstance(): Module

    /**
     * 모듈의 종류입니다.
     */
    val moduleType: ModuleType

    /**
     * 모듈의 설정 값을 초기화합니다.
     * @param configurationSection 해당 행성의 컨피그 섹션입니다.
     */
    fun initModuleConfig(configurationSection: ConfigurationSection)

    /**
     * 모듈의 설정 값을 읽어들입니다.
     * @param configurationSection 해당 행성의 컨피그 섹션입니다.
     */
    fun loadModuleConfig(configurationSection: ConfigurationSection)

    /**
     * 모듈이 저장 후 로딩될 때 Map 를 읽어 로딩합니다.
     * @param map 읽을 Map 입니다.
     */
    fun loadModuleValue(map: MutableMap<Any, Any>)

    /**
     * 모듈이 저장될 때 Map 로 저장합니다.
     * @return 저장될 Map 입니다.
     */
    fun saveModuleValue(): MutableMap<Any, Any>

    /**
     * 모듈을 추가한 애드온입니다.
     */
    val addedAddon: Addon

    /**
     * 모듈 아이템을 클릭할 때 호출되는 함수입니다.
     * @param event 클릭 이벤트입니다.
     */
    fun onClick(event: InventoryClickEvent)

    /**
     * 아이템의 설명을 렌더링할 때 호출되는 함수입니다.
     * @return 아이템의 설명 컴포넌트입니다.
     */
    fun renderLore(locale: String): List<Component>

    /**
     * 모듈의 보호 범위를 설정합니다.
     * 로켓은 모듈 중 가장 최대 범위를 반경으로 보호됩니다.
     * 구조물의 최대 반경으로 설정하세요.
     */
    val protectionRange: Int
}