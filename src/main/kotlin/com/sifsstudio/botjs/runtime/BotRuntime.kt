package com.sifsstudio.botjs.runtime

import com.sifsstudio.botjs.entity.BotEntity
import com.sifsstudio.botjs.runtime.module.BotModule
import com.sifsstudio.botjs.util.withContext
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.neoforged.neoforge.common.util.INBTSerializable
import net.neoforged.neoforge.event.server.ServerStartingEvent
import org.jetbrains.annotations.UnknownNullability
import org.mozilla.javascript.*
import java.lang.reflect.Member
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class BotRuntime : INBTSerializable<CompoundTag>, ScriptableObject() {
    var isRunning = false
        private set
    var script = ""
    private val modules: MutableList<BotModule> = mutableListOf()
    private var runtimeFuture: Future<*>? = null
    private val internalCOMs = Array(2) {
        SerialComponent(this, it)
    }
    private val ioConnection: Array<BotModule?> = Array(2) { null }
    private val syncTickLock = ReentrantLock()
    private val syncTickCondition = syncTickLock.newCondition()

    override fun serializeNBT(provider: HolderLookup.Provider): @UnknownNullability CompoundTag = CompoundTag().apply {
        putString("script", script)
        putBoolean("running", isRunning)
    }

    override fun deserializeNBT(provider: HolderLookup.Provider, nbt: CompoundTag) {
        script = nbt.getString("script")
        isRunning = nbt.getBoolean("running")
    }

    fun launch() {
        runtimeFuture = EXECUTOR.submit {
            isRunning = true
            try {
                InterruptableContextFactory.init()
                withContext { ctx ->
                    val scope = ImporterTopLevel(ctx).apply {
                        ctx.initStandardObjects(this)
                        NativeRegUtils.init(this, false)
                        defineProperty("COM0", internalCOMs[0], READONLY.or(PERMANENT))
                        defineProperty("COM1", internalCOMs[1], READONLY.or(PERMANENT))
                        put(
                            "tickSync",
                            this,
                            ExposedFunction("tickSync", SYNC_TICK_METHOD, this@BotRuntime)
                        )
                    }
                    this.parentScope = scope
                    ctx.evaluateString(scope, script, "bot_script", 0, null)
                }
            } catch (exception: WrappedException) {
                if (exception.wrappedException is InterruptedException) {
                    return@submit
                }
                exception.printStackTrace()
            } catch (err: Error) {
                return@submit
            } catch (err: InterruptedException) {
                return@submit
            } catch (exception: Exception) {
                exception.printStackTrace()
            } finally {
                isRunning = false
            }
        }
    }

    fun interrupt() {
        runtimeFuture?.cancel(true)
        runtimeFuture = null
    }

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

    fun tick(bot: BotEntity) {
        if (isRunning) {
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
    }

    @Suppress("unused")
    fun syncTick() = syncTickLock.withLock {
        syncTickCondition.await()
    }

    companion object {
        private val SYNC_TICK_METHOD = BotRuntime::class.java.getMethod("syncTick")
        private val BOT_THREAD_ID = AtomicInteger(0)
        lateinit var EXECUTOR: ExecutorService
        fun onServerStarting(@Suppress("UNUSED_PARAMETER") event: ServerStartingEvent) {
            EXECUTOR =
                Executors.newCachedThreadPool {
                    Thread.ofVirtual().name("BotJS-BotThread-" + BOT_THREAD_ID.getAndIncrement()).unstarted(it)
                }
        }

        val DUMMY_RUNTIME = BotRuntime()
    }

    override fun getClassName(): String = javaClass.name
}

class ExposedFunction(name: String, methodOrConstructor: Member, scope: Scriptable) : FunctionObject(
    name,
    methodOrConstructor,
    scope
) {
    override fun call(cx: Context, scope: Scriptable, thisObj: Scriptable, args: Array<out Any>): Any {
        return super.call(cx, scope, parentScope, args)
    }
}

class InterruptableContextFactory : ContextFactory() {
    companion object {
        private var initialized = false
        fun init() {
            if (!initialized) {
                initGlobal(InterruptableContextFactory())
                initialized = true
            }
        }
    }

    override fun observeInstructionCount(cx: Context, instructionCount: Int) {
        if (Thread.currentThread().isInterrupted) {
            throw Error("interruption")
        }
    }

    override fun makeContext(): Context = super.makeContext().apply {
        instructionObserverThreshold = 10000
    }
}
