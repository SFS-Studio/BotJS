package com.sifsstudio.botjs.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

/**
 * Sync data from server to client using ```BlockEntity.getUpdatePacket()```
 */
abstract class BaseBlockEntity(
    pType: BlockEntityType<*>,
    pPos: BlockPos,
    pBlockState: BlockState,
) : BlockEntity(pType, pPos, pBlockState) {
    abstract val sync: Boolean

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(pRegistries: HolderLookup.Provider): CompoundTag = super.getUpdateTag(pRegistries).apply {
        saveAdditional(this, pRegistries)
    }

    fun syncChange() {
        setChanged()
        if (sync) {
            this.level?.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_NEIGHBORS or Block.UPDATE_CLIENTS)
        }
    }
}