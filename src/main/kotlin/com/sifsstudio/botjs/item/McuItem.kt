package com.sifsstudio.botjs.item

import com.mojang.serialization.Codec
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.Item

sealed class PinFunction {
    class COM(val comId: UShort) : PinFunction()
    data object GPIO : PinFunction()
}

abstract class McuItem : Item(Properties().stacksTo(1)) {
    abstract val chipCode: String
    abstract val pins: UShort
    abstract val serials: UShort
    abstract val description: String
    abstract fun pinFunction(pin: UShort): PinFunction

    companion object {
        val SCRIPT_COMPONENT: DataComponentType<String> =
            DataComponentType.builder<String>().persistent(Codec.STRING).build()

        fun of(
            pins: UShort,
            serials: UShort,
            chipCode: String,
            description: String,
            pinFunction: (UShort) -> PinFunction
        ) = object : McuItem() {
            override val pins = pins
            override val serials = serials
            override val chipCode = chipCode
            override val description = description

            override fun pinFunction(pin: UShort) = pinFunction(pin)
        }
    }
}
