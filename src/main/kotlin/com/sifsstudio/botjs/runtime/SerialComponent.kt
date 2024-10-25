package com.sifsstudio.botjs.runtime

import com.sifsstudio.botjs.runtime.module.Register
import org.mozilla.javascript.ScriptRuntime
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.UniqueTag

class RegisterArray(private val registers: Array<Register>) : Scriptable {
    private var prototype: Scriptable? = null
    private var parent: Scriptable? = null

    operator fun get(index: Int): Register = registers[index]

    override fun getClassName() = "RegArray"

    override fun get(name: String, start: Scriptable?): Any = UniqueTag.NOT_FOUND

    override fun get(index: Int, start: Scriptable?): Any {
        return if (index < 0 || index >= registers.size) {
            UniqueTag.NOT_FOUND
        } else registers[index].read()
    }

    override fun has(name: String, start: Scriptable?) = false

    override fun has(index: Int, start: Scriptable?) = index >= 0 && index < registers.size

    override fun put(name: String, start: Scriptable?, value: Any?) = Unit

    override fun put(index: Int, start: Scriptable?, value: Any?) {
        if (index >= 0 && index < registers.size && value is Number) {
            registers[index].write(value.toInt())
        }
    }

    override fun delete(name: String) = Unit

    override fun delete(index: Int) = Unit

    override fun getPrototype(): Scriptable? = prototype

    override fun setPrototype(prototype: Scriptable?) {
        this.prototype = prototype
    }

    override fun getParentScope(): Scriptable? = parent

    override fun setParentScope(parent: Scriptable?) {
        this.parent = parent
    }

    override fun getIds(): Array<Any> = Array(0) {}

    override fun getDefaultValue(hint: Class<*>?): Any = "[object RegArray]"

    override fun hasInstance(instance: Scriptable) = ScriptRuntime.jsDelegatesTo(instance, this)

}

class SerialComponent(private val runtime: BotRuntime, private val id: Int) : Scriptable {
    private var prototype: Scriptable? = null
    private var parent: Scriptable? = null

    override fun getClassName() = "COM"
    override fun get(name: String, start: Scriptable?): Any =
        when (name) {
            "bufferCtrl" -> bufferCtrl.read()
            "bufferDst" -> bufferDst
            "buffers" -> buffers
            else -> UniqueTag.NOT_FOUND
        }

    override fun get(index: Int, start: Scriptable?): Any = UniqueTag.NOT_FOUND

    override fun has(name: String, start: Scriptable?): Boolean =
        when (name) {
            "bufferCtrl", "bufferDst", "buffers" -> true
            else -> false
        }

    override fun has(index: Int, start: Scriptable?): Boolean = false

    override fun put(name: String?, start: Scriptable?, value: Any?) {
        if (name == "bufferCtrl") {
            if (value is Number) {
                bufferCtrl.write(value.toInt())
            }
        }
    }

    override fun put(index: Int, start: Scriptable?, value: Any?) = Unit

    override fun delete(name: String?) = Unit

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

    override fun getDefaultValue(hint: Class<*>?) = "[object COM]"

    override fun hasInstance(instance: Scriptable): Boolean {
        var proto = instance.prototype
        while (proto != null) {
            if (proto.equals(this)) {
                return true
            }
            proto = proto.prototype
        }
        return false
    }

    private val bufferCtrl = Register(0, Register.RWFlag.ReadWrite)
    private val bufferDst = RegisterArray(Array(2) {
        Register(0, Register.RWFlag.ReadWrite)
    })
    private val buffers = RegisterArray(Array(2) {
        Register(0, Register.RWFlag.ReadWrite)
    })

    /*
     * From the least digit of bufferCtrl, every (2i,2i+1) digits
     * decides whether to interact with the register of COM[id]
     * located by bufferDst[i]
     * The least bit controls enable/disable
     * The most bit controls read/write mode, so it reads into
     * buffers[i] or copies the data from buffers[i] into dst.
     */
    fun tick() {
        if (runtime == BotRuntime.DUMMY_RUNTIME) {
            return
        }
        runtime.comConnectedModule(id)?.let { module ->
            for (i in 0..1) {
                val enBitMask = 1 shl (i shl 1)
                if (bufferCtrl.content and enBitMask == enBitMask) {
                    val rwBitMask = 1 shl ((i shl 1) + 1)
                    val register = module.register(bufferDst[i].content)
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
    }
}