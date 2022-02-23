/*
Copyright 2022, ngho

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.devngho.spacedout.config

import com.github.devngho.spacedout.Instance
import net.kyori.adventure.text.Component
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

object I18n {
    var langCache: MutableMap<String, FileConfiguration> = mutableMapOf()
    fun loadAll() {
        val folder = File(Instance.plugin.dataFolder, File.separator + "resource" + File.separator + "lang")
        folder.walk().forEach {
            if (it.isFile) {
                val f = it
                val langData: FileConfiguration = YamlConfiguration.loadConfiguration(f)
                langData.save(f)
                langCache[f.nameWithoutExtension] = langData
            }
        }
    }
    @Suppress("UNUSED")
    fun appendComponent(text: Component, lang: String, key: String): Component{
        return if(langCache.containsKey(lang)){
            text.append(Component.text(
                langCache[lang]!!.getString(
                    key, langCache[
                            Config.configConfiguration.getString("server.defaultlang")
                    ]!!.getString(key, "")
                )!!)
            )
        }else{
            text.append(Component.text(langCache[
                    Config.configConfiguration.getString("server.defaultlang")
            ]!!.getString(key, "")!!))
        }
    }
    fun getComponent(lang: String, key: String): Component{
        return if(langCache.containsKey(lang)){
            Component.text(
                langCache[lang]!!.getString(
                    key, langCache[
                            Config.configConfiguration.getString("server.defaultlang")
                    ]!!.getString(key, "")
                )!!)
        }else{
            Component.text(langCache[
                    Config.configConfiguration.getString("server.defaultlang")
            ]!!.getString(key, "")!!)
        }
    }
    fun getString(lang: String, key: String): String{
        return if(langCache.containsKey(lang)){
                langCache[lang]!!.getString(
                    key, langCache[
                            Config.configConfiguration.getString("server.defaultlang")
                    ]!!.getString(key, "")
                )!!
        }else{
            langCache[
                    Config.configConfiguration.getString("server.defaultlang")
            ]!!.getString(key, "")!!
        }
    }
    fun saveLangData(){
        val folder = File(Instance.plugin.dataFolder, File.separator + "resource" + File.separator + "lang")
        langCache.forEach {
            it.value.save(File(folder, File.separator + it.key + ".yml"))
        }
    }
}

fun Player.getLang(): String{
    return "${this.locale().language}-${this.locale().country}".lowercase()
}