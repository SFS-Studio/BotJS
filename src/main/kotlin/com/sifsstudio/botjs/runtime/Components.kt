package com.sifsstudio.botjs.runtime

import org.mozilla.javascript.ScriptRuntime
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.UniqueTag

abstract class IOProxy(size: Int): Scriptable {
    private var prototype: Scriptable? = null
    private var parent: Scriptable? = null

    override fun getClassName() = "IOProxy"
    override fun get(name: String?, start: Scriptable?): Any =
        when (name) {
            "bufferCtrl" -> bufferCtrl.read()
            "bufferDst" -> bufferDst
            "buffers" -> buffers
            else -> UniqueTag.NOT_FOUND
        }

    override fun get(index: Int, start: Scriptable?): Any = UniqueTag.NOT_FOUND

    override fun has(name: String?, start: Scriptable?) =
        when (name) {
            "bufferCtrl", "bufferDst", "buffers" -> true
            else -> false
        }

    override fun has(index: Int, start: Scriptable?) = false

    override fun put(name: String?, start: Scriptable?, value: Any?) {
        if (name == "bufferCtrl") {
            if (value is Number) {
                bufferCtrl.write(value.toInt())
            }
        }
    }

    override fun put(index: Int, start: Scriptable?, value: Any?) = Unit

    override fun delete(name: String?) =
        when (name) {
            "bufferCtrl" -> bufferCtrl.reset()
            "bufferDst" -> bufferDst.reset()
            "buffers" -> buffers.reset()
            else -> Unit
        }

    override fun delete(index: Int) = Unit

    override fun getPrototype() = prototype

    override fun setPrototype(prototype: Scriptable?) {
        this.prototype = prototype
    }

    override fun getParentScope() = parent

    override fun setParentScope(parent: Scriptable?) {
        this.parent = parent
    }

    override fun getIds() = emptyArray<Any>()

    override fun getDefaultValue(hint: Class<*>?) = "[object IOProxy]"

    override fun hasInstance(instance: Scriptable) = ScriptRuntime.jsDelegatesTo(instance, this)

    abstract fun register(reg: Int): Register?

    protected val bufferCtrl: Register
    protected val bufferDst: RegisterArray
    protected val buffers: RegisterArray

    init {
        check(size in 2..16 && size and 1 == 0)
        bufferCtrl = IntRegister(0, Register.RWFlag.ReadWrite)
        bufferDst = RegisterArray(Array(size) {
            IntRegister(0, Register.RWFlag.ReadWrite)
        })
        buffers = RegisterArray(Array(size) {
            IntRegister(0, Register.RWFlag.ReadWrite)
        })
    }

    /*
     * From the least digit of bufferCtrl, every (2i,2i+1) digits
     * decides whether to interact with the register of COM[id]
     * located by bufferDst[i]
     * The least bit controls enable/disable
     * The most bit controls read/write mode, so it reads into
     * buffers[i] or copies the data from buffers[i] into dst.
     */
    fun tick() {
        for (i in 0..1) {
            val enBitMask = 1 shl (i shl 1)
            if (bufferCtrl.content and enBitMask != enBitMask) {
                continue
            }
            val register = register(bufferDst[i].content) ?: continue
            val rwBitMask = 1 shl ((i shl 1) + 1)
            when (bufferCtrl.content and rwBitMask) {
                0 -> {
                    buffers[i].content = register.read()
                }

                rwBitMask -> {
                    register.write(buffers[i].content)
                }
            }
        }
    }

}

class SerialComponent(size: Int, private val runtime: BotRuntime, private val id: Int) : IOProxy(size) {
    override fun register(reg: Int) = runtime
        .comConnectedModule(id)
        ?.register(reg)
}

class StorageComponent(size: Int, val storage: IntArray): IOProxy(size) {
    private val registers = ArrayRefRegister.fromArray(storage, 0, Register.RWFlag.ReadWrite)
    override fun register(reg: Int) = registers[reg]
}