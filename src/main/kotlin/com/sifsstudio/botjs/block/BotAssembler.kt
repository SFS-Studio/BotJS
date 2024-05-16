package com.sifsstudio.botjs.block

import com.mojang.serialization.MapCodec
import com.sifsstudio.botjs.blockentity.BotAssemblerEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState

class BotAssembler : BaseEntityBlock(Properties.of().noOcclusion().sound(SoundType.METAL)) {
    companion object {
        private val CODEC = simpleCodec {
            BotAssembler()
        }
    }

    override fun codec(): MapCodec<BotAssembler> = CODEC

    override fun newBlockEntity(pPos: BlockPos, pState: BlockState) = BotAssemblerEntity(pPos, pState)
}