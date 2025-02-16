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
import java.util.UUID

data class FlashMCU(val session: UUID, val pos: BlockPos) :
    CustomPacketPayload {
    companion object {
        val TYPE =
            CustomPacketPayload.Type<FlashMCU>(ResourceLocation.fromNamespaceAndPath(BotJS.ID, "flash_mcu"))
        val CODEC: StreamCodec<ByteBuf, FlashMCU> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            FlashMCU::session,
            BlockPos.STREAM_CODEC,
            FlashMCU::pos,
            ::FlashMCU
        )
    }

    override fun type() = TYPE
}

data class SyncScript(val session: UUID, val script: String, val pos: BlockPos) : CustomPacketPayload {
    companion object {
        val TYPE = CustomPacketPayload.Type<SyncScript>(ResourceLocation.fromNamespaceAndPath(BotJS.ID, "sync_script"))
        val CODEC: StreamCodec<ByteBuf, SyncScript> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
            SyncScript::session,
            ByteBufCodecs.STRING_UTF8,
            SyncScript::script,
            BlockPos.STREAM_CODEC,
            SyncScript::pos,
            ::SyncScript
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
            FlashMCU.TYPE,
            FlashMCU.CODEC,
            ServerPayloadHandlers::handleFlashMCU
        )
        registrar.playToServer(SyncScript.TYPE, SyncScript.CODEC, ServerPayloadHandlers::handleSyncScript)
    }
}