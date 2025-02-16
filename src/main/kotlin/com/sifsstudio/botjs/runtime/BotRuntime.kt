package com.sifsstudio.botjs.runtime

import com.dokar.quickjs.QuickJs
import com.dokar.quickjs.binding.asyncFunction
import com.dokar.quickjs.binding.define
import com.dokar.quickjs.quickJs
import com.sifsstudio.botjs.entity.BotEntity
import com.sifsstudio.botjs.runtime.module.BotModule
import com.sifsstudio.botjs.runtime.module.DUMMY_MODULE
import com.sifsstudio.botjs.runtime.threading.RUNTIME_SCOPE
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BotRuntime {
    var isRunning = false
        private set
    var script = ""

    private val modules = mutableListOf<BotModule>()
    private val internalCOMs = Array(2) { SerialComponent(2, this, it) }
    private val ioConnection = Array(2) { DUMMY_MODULE }
    private var nextTickContinuation: Continuation<Unit>? = null
    private var runningContext: QuickJs? = null
    private var runningJob: Job? = null

    fun launch(bytecode: ByteArray) {
        isRunning = true
        runningJob = RUNTIME_SCOPE.launch {
            runningContext = quickJs {
                internalCOMs[0].defineSerialComponent(this)
                internalCOMs[1].defineSerialComponent(this)
                define("NativeRegUtils") {
                    function("rawBitsToFloat") { args ->
                        if (args.size == 1 && args[0] is Number) {
                            return@function Float.fromBits((args[0] as Number).toInt())
                        }
                        return@function 0.0
                    }
                    function("floatToRawBits") { args ->
                        if (args.size == 1 && args[0] is Number) {
                            (args[0] as Number).toFloat().toRawBits()
                        }
                    }
                }
                asyncFunction("nextTick") {
                    suspendCoroutine { cont ->
                        if (nextTickContinuation != null) {
                            nextTickContinuation = cont
                        }
                    }
                }
                evaluate<Any?>(bytecode)
                this
            }
            runningContext = null
            runningJob = null
            isRunning = false
        }
    }

    fun interrupt(): Boolean {
        var actualInterruption = false
        if (runningContext?.isClosed == false) {
            actualInterruption = true
            runningContext?.close()
        }
        runningContext = null
        runningJob?.cancel()
        runningJob = null
        return actualInterruption
    }

    fun stop() {
        isRunning = false
        interrupt()
    }

    fun clearModule() = modules.clear()

    fun installModule(module: BotModule) {
        modules.add(module)
        // TODO: REMOVE THIS, TEST ONLY
        ioConnection[0] = module
        ioConnection[1] = module
    }

    fun comConnectedModule(id: Int) = ioConnection[id]
        .takeIf { it != DUMMY_MODULE }

    fun tick(bot: BotEntity) {
        if (!isRunning) {
            return
        }
        modules.forEach {
            it.tick(bot)
        }
        internalCOMs.forEach {
            it.tick()
        }
        nextTickContinuation?.resume(Unit)
    }
}
