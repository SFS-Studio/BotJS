package com.sifsstudio.botjs.capability

import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.blockentity.BlockEntities
import com.sifsstudio.botjs.entity.Entities
import net.minecraft.core.Direction
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent

@Suppress("unused")
@EventBusSubscriber(modid = BotJS.ID, bus = EventBusSubscriber.Bus.MOD)
object CapabilityRegistry {
    @SubscribeEvent
    fun registerCapabilities(event: RegisterCapabilitiesEvent) {
        event.registerEntity(
            Capabilities.ItemHandler.ENTITY_AUTOMATION,
            Entities.BOT
        ) { entity, _ ->
            entity.modules
        }
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            BlockEntities.FIRMWARE_PROGRAMMER
        ) { blockEntity, direction ->
            when (direction) {
                Direction.UP -> blockEntity.mcuIn
                Direction.DOWN -> blockEntity.mcuOut
                else -> null
            }
        }
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            BlockEntities.BOT_ASSEMBLER
        ) { blockEntity, direction ->
            when (direction) {
                Direction.UP -> blockEntity.mcu
                Direction.EAST -> blockEntity.storage
                Direction.NORTH -> blockEntity.components
                else -> null
            }
        }
    }
}