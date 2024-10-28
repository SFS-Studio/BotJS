package com.sifsstudio.botjs.blockentity

import com.sifsstudio.botjs.inventory.BotAssemblerMenu
import com.sifsstudio.botjs.item.BotModuleItem
import com.sifsstudio.botjs.item.McuItem
import com.sifsstudio.botjs.item.StorageItem
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler

class BotAssemblerBlockEntity(pPos: BlockPos, pBlockState: BlockState) :
    BaseBlockEntity(BlockEntities.BOT_ASSEMBLER, pPos, pBlockState), MenuProvider {
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

    override fun createMenu(pContainerId: Int, pPlayerInventory: Inventory, pPlayer: Player) =
        BotAssemblerMenu(pContainerId, pPlayerInventory, mcu, storage, components)

    override fun getDisplayName(): Component = Component.translatable("menu.botjs.bot_assembler.title")

    override val sync = false

    override fun getUpdateTag(pRegistries: HolderLookup.Provider) = CompoundTag()

    override fun handleUpdateTag(tag: CompoundTag, lookupProvider: HolderLookup.Provider) {}
}