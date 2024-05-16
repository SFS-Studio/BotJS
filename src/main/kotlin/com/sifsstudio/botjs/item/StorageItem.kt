package com.sifsstudio.botjs.item

import net.minecraft.world.item.Item

abstract class StorageItem : Item(Properties().stacksTo(1)) {
    abstract val chipCode: String
    abstract val volume: Int
}