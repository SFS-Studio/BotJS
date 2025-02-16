package com.sifsstudio.botjs.inventory

import com.sifsstudio.botjs.block.Blocks
import com.sifsstudio.botjs.item.Items
import com.sifsstudio.botjs.item.McuItem
import com.sifsstudio.botjs.util.isItem
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.IItemHandler
import net.neoforged.neoforge.items.ItemStackHandler
import net.neoforged.neoforge.items.SlotItemHandler
import java.util.*

class FirmwareProgrammerMenu(
    pContainerId: Int,
    pPlayerInventory: Inventory,
    val containerLevelAccess: ContainerLevelAccess,
    val session: UUID,
    script: IItemHandler,
    mcu: IItemHandler,
) : AbstractContainerMenu(MenuTypes.FIRMWARE_PROGRAMMER, pContainerId) {

    val pos
        get() = containerLevelAccess.evaluate { _, pos -> pos }.get()

    init {
        addSlot(SlotItemHandler(script, 0, 355, 179))
        addSlot(SlotItemHandler(mcu, 0, 439, 179))
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
        ContainerLevelAccess.create(pPlayerInventory.player.level(), pFriendlyByteBuf.readBlockPos()),
        pFriendlyByteBuf.readUUID(),
        ItemStackHandler(1),
        ItemStackHandler(1)
    )

    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        val slot = this.slots[pIndex]
        var quickMovedStack = ItemStack.EMPTY
        if (slot.hasItem()) {
            val rawStack = slot.item
            quickMovedStack = rawStack.copy()
            if (pIndex in 2..38) {
                if (quickMovedStack.item is McuItem) {
                    if (!this.moveItemStackTo(rawStack, 1, 2, false)) {
                        if (pIndex < 29) {
                            if (!this.moveItemStackTo(rawStack, 29, 38, false)) {
                                return ItemStack.EMPTY
                            }
                        } else if (!this.moveItemStackTo(rawStack, 2, 29, false)) {
                            return ItemStack.EMPTY
                        }
                    }
                } else if (quickMovedStack isItem Items.SCRIPT) {
                    if (!this.moveItemStackTo(rawStack, 0, 1, false)) {
                        if (pIndex < 29) {
                            if (!this.moveItemStackTo(rawStack, 29, 38, false)) {
                                return ItemStack.EMPTY
                            }
                        } else if (!this.moveItemStackTo(rawStack, 2, 29, false)) {
                            return ItemStack.EMPTY
                        }
                    }
                } else {
                    if (pIndex < 29) {
                        if (!this.moveItemStackTo(rawStack, 29, 38, false)) {
                            return ItemStack.EMPTY
                        }
                    } else if (!this.moveItemStackTo(rawStack, 2, 29, false)) {
                        return ItemStack.EMPTY
                    }
                }
            } else if (!this.moveItemStackTo(rawStack, 2, 38, false)) {
                return ItemStack.EMPTY
            }

            if (rawStack.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }

            if (quickMovedStack.count == rawStack.count) {
                return ItemStack.EMPTY
            }

            slot.onTake(pPlayer, rawStack)
        }
        return quickMovedStack
    }

    override fun stillValid(pPlayer: Player) = stillValid(containerLevelAccess, pPlayer, Blocks.FIRMWARE_PROGRAMMER)
}