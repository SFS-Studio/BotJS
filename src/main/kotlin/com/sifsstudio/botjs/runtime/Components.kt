package com.sifsstudio.botjs.runtime

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.define

class SerialComponent(size: Int, private val runtime: BotRuntime, private val id: Int) {
    private val bufferCtrl: Register
    private val bufferDst: RegisterArray
    private val buffers: RegisterArray

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

    fun defineSerialComponent(quickJs: QuickJs) = quickJs.apply {
        define("COM$id") {
            property("ctrl") {
                getter {
                    bufferCtrl.read()
                }
                setter {
                    bufferCtrl.write(it)
                }
            }
            bufferDst.defineRegisterArray("dst", this)
            buffers.defineRegisterArray("buffers", this)
        }
    }

    fun register(address: Int) = runtime.comConnectedModule(id)?.register(address)

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
