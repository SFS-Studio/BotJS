package com.sifsstudio.botjs.runtime

import com.sifsstudio.botjs.entity.BotEntity
import com.sifsstudio.botjs.runtime.module.BotModule
import com.sifsstudio.botjs.runtime.module.DUMMY_MODULE
import com.sifsstudio.botjs.runtime.threading.RUNTIME_EXECUTOR
import com.sifsstudio.botjs.util.set
import com.sifsstudio.botjs.util.withContextCatching
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.neoforged.neoforge.common.util.INBTSerializable
import org.mozilla.javascript.*
import java.lang.reflect.Member
import java.util.concurrent.Future
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

//TODO: Storage data stored into an item?
const val STORAGE_SIZE = 1

class BotRuntime : INBTSerializable<CompoundTag>, ScriptableObject() {
    var isRunning = false
        private set
    var script = ""

    private val modules = mutableListOf<BotModule>()
    private var runtimeFuture: Future<*>? = null
    private val internalCOMs = Array(2) { SerialComponent(2, this, it) }
    private val ioConnection = Array(2) { DUMMY_MODULE }
    private val storage = StorageComponent(2, IntArray(STORAGE_SIZE))
    private val syncTickLock = ReentrantLock()
    private val syncTickCondition = syncTickLock.newCondition()

    override fun serializeNBT(provider: HolderLookup.Provider) = CompoundTag().apply {
        this["script"] = script
        this["running"] = isRunning
        this["storage"] = storage.storage
    }

    override fun deserializeNBT(provider: HolderLookup.Provider, nbt: CompoundTag) {
        script = nbt.getString("script")
        isRunning = nbt.getBoolean("running")
        val array = nbt.getIntArray("storage")
        check(array.size == STORAGE_SIZE)
        System.arraycopy(array, 0, storage.storage, 0, STORAGE_SIZE)
    }

    fun launch() {
        runtimeFuture = RUNTIME_EXECUTOR.submit {
            isRunning = true
            InterruptibleContextFactory.init()
            withContextCatching { ctx ->
                val scope = ImporterTopLevel(ctx).apply {
                    //Avoid redundant initialization
                    //ctx.initSafeStandardObjects(this)
                    NativeRegUtils.init(this, false)
                    defineProperty("storage", storage, READONLY or PERMANENT)
                    defineProperty("COM0", internalCOMs[0], READONLY or PERMANENT)
                    defineProperty("COM1", internalCOMs[1], READONLY or PERMANENT)
                    put(
                        "tickSync",
                        this,
                        ExposedFunction("tickSync", SYNC_TICK_METHOD, this@BotRuntime)
                    )
                }
                parentScope = scope
                ctx.evaluateString(scope, script, "bot_script", 0, null)
            }.onFailure {
                when (it) {
                    is WrappedException -> {
                        if (it.wrappedException !is InterruptedException) {
                            it.printStackTrace()
                        }
                    }
                    is Exception -> {
                        it.printStackTrace()
                    }
                }
            }
            isRunning = false
        }
    }

    fun interrupt() = runtimeFuture?.also {
        it.cancel(true)
        runtimeFuture = null
    } == null

    fun stop() {
        isRunning = false
        interrupt()
    }

    fun clearModule() = modules.clear()

    fun installModule(module: BotModule) {
        modules.add(module)
        // TEST ONLY
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
        syncTickLock.withLock {
            syncTickCondition.signal()
        }
    }

    @Suppress("unused")
    fun syncTick() = syncTickLock.withLock {
        syncTickCondition.await()
    }

    override fun getClassName(): String = javaClass.name

}

private val SYNC_TICK_METHOD = BotRuntime::class.java.getMethod("syncTick")

@Suppress("UNUSED")
val DUMMY_RUNTIME = BotRuntime()

class ExposedFunction(name: String, methodOrConstructor: Member, scope: Scriptable) : FunctionObject(
    name,
    methodOrConstructor,
    scope
) {
    override fun call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array<out Any>): Any {
        return super.call(cx, scope, parentScope, args)
    }
}

class InterruptibleContextFactory : ContextFactory() {

    companion object {
        private var initialized = false

        //Double check to avoid redundant initialization
        //Otherwise initGlobal will throw IllegalStateException
        fun init() {
            if (initialized) {
                return
            }
            synchronized(InterruptibleContextFactory) {
                if(initialized) {
                    return
                }
                initGlobal(InterruptibleContextFactory())
                initialized = true
            }
        }
    }

    override fun observeInstructionCount(cx: Context, instructionCount: Int) {
        if (Thread.interrupted()) {
            throw InterruptedException("interruption")
        }
    }

    override fun makeContext(): Context = super.makeContext().apply {
        instructionObserverThreshold = 10000
    }
}
