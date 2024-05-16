package com.sifsstudio.botjs.blockentity

import com.sifsstudio.botjs.item.McuItem
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler

class FirmwareProgrammerEntity(pPos: BlockPos, pBlockState: BlockState) :
    BlockEntity(BlockEntities.FIRMWARE_PROGRAMMER, pPos, pBlockState) {

    val mcuIn = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
            return stack.item is McuItem
        }
    }
    val mcuOut = ItemStackHandler(1)
    var script = ""

    override fun saveAdditional(pTag: CompoundTag, pRegistries: HolderLookup.Provider) {
        super.saveAdditional(pTag, pRegistries)
        pTag.putString("script", script)
        pTag.put("mcuIn", mcuIn.serializeNBT(pRegistries))
        pTag.put("mcuOut", mcuOut.serializeNBT(pRegistries))
    }

    override fun loadAdditional(pTag: CompoundTag, pRegistries: HolderLookup.Provider) {
        super.loadAdditional(pTag, pRegistries)
        script = pTag.getString("script")
        mcuIn.deserializeNBT(pRegistries, pTag.getCompound("mcuIn"))
        mcuOut.deserializeNBT(pRegistries, pTag.getCompound("mcuOut"))
    }
}