package com.sifsstudio.botjs.block

import com.sifsstudio.botjs.BotJS
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object Blocks {
    val REGISTRY: DeferredRegister.Blocks = DeferredRegister.createBlocks(BotJS.ID)

    val FIRMWARE_PROGRAMMER: FirmwareProgrammer by REGISTRY.registerBlock(
        "firmware_programmer",
        ::FirmwareProgrammer,
        Properties.of()
            .noOcclusion()
            .sound(SoundType.METAL)
    )
    val BOT_ASSEMBLER: BotAssembler by REGISTRY.registerBlock(
        "bot_assembler",
        ::BotAssembler,
        Properties.of().noOcclusion().sound(SoundType.METAL)
    )
}