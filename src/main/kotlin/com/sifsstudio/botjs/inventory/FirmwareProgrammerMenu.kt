package com.sifsstudio.botjs.inventory

import com.sifsstudio.botjs.item.McuItem
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.IItemHandler
import net.neoforged.neoforge.items.ItemStackHandler
import net.neoforged.neoforge.items.SlotItemHandler

class FirmwareProgrammerMenu(
    pContainerId: Int,
    pPlayerInventory: Inventory,
    val flasherPos: BlockPos,
    val script: String,
    mcuIn: IItemHandler,
    mcuOut: IItemHandler
) : AbstractContainerMenu(MenuTypes.FIRMWARE_PROGRAMMER, pContainerId) {

    init {
        addSlot(SlotItemHandler(mcuIn, 0, 353, 176))
        addSlot(SlotItemHandler(mcuOut, 0, 454, 176))
        for (i in 0..2) {
            for (k in 0..8) {
                addSlot(Slot(pPlayerInventory, k + i * 9 + 9, 332 + k * 18, 232 + i * 18))
            }
        }
        for (j in 0..8) {
            addSlot(Slot(pPlayerInventory, j, 332 + j * 18, 290))
        }
    }

    constructor(pContainerId: Int, pPlayerInventory: Inventory, pFriendlyByteBuf: FriendlyByteBuf) : this(
        pContainerId,
        pPlayerInventory,
        pFriendlyByteBuf.readBlockPos(),
        pFriendlyByteBuf.readUtf(),
        ItemStackHandler(1),
        ItemStackHandler(1)
    )

    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        val slot = this.slots[pIndex]
        var oldStack = ItemStack.EMPTY
        if (slot.hasItem()) {
            val newStack = slot.item
            oldStack = newStack.copy()
            if (pIndex == 1) {
                if (!this.moveItemStackTo(newStack, 2, 38, true)) {
                    return ItemStack.EMPTY
                }
                slot.onQuickCraft(newStack, oldStack)
            } else if (pIndex != 0) {
                if (newStack.item is McuItem) {
                    if (!this.moveItemStackTo(newStack, 0, 1, false)) {
                        return ItemStack.EMPTY
                    }
                } else if (pIndex in 2..28) {
                    if (!this.moveItemStackTo(newStack, 29, 38, false)) {
                        return ItemStack.EMPTY
                    }
                } else if (pIndex in 29..37 && !this.moveItemStackTo(newStack, 2, 29, false)) {
                    return ItemStack.EMPTY
                }
            } else if (!this.moveItemStackTo(newStack, 2, 38, false)) {
                return ItemStack.EMPTY
            }

            if (newStack.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            if (oldStack.count == newStack.count) {
                return ItemStack.EMPTY
            }

            slot.onTake(pPlayer, newStack)
        }
        return oldStack
    }

    override fun stillValid(pPlayer: Player) = true
}