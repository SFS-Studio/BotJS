package com.sifsstudio.botjs.network

import com.sifsstudio.botjs.blockentity.FirmwareProgrammerBlockEntity
import net.minecraft.network.chat.Component
import net.neoforged.neoforge.network.handling.IPayloadContext

object ServerPayloadHandlers {
    fun handleFirmwareProgrammerAction(payload: FirmwareProgrammerAction, context: IPayloadContext) {
        context.enqueueWork {
            val level = context.player().level()
            (level.getBlockEntity(payload.flasherPos) as? FirmwareProgrammerBlockEntity) ?.let { be ->
                be.script = payload.script
                if(payload.flash) {
                    be.flash()
                }
                //We already setChangedAndSync() so don't do it again here
                //be.setChanged()
            }
        }.exceptionally {
            context.disconnect(Component.translatable("botjs.networking.failed", it.message))
            null
        }
    }
}