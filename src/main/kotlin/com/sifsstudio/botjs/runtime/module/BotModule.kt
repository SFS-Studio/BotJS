package com.sifsstudio.botjs.runtime.module

import com.sifsstudio.botjs.entity.BotEntity

class Register(private val default: Int, private val rwFlag: RWFlag) {
    companion object {
        const val FAIL_DEFAULT = -1
    }

    internal var content = default

    enum class RWFlag(val readable: Boolean, val writable: Boolean) {
        ReadOnly(true, false),
        WriteOnly(false, true),
        ReadWrite(true, true),
    }

    fun write(data: Int) {
        if(rwFlag.writable) {
            content = data
        }
    }

    fun read() =
        if(rwFlag.readable) {
            content
        } else FAIL_DEFAULT

    fun reset() {
        content = default
    }
}

interface BotModule {
    companion object {
        val DUMMY_REGISTER = Register(0, Register.RWFlag.ReadOnly)

        val DUMMY_MODULE = object: BotModule {
            override fun register(address: Int) = DUMMY_REGISTER
            override fun tick(bot: BotEntity) = Unit
        }
    }

    fun register(address: Int): Register
    fun tick(bot: BotEntity)
}