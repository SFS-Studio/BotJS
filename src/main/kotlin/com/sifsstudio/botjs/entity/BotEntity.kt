package com.sifsstudio.botjs.entity

import com.sifsstudio.botjs.inventory.BotMountMenu
import com.sifsstudio.botjs.item.BotModuleItem
import com.sifsstudio.botjs.item.Items
import com.sifsstudio.botjs.item.component.DataComponents
import com.sifsstudio.botjs.runtime.BotRuntime
import com.sifsstudio.botjs.util.isItem
import com.sifsstudio.botjs.util.set
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.neoforged.neoforge.items.ItemStackHandler

class BotEntity(type: EntityType<BotEntity>, level: Level) : Mob(type, level), MenuProvider {

    val runtime = BotRuntime()
    val modules = ItemStackHandler(9)
    val mcu = ItemStackHandler(1)
    var needResume = false

    private fun recollectModule() {
        runtime.clearModule()
        for (i in 0..<modules.slots) {
            val item = modules.getStackInSlot(i).item
            if (item is BotModuleItem) {
                runtime.installModule(item.module())
            }
        }
    }

    override fun addAdditionalSaveData(pCompound: CompoundTag) {
        super.addAdditionalSaveData(pCompound)
        if (!level().isClientSide) {
            pCompound["modules"] = modules.serializeNBT(this.registryAccess())
            pCompound["needResume"] = needResume
        }
    }

    override fun readAdditionalSaveData(pCompound: CompoundTag) {
        super.readAdditionalSaveData(pCompound)
        if (!level().isClientSide) {
            modules.deserializeNBT(this.registryAccess(), pCompound.getCompound("modules"))
            needResume = pCompound.getBoolean("needResume")
        }
    }

    override fun onAddedToLevel() {
        super.onAddedToLevel()
        if (level().isClientSide) {
            return
        }
        recollectModule()
        if (needResume) {
            mcu.getStackInSlot(0).get(DataComponents.FIRMWARE)?.let {
                runtime.launch(it.byteCode)
            }
        }
    }

    override fun onRemovedFromLevel() {
        super.onRemovedFromLevel()
        if (!level().isClientSide) {
            needResume = runtime.interrupt()
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
                    mcu.getStackInSlot(0).get(DataComponents.FIRMWARE)?.let {
                        runtime.launch(it.byteCode)
                    }
                } else {
                    runtime.stop()
                }
            }
        }
        return super.mobInteract(pPlayer, pHand)
    }

    override fun createMenu(pContainerId: Int, pPlayerInventory: Inventory, pPlayer: Player) =
        BotMountMenu(
            pContainerId,
            pPlayerInventory,
            modules
        )
}