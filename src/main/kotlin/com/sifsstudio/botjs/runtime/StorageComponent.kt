package com.sifsstudio.botjs.runtime

import org.mozilla.javascript.ScriptRuntime
import org.mozilla.javascript.Scriptable

class StorageComponent(private val storage: IntArray): Scriptable {
    private var prototype: Scriptable? = null
    private var parent: Scriptable? = null

    override fun getClassName() = "Storage"

    override fun get(name: String, start: Scriptable?): Any {
        TODO("Not yet implemented")
    }

    override fun get(index: Int, start: Scriptable?): Any {
        TODO("Not yet implemented")
    }

    override fun has(name: String?, start: Scriptable?): Boolean {
        TODO("Not yet implemented")
    }

    override fun has(index: Int, start: Scriptable?): Boolean {
        TODO("Not yet implemented")
    }

    override fun put(name: String?, start: Scriptable?, value: Any?) {
        TODO("Not yet implemented")
    }

    override fun put(index: Int, start: Scriptable?, value: Any?) {

    }

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

    override fun getIds() = Array(0) {}

    override fun getDefaultValue(hint: Class<*>?) = "[object Storage]"

    override fun hasInstance(instance: Scriptable) = ScriptRuntime.jsDelegatesTo(instance, this)
}