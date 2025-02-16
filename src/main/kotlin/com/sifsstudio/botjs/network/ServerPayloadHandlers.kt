package com.sifsstudio.botjs.network

import com.sifsstudio.botjs.blockentity.FirmwareProgrammerBlockEntity
import com.sifsstudio.botjs.item.Items
import com.sifsstudio.botjs.item.component.DataComponents
import com.sifsstudio.botjs.util.isItem
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.network.handling.IPayloadContext

object ServerPayloadHandlers {
    fun handleFlashMCU(payload: FlashMCU, context: IPayloadContext) {
        context.enqueueWork {
            val level = context.player().level()
            (level.getBlockEntity(payload.pos) as? FirmwareProgrammerBlockEntity)?.let {
                if (it.currentSession == payload.session) {
                    it.flash()
                }
            }
        }.exceptionally {
            context.disconnect(Component.translatable("botjs.networking.failed", it.message))
            null
        }
    }

    fun handleSyncScript(payload: SyncScript, context: IPayloadContext) {
        context.enqueueWork {
            val level = context.player().level()
            val be = level.getBlockEntity(payload.pos) as? FirmwareProgrammerBlockEntity
            val scriptStack = be?.apply {
                if (this.currentSession != payload.session) {
                    return@enqueueWork
                }
            }?.script?.extractItem(0, 1, false) ?: ItemStack.EMPTY
            if (scriptStack isItem Items.SCRIPT) {
                scriptStack.set(DataComponents.SCRIPT, payload.script)
                be?.script?.insertItem(0, scriptStack, false)
                be?.syncChange()
            }
        }.exceptionally {
            context.disconnect(Component.translatable("botjs.networking.failed", it.message))
            null
        }
    }
}