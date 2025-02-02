package com.sifsstudio.botjs.network

import com.sifsstudio.botjs.BotJS
import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent

data class FirmwareProgrammerAction(val flasherPos: BlockPos, val script: String, val flash: Boolean) :
    CustomPacketPayload {
    companion object {
        val TYPE =
            CustomPacketPayload.Type<FirmwareProgrammerAction>(ResourceLocation.fromNamespaceAndPath(BotJS.ID, "firmware_programmer_action"))
        val CODEC: StreamCodec<ByteBuf, FirmwareProgrammerAction> = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            FirmwareProgrammerAction::flasherPos,
            ByteBufCodecs.STRING_UTF8,
            FirmwareProgrammerAction::script,
            ByteBufCodecs.BOOL,
            FirmwareProgrammerAction::flash,
            ::FirmwareProgrammerAction
        )
    }

    override fun type() = TYPE
}

data class FlashResult(val messageKey: String) : CustomPacketPayload {
    companion object {
        val TYPE = CustomPacketPayload.Type<FlashResult>(ResourceLocation.fromNamespaceAndPath(BotJS.ID, "flash_result"))
        val CODEC: StreamCodec<ByteBuf, FlashResult> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            FlashResult::messageKey,
            ::FlashResult
        )
    }

    override fun type() = TYPE
}

@Suppress("unused")
@EventBusSubscriber(modid = BotJS.ID, bus = EventBusSubscriber.Bus.MOD)
object NetworkRegistry {
    @SubscribeEvent
    fun register(event: RegisterPayloadHandlersEvent) {
        val registrar = event.registrar(BotJS.ID)
        registrar.playToServer(
            FirmwareProgrammerAction.TYPE,
            FirmwareProgrammerAction.CODEC,
            ServerPayloadHandlers::handleFirmwareProgrammerAction
        )
        registrar.playToClient(FlashResult.TYPE, FlashResult.CODEC, ClientPayloadHandlers::handleFlashResult)
    }
}