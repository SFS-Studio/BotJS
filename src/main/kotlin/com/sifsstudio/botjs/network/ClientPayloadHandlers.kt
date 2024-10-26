package com.sifsstudio.botjs.network

import com.sifsstudio.botjs.client.gui.screen.inventory.FirmwareProgrammerScreen
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.network.handling.IPayloadContext

object ClientPayloadHandlers {
    fun handleFlashResult(payload: FlashResult, context: IPayloadContext) {
        context.enqueueWork {
            (Minecraft.getInstance().screen as? FirmwareProgrammerScreen)?.run {
                flashResult = Component.translatable(payload.messageKey)
            }
        }.exceptionally {
            context.disconnect(Component.translatable("botjs.networking.failed", it.message))
            null
        }
    }
}