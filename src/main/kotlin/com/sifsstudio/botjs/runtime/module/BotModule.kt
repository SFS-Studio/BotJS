package com.sifsstudio.botjs.runtime.module

import com.sifsstudio.botjs.entity.BotEntity

class Register(private val resetValue: Int, private val rwFlag: RWFlag) {
    companion object {
        const val FAIL_DEFAULT = -1
    }

    internal var content = resetValue

    enum class RWFlag {
        ReadOnly,
        WriteOnly,
        ReadWrite,
    }

    fun write(data: Int) {
        when (rwFlag) {
            RWFlag.ReadWrite, RWFlag.WriteOnly -> content = data
            else -> Unit
        }
    }

    fun read() =
        when (rwFlag) {
            RWFlag.ReadOnly, RWFlag.ReadWrite -> content
            else -> FAIL_DEFAULT
        }

    fun reset() {
        content = resetValue
    }
}

interface BotModule {
    companion object {
        val DUMMY_REGISTER = Register(0, Register.RWFlag.ReadOnly)
    }

    fun register(address: Int): Register
    fun tick(bot: BotEntity)
}