package com.sifsstudio.botjs.data

import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.entity.Entities
import com.sifsstudio.botjs.item.Items
import net.minecraft.data.PackOutput
import net.minecraft.world.item.BlockItem
import net.neoforged.neoforge.common.data.LanguageProvider
import net.neoforged.neoforge.registries.DeferredHolder

class ModLangEn(packOutput: PackOutput) : LanguageProvider(packOutput, BotJS.ID, "en_us") {
    companion object {
        val <R, T : R> DeferredHolder<R, T>.English
            get() = id.path.split('_').joinToString(" ") { segment ->
                segment.replaceFirstChar {
                    it.uppercase()
                }
            }

        val entries = mapOf(
            "item_group.botjs" to "BotJS",
            "menu.botjs.firmware_programmer.title" to "Firmware Programmer",
            "menu.botjs.firmware_programmer.script_here" to "Script Here...",
            "menu.botjs.firmware_programmer.flash.nothing_to_flash" to "No MCU found in MCU slot!",
            "menu.botjs.firmware_programmer.flash.error" to "Compile error: %1\$s",
            "menu.botjs.firmware_programmer.flash.success" to "Flush success",
            "botjs.networking.failed" to "Network failed due to: %1\$s",
        )
    }

    override fun addTranslations() {
        Items.REGISTRY.entries.forEach {
            val item = it.get()
            val name = it.English
            if (item is BlockItem) {
                addBlock({ item.block }, name)
            } else {
                addItem({ item }, name)
            }
        }
        Entities.REGISTRY.entries.forEach {
            val entity = it.get()
            val name = it.English
            addEntityType({ entity }, name)
        }
        entries.forEach { (key, value) ->
            add(key, value)
        }
    }
}