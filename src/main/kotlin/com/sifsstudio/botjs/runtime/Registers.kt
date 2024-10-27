package com.sifsstudio.botjs.runtime

val DUMMY_REGISTER = IntRegister(0, Register.RWFlag.Read)
const val REGISTER_READ_FAILED = -1

abstract class Register(private val default: Int, protected val flag: RWFlag) {
    abstract var content: Int

    fun write(data: Int) {
        if(flag.writable) {
            content = data
        }
    }

    fun read() =
        if(flag.readable) {
            content
        } else REGISTER_READ_FAILED

    fun reset() {
        content = default
    }

    enum class RWFlag(val readable: Boolean, val writable: Boolean) {
        Read(true, false),
        Write(false, true),
        ReadWrite(true, true),
    }
}

class IntRegister(default: Int, flag: RWFlag): Register(default, flag) {
    override var content = default
}

class ArrayRefRegister(
    default: Int,
    flag: RWFlag,
    private val source: IntArray,
    private val index: Int,
): Register(default, flag) {
    override var content: Int
        get() = source[index]
        set(value) { source[index] = value }

    companion object {
        fun fromArray(array: IntArray, default: Int, flag: RWFlag)
            = Array(array.size) { ArrayRefRegister(default, flag, array, it) }
    }
}
