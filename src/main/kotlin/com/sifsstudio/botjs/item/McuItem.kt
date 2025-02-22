package com.sifsstudio.botjs.item

import net.minecraft.world.item.Item

sealed class PinFunction {
    class COM(val comId: UShort) : PinFunction()
    data object GPIO : PinFunction()
}

abstract class McuItem(properties: Properties) : Item(properties) {
    abstract val chipCode: String
    abstract val pins: UShort
    abstract val serials: UShort
    abstract val description: String
    abstract fun pinFunction(pin: UShort): PinFunction

    companion object {
        fun of(
            properties: Properties,
            pins: UShort,
            serials: UShort,
            chipCode: String,
            description: String,
            pinFunction: (UShort) -> PinFunction
        ) = object : McuItem(properties) {
            override val pins = pins
            override val serials = serials
            override val chipCode = chipCode
            override val description = description

            override fun pinFunction(pin: UShort) = pinFunction(pin)
        }
    }
}
