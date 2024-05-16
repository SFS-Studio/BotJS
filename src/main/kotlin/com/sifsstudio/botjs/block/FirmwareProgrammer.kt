package com.sifsstudio.botjs.block

import com.mojang.serialization.MapCodec
import com.sifsstudio.botjs.blockentity.FirmwareProgrammerEntity
import com.sifsstudio.botjs.inventory.FirmwareProgrammerMenu
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class FirmwareProgrammer : BaseEntityBlock(Properties.of().noOcclusion().sound(SoundType.METAL)) {
    companion object {
        private val CODEC = simpleCodec {
            FirmwareProgrammer()
        }
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity =
        FirmwareProgrammerEntity(pPos, pState)

    @Deprecated("")
    override fun getMenuProvider(pState: BlockState, pLevel: Level, pPos: BlockPos): MenuProvider =
        SimpleMenuProvider({ pContainerId, pPlayerInventory, _ ->
            val blockEntity = pLevel.getBlockEntity(pPos)!! as FirmwareProgrammerEntity
            FirmwareProgrammerMenu(
                pContainerId,
                pPlayerInventory,
                pPos,
                blockEntity.script,
                blockEntity.mcuIn,
                blockEntity.mcuOut
            )
        }, Component.translatable("menu.botjs.firmware_programmer.title"))

    // Passed from useItemOn
    @Deprecated("")
    override fun useWithoutItem(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pPlayer: Player,
        pHit: BlockHitResult
    ): InteractionResult {
        if (!pLevel.isClientSide && pPlayer is ServerPlayer) {
            pState.getMenuProvider(pLevel, pPos)?.let { menu ->
                pPlayer.openMenu(menu) {
                    it.writeBlockPos(pPos)
                    pLevel.getBlockEntity(pPos)?.let { blockEntity ->
                        if (blockEntity is FirmwareProgrammerEntity) {
                            it.writeUtf(blockEntity.script)
                        }
                    }
                }
            }
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide)
    }
}