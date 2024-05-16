package com.sifsstudio.botjs.block

import com.sifsstudio.botjs.BotJS
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object Blocks {
    val REGISTRY: DeferredRegister<Block> = DeferredRegister.create(Registries.BLOCK, BotJS.ID)

    val FIRMWARE_PROGRAMMER: FirmwareProgrammer by REGISTRY.register("firmware_programmer", ::FirmwareProgrammer)
    val BOT_ASSEMBLER: BotAssembler by REGISTRY.register("bot_assembler", ::BotAssembler)
}