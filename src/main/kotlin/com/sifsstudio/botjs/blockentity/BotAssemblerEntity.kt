package com.sifsstudio.botjs.blockentity

import com.sifsstudio.botjs.item.BotModuleItem
import com.sifsstudio.botjs.item.McuItem
import com.sifsstudio.botjs.item.StorageItem
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler

class BotAssemblerEntity(pPos: BlockPos, pBlockState: BlockState) :
    BlockEntity(BlockEntities.BOT_ASSEMBLER, pPos, pBlockState) {
    val mcu = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
            return stack.item is McuItem
        }
    }
    val storage = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
            return stack.item is StorageItem
        }
    }
    val components = object : ItemStackHandler(18) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
            return stack.item is BotModuleItem
        }
    }

    override fun saveAdditional(pTag: CompoundTag, pRegistries: HolderLookup.Provider) {
        super.saveAdditional(pTag, pRegistries)
        pTag.put("mcu", mcu.serializeNBT(pRegistries))
        pTag.put("storage", storage.serializeNBT(pRegistries))
        pTag.put("components", components.serializeNBT(pRegistries))
    }

    override fun loadAdditional(pTag: CompoundTag, pRegistries: HolderLookup.Provider) {
        super.loadAdditional(pTag, pRegistries)
        mcu.deserializeNBT(pRegistries, pTag.getCompound("mcu"))
        storage.deserializeNBT(pRegistries, pTag.getCompound("storage"))
        components.deserializeNBT(pRegistries, pTag.getCompound("components"))
    }
}