package com.sifsstudio.botjs.inventory

import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.IItemHandler
import net.neoforged.neoforge.items.ItemStackHandler

class BotAssemblerMenu(
    pContainerId: Int, pPlayerInventory: Inventory, mcu: IItemHandler, storage: IItemHandler, components: IItemHandler
) : AbstractContainerMenu(MenuTypes.BOT_ASSEMBLER, pContainerId) {
    init {

    }

    constructor(pContainerId: Int, pPlayerInventory: Inventory): this(
        pContainerId, pPlayerInventory, ItemStackHandler(1), ItemStackHandler(1), ItemStackHandler(18)
    )

    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun stillValid(pPlayer: Player): Boolean = true
}