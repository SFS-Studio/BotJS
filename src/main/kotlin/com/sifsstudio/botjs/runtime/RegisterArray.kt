package com.sifsstudio.botjs.runtime

import org.mozilla.javascript.ScriptRuntime
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.UniqueTag

class RegisterArray(private val registers: Array<Register>): Scriptable {
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

    override fun delete(index: Int) = registers[index].reset()

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

    fun reset() = registers.forEach { it.reset() }
}