package com.sifsstudio.botjs.runtime.threading

import com.dokar.quickjs.QuickJsException
import com.dokar.quickjs.quickJs
import com.mojang.datafixers.util.Either
import kotlinx.coroutines.*
import net.minecraft.server.MinecraftServer
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

private val BOT_THREAD_ID = AtomicInteger(0)
private val COMPILE_THREAD_ID = AtomicInteger(0)
private lateinit var RUNTIME_DISPATCHER: ExecutorCoroutineDispatcher
private lateinit var COMPILE_DISPATCHER: ExecutorCoroutineDispatcher
lateinit var RUNTIME_SCOPE: CoroutineScope
lateinit var COMPILE_SCOPE: CoroutineScope
lateinit var SERVER_THREAD: MinecraftServer

fun onServerStarting(event: ServerStartingEvent) {
    RUNTIME_DISPATCHER = Executors.newCachedThreadPool {
        Thread.ofVirtual().name("BotJS-BotVThread-" + BOT_THREAD_ID.getAndIncrement()).unstarted(it)
    }.asCoroutineDispatcher()
    COMPILE_DISPATCHER = Executors.newFixedThreadPool(2) {
        Thread.ofPlatform().name("BotJS-CompileThread-" + COMPILE_THREAD_ID.getAndIncrement()).unstarted(it)
    }.asCoroutineDispatcher()
    RUNTIME_SCOPE = CoroutineScope(RUNTIME_DISPATCHER)
    COMPILE_SCOPE = CoroutineScope(COMPILE_DISPATCHER)

    SERVER_THREAD = event.server
}

@Suppress("UNUSED_PARAMETER")
fun onServerStopped(event: ServerStoppedEvent) {
    RUNTIME_SCOPE.cancel()
    RUNTIME_DISPATCHER.close()
    COMPILE_SCOPE.cancel()
    COMPILE_DISPATCHER.close()
}

/**
 * Asynchronously compile with quick-js
 **/
fun tryCompile(script: String) = COMPILE_SCOPE.async {
    quickJs {
        try {
            val byteCode = compile(script)
            return@async Either.left(byteCode)
        } catch (err: QuickJsException) {
            return@async Either.right(err)
        }
    }
}
