package com.sifsstudio.botjs.blockentity

import com.sifsstudio.botjs.item.Items
import com.sifsstudio.botjs.item.McuItem
import com.sifsstudio.botjs.item.component.DataComponents
import com.sifsstudio.botjs.item.component.FirmwareComponent
import com.sifsstudio.botjs.runtime.threading.SERVER_THREAD
import com.sifsstudio.botjs.runtime.threading.tryCompile
import com.sifsstudio.botjs.util.isItem
import com.sifsstudio.botjs.util.set
import kotlinx.coroutines.future.asCompletableFuture
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler
import java.util.UUID

class FirmwareProgrammerBlockEntity(pPos: BlockPos, pBlockState: BlockState) :
    BaseBlockEntity(BlockEntities.FIRMWARE_PROGRAMMER, pPos, pBlockState) {

    val mcu = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack) = stack.item is McuItem
    }

    val script = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack) = stack isItem Items.SCRIPT
    }

    var currentSession: UUID? = null

    var compileResult: Component = Component.empty()
    private var compileInProgress = false

    override fun saveAdditional(pTag: CompoundTag, pRegistries: HolderLookup.Provider) {
        super.saveAdditional(pTag, pRegistries)
        pTag["mcu"] = mcu.serializeNBT(pRegistries)
        pTag["script"] = script.serializeNBT(pRegistries)
        pTag["compileResult"] = Component.Serializer.toJson(compileResult, pRegistries)
    }

    override fun loadAdditional(pTag: CompoundTag, pRegistries: HolderLookup.Provider) {
        super.loadAdditional(pTag, pRegistries)
        mcu.deserializeNBT(pRegistries, pTag.getCompound("mcu"))
        script.deserializeNBT(pRegistries, pTag.getCompound("script"))
        compileResult = Component.Serializer.fromJson(pTag.getString("compileResult"), pRegistries) ?: Component.empty()
    }

    override val sync = true

    override fun getUpdateTag(pRegistries: HolderLookup.Provider) = super.getUpdateTag(pRegistries).apply {
        this["compileInProgress"] = compileInProgress
    }

    override fun handleUpdateTag(tag: CompoundTag, lookupProvider: HolderLookup.Provider) {
        super.handleUpdateTag(tag, lookupProvider)
        this.compileInProgress = tag.getBoolean("compileInProgress")
    }

    fun flash() {
        val script = script.getStackInSlot(0).get(DataComponents.SCRIPT) ?: ""
        if (mcu.getStackInSlot(0) == ItemStack.EMPTY) {
            compileResult = Component.translatable("menu.botjs.firmware_programmer.flash.nothing_to_flash")
                .withColor(0xFF0000)
            syncChange()
        } else {
            if (compileInProgress) {
                return
            }
            compileInProgress = true
            syncChange()
            tryCompile(script).asCompletableFuture().whenCompleteAsync({ compile, exception ->
                compileInProgress = false
                compile.ifLeft {
                    // successfully compiled
                    val mcuStack = mcu.extractItem(0, 1, false)
                    if (mcuStack.item is McuItem) {
                        try {
                            mcuStack.set(DataComponents.FIRMWARE, FirmwareComponent(
                                "SCRIPT",
                                it,
                            ))
                        } catch (err: Exception) {
                            err.printStackTrace()
                        }
                        compileResult = Component.translatable("menu.botjs.firmware_programmer.flash.success")
                            .withColor(0x00FF00)
                    } else {
                        compileResult =
                            Component.translatable("menu.botjs.firmware_programmer.flash.nothing_to_flash")
                                .withColor(0xFF0000)
                    }
                    mcu.insertItem(0, mcuStack, false)
                }.ifRight {
                    // compilation error
                    compileResult =
                        Component.translatable("menu.botjs.firmware_programmer.flash.error", it.toString())
                            .withColor(0xFF0000)
                }
                if (exception != null) {
                // cancellation
                }
                syncChange() }, SERVER_THREAD)
        }
    }
}