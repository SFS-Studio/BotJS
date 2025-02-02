package com.sifsstudio.botjs.block

import com.mojang.serialization.MapCodec
import com.sifsstudio.botjs.blockentity.FirmwareProgrammerBlockEntity
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
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class FirmwareProgrammer(properties: Properties) : BaseEntityBlock(properties) {
    companion object {
        private val CODEC = simpleCodec(::FirmwareProgrammer)
    }

    override fun codec(): MapCodec<FirmwareProgrammer> = CODEC

    override fun newBlockEntity(pPos: BlockPos, pState: BlockState) =
        FirmwareProgrammerBlockEntity(pPos, pState)

    @Deprecated("")
    override fun getMenuProvider(pState: BlockState, pLevel: Level, pPos: BlockPos): MenuProvider =
        SimpleMenuProvider({ pContainerId, pPlayerInventory, _ ->
            val blockEntity = pLevel.getBlockEntity(pPos)!! as FirmwareProgrammerBlockEntity
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
                        if (blockEntity is FirmwareProgrammerBlockEntity) {
                            it.writeUtf(blockEntity.script)
                        }
                    }
                }
            }
        }
        return InteractionResult.SUCCESS_SERVER
    }
}