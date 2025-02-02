package com.sifsstudio.botjs.blockentity

import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.block.Blocks
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue
import java.util.function.Supplier

object BlockEntities {
    val REGISTRY: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BotJS.ID)

    val FIRMWARE_PROGRAMMER: BlockEntityType<FirmwareProgrammerBlockEntity> by REGISTRY.register(
        "firmware_programmer",
        Supplier {
            BlockEntityType(::FirmwareProgrammerBlockEntity, Blocks.FIRMWARE_PROGRAMMER)
        }
    )

    val BOT_ASSEMBLER: BlockEntityType<BotAssemblerBlockEntity> by REGISTRY.register(
        "bot_assembler",
        Supplier {
            BlockEntityType(::BotAssemblerBlockEntity, Blocks.BOT_ASSEMBLER)
        }
    )
}