package com.sifsstudio.botjs.entity

import com.sifsstudio.botjs.inventory.BotMountMenu
import com.sifsstudio.botjs.item.BotModuleItem
import com.sifsstudio.botjs.item.Items
import com.sifsstudio.botjs.runtime.BotRuntime
import com.sifsstudio.botjs.util.isItem
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.Level
import net.neoforged.neoforge.items.ItemStackHandler

class BotEntity(type: EntityType<BotEntity>, level: Level) : Mob(type, level), MenuProvider {

    val runtime = BotRuntime()
    val modules = ItemStackHandler(9)

    private fun recollectModule() {
        runtime.clearModule()
        for (i in 0..<modules.slots) {
            val item = modules.getStackInSlot(i).item
            if (item is BotModuleItem) {
                runtime.installModule(item.module())
            }
        }
    }

    //TODO: Thread safety when unload
    override fun addAdditionalSaveData(pCompound: CompoundTag) {
        super.addAdditionalSaveData(pCompound)
        if (!level().isClientSide) {
            pCompound.put("runtime", runtime.serializeNBT(this.registryAccess()))
            pCompound.put(
                "modules",
                modules.serializeNBT(this.registryAccess())
            )
        }
    }

    override fun readAdditionalSaveData(pCompound: CompoundTag) {
        super.readAdditionalSaveData(pCompound)
        if (!level().isClientSide) {
            runtime.deserializeNBT(this.registryAccess(), pCompound.getCompound("runtime"))
            modules.deserializeNBT(this.registryAccess(), pCompound.getCompound("modules"))
        }
    }

    override fun onAddedToWorld() {
        super.onAddedToWorld()
        if (!level().isClientSide) {
            recollectModule()
        }
    }

    override fun onRemovedFromWorld() {
        super.onRemovedFromWorld()
        if (!level().isClientSide) {
            runtime.interrupt()
        }
    }

    override fun tick() {
        super.tick()
        if (!level().isClientSide) {
            runtime.tick(this)
        }
    }

    override fun mobInteract(pPlayer: Player, pHand: InteractionHand): InteractionResult {
        if (pPlayer.getItemInHand(pHand) isItem Items.WRENCH) {
            if (!this.level().isClientSide && pPlayer is ServerPlayer && !runtime.isRunning) {
                pPlayer.openMenu(this)
            }
        } else if (pPlayer.getItemInHand(pHand) isItem Items.SWITCH) {
            if (!this.level().isClientSide) {
                if (!runtime.isRunning) {
                    recollectModule()
                    runtime.launch()
                } else {
                    runtime.stop()
                }
            }
        }
        return super.mobInteract(pPlayer, pHand)
    }

    override fun createMenu(pContainerId: Int, pPlayerInventory: Inventory, pPlayer: Player): AbstractContainerMenu =
        BotMountMenu(
            pContainerId,
            pPlayerInventory,
            modules
        )
}