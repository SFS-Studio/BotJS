package com.sifsstudio.botjs.runtime

import com.dokar.quickjs.binding.ObjectBindingScope

class RegisterArray(private val registers: Array<Register>) {
    operator fun get(index: Int): Register = registers[index]

    fun defineRegisterArray(name: String, objectBindingScope: ObjectBindingScope) = objectBindingScope.define(name) {
        for (i in registers.indices) {
            property(i.toString()) {
                getter {
                    registers[i].read()
                }
                setter {
                    registers[i].write(it)
                }
            }
        }
    }
}
