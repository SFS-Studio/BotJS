package com.sifsstudio.botjs.runtime.threading

import com.sifsstudio.botjs.util.withContextCatching
import net.minecraft.server.MinecraftServer
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import org.mozilla.javascript.Script
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private val BOT_THREAD_ID = AtomicInteger(0)
private val COMPILE_THREAD_ID = AtomicInteger(0)
lateinit var RUNTIME_EXECUTOR: ExecutorService
lateinit var COMPILE_EXECUTOR: ExecutorService
lateinit var SERVER_THREAD: MinecraftServer

fun onServerStarting(event: ServerStartingEvent) {
    RUNTIME_EXECUTOR =
        Executors.newCachedThreadPool {
            Thread.ofVirtual().name("BotJS-BotVThread-" + BOT_THREAD_ID.getAndIncrement()).unstarted(it)
        }
    COMPILE_EXECUTOR =
        Executors.newFixedThreadPool(2) {
            Thread.ofPlatform().name("BotJS-CompileThread-" + COMPILE_THREAD_ID.getAndIncrement()).unstarted(it)
        }
    SERVER_THREAD = event.server
}

@Suppress("UNUSED_PARAMETER")
fun onServerStopped(event: ServerStoppedEvent) {
    RUNTIME_EXECUTOR.awaitTermination(1L, TimeUnit.SECONDS)
    COMPILE_EXECUTOR.awaitTermination(1L, TimeUnit.SECONDS)
}

class CompileScope {
    lateinit var fail: (Throwable) -> Unit
    lateinit var succeed: (Script) -> Unit
    fun onFailure(closure: (Throwable) -> Unit) {
        fail = closure
    }
    fun onSuccess(closure: (Script) -> Unit) {
        succeed = closure
    }
}

/**
 * All callbacks are invoked on the server thread.
 */
inline fun tryCompile(script: String, initialize: CompileScope.() -> Unit) {
    val scope = CompileScope()
    scope.initialize()
    COMPILE_EXECUTOR.submit {
        val res = withContextCatching { ctx ->
            ctx.compileString(script, "bot_script", 0, null)
        }
        SERVER_THREAD.execute {
            res.fold(
                onSuccess = scope.succeed,
                onFailure = scope.fail,
            )
        }
    }
}
