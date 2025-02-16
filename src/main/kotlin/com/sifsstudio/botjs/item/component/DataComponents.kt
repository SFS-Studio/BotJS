package com.sifsstudio.botjs.item.component

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.sifsstudio.botjs.BotJS
import io.netty.buffer.ByteBuf
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.nio.ByteBuffer

data class FirmwareComponent(
    val scriptName: String,
    val byteCode: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FirmwareComponent

        if (scriptName != other.scriptName) return false
        if (!byteCode.contentEquals(other.byteCode)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scriptName.hashCode()
        result = 31 * result + byteCode.contentHashCode()
        return result
    }

    companion object {
        val CODEC: Codec<FirmwareComponent> = RecordCodecBuilder.create {
            it.group(
                Codec.STRING.fieldOf("scriptName").forGetter(FirmwareComponent::scriptName),
                DataComponents.BYTE_ARRAY_CODEC.fieldOf("byteCode").forGetter(FirmwareComponent::byteCode)
            ).apply(it, ::FirmwareComponent)
        }

        val STREAM_CODEC: StreamCodec<ByteBuf, FirmwareComponent> = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FirmwareComponent::scriptName,
            ByteBufCodecs.BYTE_ARRAY, FirmwareComponent::byteCode,
            ::FirmwareComponent
        )
    }
}

object DataComponents {
    val REGISTRY: DeferredRegister.DataComponents = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, BotJS.ID)

    val SCRIPT: DataComponentType<String> by REGISTRY.registerComponentType("script") {
        it.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
    }

    val FIRMWARE: DataComponentType<FirmwareComponent> by REGISTRY.registerComponentType("firmware") {
        it.persistent(FirmwareComponent.CODEC)
            .networkSynchronized(FirmwareComponent.STREAM_CODEC)
    }

    val BYTE_ARRAY_CODEC = object : PrimitiveCodec<ByteArray> {
        override fun <T : Any?> read(ops: DynamicOps<T>, input: T): DataResult<ByteArray> =
            ops.getByteBuffer(input).map(ByteBuffer::array)

        override fun <T : Any?> write(ops: DynamicOps<T>, value: ByteArray): T =
            ops.createByteList(ByteBuffer.wrap(value))
    }
}