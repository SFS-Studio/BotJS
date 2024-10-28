package com.sifsstudio.botjs.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

/**
 * Sync data from server to client using ```BlockEntity.getUpdatePacket()```
 *
 * How can you write such shit code Mojang?
 * What the fuck???
 */
abstract class BaseBlockEntity(
    pType: BlockEntityType<*>,
    pPos: BlockPos,
    pBlockState: BlockState,
) : BlockEntity(pType, pPos, pBlockState) {
    abstract val sync: Boolean

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    abstract override fun getUpdateTag(pRegistries: HolderLookup.Provider): CompoundTag

    abstract override fun handleUpdateTag(tag: CompoundTag, lookupProvider: HolderLookup.Provider)

    fun sync() {
        val lvl = level
        if (lvl !is ServerLevel || !sync) {
            return
        }
        lvl.chunkSource.blockChanged(blockPos)
    }

    fun setChangedAndSync() {
        setChanged()
        sync()
    }
}