package com.sifsstudio.botjs.runtime.module

import com.sifsstudio.botjs.entity.BotEntity
import com.sifsstudio.botjs.runtime.DUMMY_REGISTER
import com.sifsstudio.botjs.runtime.Register

interface BotModule {
    fun register(address: Int): Register
    fun tick(bot: BotEntity)
}

val DUMMY_MODULE = object: BotModule {
    override fun register(address: Int) = DUMMY_REGISTER
    override fun tick(bot: BotEntity) = Unit
}
