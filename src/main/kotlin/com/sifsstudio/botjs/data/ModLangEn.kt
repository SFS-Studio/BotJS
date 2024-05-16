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
            "menu.botjs.firmware_programmer.flash" to "Flash",
            "menu.botjs.firmware_programmer.flash_result.output_occupied" to "Output slot is occupied!",
            "menu.botjs.firmware_programmer.flash_result.nothing_to_flash" to "No MCU found in input slot!",
            "toast.botjs.flash_failed" to "Failed to flash the MCU!"
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