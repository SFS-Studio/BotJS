package com.sifsstudio.botjs.runtime

import org.mozilla.javascript.*

class NativeRegUtils : IdScriptableObject() {
    companion object {
        const val REG_UTILS_TAG = "RegUtils"
        const val ID_RAW_BITS_TO_FLOAT = 1
        const val ID_FLOAT_TO_RAW_BITS = 2
        private const val LAST_METHOD_ID = ID_FLOAT_TO_RAW_BITS
        private const val MAX_ID = LAST_METHOD_ID

        fun init(scope: Scriptable, sealed: Boolean) {
            val obj = NativeRegUtils()
            obj.activatePrototypeMap(MAX_ID)
            obj.prototype = getObjectPrototype(scope)
            obj.parentScope = scope
            if (sealed) {
                obj.sealObject()
            }
            ScriptableObject.defineProperty(scope, "RegUtils", obj, ScriptableObject.DONTENUM)
        }
    }

    override fun getClassName() = "RegUtils"

    override fun initPrototypeId(id: Int) {
        val name: String
        val arity: Int
        when (id) {
            ID_RAW_BITS_TO_FLOAT -> {
                name = "rawBitsToFloat"
                arity = 1
            }

            ID_FLOAT_TO_RAW_BITS -> {
                name = "floatToRawBits"
                arity = 1
            }

            else -> throw IllegalStateException(id.toString())
        }
        initPrototypeMethod(REG_UTILS_TAG, id, name, arity)
    }

    override fun execIdCall(
        f: IdFunctionObject,
        cx: Context,
        scope: Scriptable,
        thisObj: Scriptable,
        args: Array<out Any>
    ): Any {
        if (!f.hasTag(REG_UTILS_TAG)) {
            return super.execIdCall(f, cx, scope, thisObj, args)
        }
        when (val methodId = f.methodId()) {
            ID_RAW_BITS_TO_FLOAT -> {
                val rawBits = ScriptRuntime.toInt32(args, 0)
                return ScriptRuntime.wrapNumber(Float.fromBits(rawBits).toDouble())
            }

            ID_FLOAT_TO_RAW_BITS -> {
                val float = ScriptRuntime.toNumber(args, 0)
                return ScriptRuntime.wrapInt(float.toFloat().toRawBits())
            }

            else -> throw IllegalStateException(methodId.toString())
        }
    }

    override fun findPrototypeId(name: String) = when (name) {
        "rawBitsToFloat" -> ID_RAW_BITS_TO_FLOAT
        "floatToRawBits" -> ID_FLOAT_TO_RAW_BITS
        else -> 0
    }
}