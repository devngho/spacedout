/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.rocket

import com.github.devngho.nplug.api.structure.Structure
import com.github.devngho.spacedout.Instance
import com.github.devngho.spacedout.addon.Addon
import com.github.devngho.spacedout.addon.AddonManager
import com.github.devngho.spacedout.config.I18n
import com.github.devngho.spacedout.config.StructureLoader
import com.github.devngho.spacedout.util.Pair
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.inventory.InventoryClickEvent
import java.io.File
import java.util.*

class ControlModule(override var height: Int = 10, override var graphicMaterial: Material = Material.CALCITE) : Module {
    var ridedPlayer: UUID? = null
    override val id: String = "controlmodule"
    override var name: String = "modules.${id}"
    override val sizeY: Int = 6
    override val buildRequires: List<Pair<Material, Int>> = listOf(Pair(Material.STONE, 30), Pair(Material.IRON_INGOT, 5))
    override val moduleType: ModuleType = ModuleType.NORMAL
    override val addedAddon: Addon
        get() = AddonManager.spacedoutAddon
    override val structure: Structure = StructureLoader.loadFromFile(File(Instance.plugin.dataFolder.absolutePath + File.separator + "resource/modules" + File.separator + "$id.json"))
    override val useStructure: Boolean = true
    override val protectionRange: Int = 3

    override fun render(rocket: RocketDevice, position: Location) {
        for(y in 0 until sizeY) {
            for (x in -1..1) {
                for (z in -1..1) {
                    position.clone().add(x.toDouble(), y.toDouble(), z.toDouble()).block.type = Material.IRON_BLOCK
                }
            }
        }
    }

    override fun newInstance(): Module {
        return ControlModule(height, graphicMaterial)
    }
    override fun initModuleConfig(configurationSection: ConfigurationSection) {
        configurationSection.set("height", 10)
        configurationSection.set("graphicmaterial", "CALCITE")
    }

    override fun loadModuleConfig(configurationSection: ConfigurationSection) {
        height = configurationSection.getInt("height", 10)
        graphicMaterial = Material.getMaterial(configurationSection.getString("graphicmaterial", "CALCITE")!!.uppercase(), false) ?: Material.CALCITE
    }
    override fun loadModuleValue(map: MutableMap<Any, Any>) {
        if (map.containsKey("ridedplayer")) {
            try {
                ridedPlayer = UUID.fromString(map["ridedplayer"]!!.toString())
            }catch (_: Exception){}
        }
    }
    override fun saveModuleValue(): MutableMap<Any, Any> {return mutableMapOf(kotlin.Pair("ridedplayer", ridedPlayer?.toString() ?: "")) }
    override fun onClick(event: InventoryClickEvent) {
        ridedPlayer = if (event.whoClicked.uniqueId == ridedPlayer){
            null
        }else {
            event.whoClicked.uniqueId
        }
    }

    override fun renderLore(locale: String): List<Component> {
        val returnValue = mutableListOf<Component>()
        val p = ridedPlayer
        if (p != null){
            returnValue.add(Component.text(Instance.server.getOfflinePlayer(p).name!!).color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false
            ))
        }
        returnValue.add(Component.text(I18n.getString(locale, "controlmodule.clicktoselect")).color(TextColor.color(255, 255, 255)).decoration(TextDecoration.ITALIC, false
        ))
        return returnValue
    }

    init {
        if (I18n.langCache.containsKey("en-us")){
            if (!I18n.langCache["en-us"]!!.contains("controlmodule")){
                I18n.langCache["en-us"]!!.createSection("controlmodule")
                I18n.langCache["en-us"]!!.set("controlmodule.clicktoselect", "Click to ride.")
            }
        }
        if (I18n.langCache.containsKey("ko-kr")){
            if (!I18n.langCache["ko-kr"]!!.contains("controlmodule")){
                I18n.langCache["ko-kr"]!!.createSection("controlmodule")
                I18n.langCache["ko-kr"]!!.set("controlmodule.clicktoselect", "클릭해서 탑승하세요.")
            }
        }
    }
}