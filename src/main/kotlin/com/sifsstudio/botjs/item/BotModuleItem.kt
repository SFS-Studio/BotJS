package com.sifsstudio.botjs.item

import com.sifsstudio.botjs.runtime.module.BotModule
import net.minecraft.world.item.Item

abstract class BotModuleItem : Item(Properties().stacksTo(1)) {
    abstract val pins: UShort
    abstract val chipCode: String
    abstract fun module(): BotModule

    companion object {
        fun of(pins: UShort, chipCode: String, moduleFactory: () -> BotModule) = object : BotModuleItem() {
            override val pins = pins
            override val chipCode = chipCode
            override fun module() = moduleFactory()
        }
    }
}